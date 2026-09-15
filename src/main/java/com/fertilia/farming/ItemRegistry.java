package com.fertilia.farming;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

final class ItemRegistry {
    private final FertiliaPublicFarmingPlugin plugin;
    private final File file;
    private final Map<String, ItemStack> items = new LinkedHashMap<>();

    ItemRegistry(FertiliaPublicFarmingPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "items.yml");
    }

    void load() {
        items.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("items");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ItemStack item = section.getItemStack(key);
            if (item != null) {
                items.put(CropType.normalizeId(key), item);
            }
        }
    }

    void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection section = yaml.createSection("items");
        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            section.set(entry.getKey(), entry.getValue());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save items.yml", e);
        }
    }

    void put(String key, ItemStack item) {
        items.put(CropType.normalizeId(key), item.clone());
        save();
    }

    Optional<ItemStack> get(String key) {
        ItemStack item = items.get(CropType.normalizeId(key));
        return item == null ? Optional.empty() : Optional.of(item.clone());
    }

    void remove(String key) {
        items.remove(CropType.normalizeId(key));
        save();
    }

    boolean contains(String key) {
        return items.containsKey(CropType.normalizeId(key));
    }

    Set<String> keys() {
        return items.keySet();
    }

    int size() {
        return items.size();
    }
}
