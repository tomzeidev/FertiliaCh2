package com.fertilia.farming;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

final class CropRegistry {
    private final FertiliaPublicFarmingPlugin plugin;
    private final ItemRegistry itemRegistry;
    private final File file;
    private final Map<String, CropType> cropsById = new HashMap<>();
    private final Map<String, CropType> cropsByMatureBlockData = new HashMap<>();

    CropRegistry(FertiliaPublicFarmingPlugin plugin, ItemRegistry itemRegistry) {
        this.plugin = plugin;
        this.itemRegistry = itemRegistry;
        this.file = new File(plugin.getDataFolder(), "crops.yml");
    }

    void load() {
        cropsById.clear();
        cropsByMatureBlockData.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection crops = yaml.getConfigurationSection("crops");
        if (crops == null) {
            return;
        }
        for (String id : crops.getKeys(false)) {
            ConfigurationSection section = crops.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            CropType crop = CropType.fromConfig(id, section);
            if (!crop.matureBlockData().isBlank()) {
                cropsById.put(crop.id(), crop);
                cropsByMatureBlockData.put(crop.matureBlockData(), crop);
            }
        }
    }

    void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection crops = yaml.createSection("crops");
        for (CropType crop : cropsById.values()) {
            crop.save(crops.createSection(crop.id()));
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save crops.yml", e);
        }
    }

    CropType registerFromBlock(String id, Block block) {
        String normalizedId = CropType.normalizeId(id);
        CropType existing = cropsById.get(normalizedId);
        String blockData = block.getBlockData().getAsString();
        if (existing != null) {
            cropsByMatureBlockData.remove(existing.matureBlockData());
        }
        CropType crop = new CropType(normalizedId, blockData, block.getType().name());
        cropsById.put(crop.id(), crop);
        cropsByMatureBlockData.put(blockData, crop);
        save();
        return crop;
    }

    Optional<CropType> byId(String id) {
        return Optional.ofNullable(cropsById.get(CropType.normalizeId(id)));
    }

    Optional<CropType> byBlock(Block block) {
        return Optional.ofNullable(cropsByMatureBlockData.get(block.getBlockData().getAsString()));
    }

    Collection<CropType> all() {
        return cropsById.values();
    }

    int size() {
        return cropsById.size();
    }

    void setReplant(CropType crop, Block block) {
        crop.setReplantBlockData(block.getBlockData().getAsString());
        save();
    }

    void giveReward(CropType crop, org.bukkit.entity.Player player) {
        ItemStack reward = null;
        if (crop.rewardItemKey() != null) {
            reward = itemRegistry.get(crop.rewardItemKey()).orElse(null);
        } else {
            reward = crop.rewardItem();
        }
        if (reward == null || reward.getType() == Material.AIR) {
            return;
        }
        int amount = crop.rewardMin();
        if (crop.rewardMax() > crop.rewardMin()) {
            amount += java.util.concurrent.ThreadLocalRandom.current().nextInt(crop.rewardMax() - crop.rewardMin() + 1);
        }
        reward.setAmount(amount);
        player.getInventory().addItem(reward).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }

    void replant(CropType crop, Block block) {
        if (crop.replantBlockData() == null || crop.replantBlockData().isBlank()) {
            block.setType(Material.AIR, true);
            return;
        }
        BlockData data = Bukkit.createBlockData(crop.replantBlockData());
        block.setBlockData(data, true);
    }
}
