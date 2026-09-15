package com.fertilia.farming;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

final class CropType {
    private final String id;
    private String matureBlockData;
    private String replantBlockData;
    private String material;
    private String rewardItemKey;
    private ItemStack rewardItem;
    private int rewardMin = 1;
    private int rewardMax = 1;
    private boolean enabled = true;

    CropType(String id, String matureBlockData, String material) {
        this.id = normalizeId(id);
        this.matureBlockData = matureBlockData;
        this.material = material;
    }

    static String normalizeId(String id) {
        return id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "_");
    }

    static CropType fromConfig(String id, ConfigurationSection section) {
        CropType crop = new CropType(id, section.getString("mature-block-data", ""), section.getString("material", ""));
        crop.replantBlockData = section.getString("replant-block-data", null);
        crop.rewardItemKey = section.getString("reward.item-key", null);
        crop.rewardItem = section.getItemStack("reward.item");
        crop.rewardMin = Math.max(1, section.getInt("reward.min", 1));
        crop.rewardMax = Math.max(crop.rewardMin, section.getInt("reward.max", crop.rewardMin));
        crop.enabled = section.getBoolean("enabled", true);
        return crop;
    }

    void save(ConfigurationSection section) {
        section.set("mature-block-data", matureBlockData);
        section.set("replant-block-data", replantBlockData);
        section.set("material", material);
        section.set("enabled", enabled);
        section.set("reward.item-key", rewardItemKey);
        section.set("reward.item", rewardItem);
        section.set("reward.min", rewardMin);
        section.set("reward.max", rewardMax);
    }

    String id() {
        return id;
    }

    String matureBlockData() {
        return matureBlockData;
    }

    String replantBlockData() {
        return replantBlockData;
    }

    void setReplantBlockData(String replantBlockData) {
        this.replantBlockData = replantBlockData;
    }

    String material() {
        return material;
    }

    String rewardItemKey() {
        return rewardItemKey;
    }

    ItemStack rewardItem() {
        return rewardItem == null ? null : rewardItem.clone();
    }

    int rewardMin() {
        return rewardMin;
    }

    int rewardMax() {
        return rewardMax;
    }

    void setRewardKey(String rewardItemKey, int min, int max) {
        this.rewardItemKey = rewardItemKey;
        this.rewardItem = null;
        setRewardRange(min, max);
    }

    void setRewardItem(ItemStack rewardItem, int min, int max) {
        this.rewardItem = rewardItem == null ? null : rewardItem.clone();
        this.rewardItemKey = null;
        setRewardRange(min, max);
    }

    boolean enabled() {
        return enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void setRewardRange(int min, int max) {
        this.rewardMin = Math.max(1, min);
        this.rewardMax = Math.max(this.rewardMin, max);
    }
}
