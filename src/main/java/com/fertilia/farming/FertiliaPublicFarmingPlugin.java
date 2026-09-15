package com.fertilia.farming;

import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FertiliaPublicFarmingPlugin extends JavaPlugin {
    private CropRegistry cropRegistry;
    private ItemRegistry itemRegistry;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        itemRegistry = new ItemRegistry(this);
        cropRegistry = new CropRegistry(this, itemRegistry);
        itemRegistry.load();
        cropRegistry.load();

        FarmingCommand farmingCommand = new FarmingCommand(this, cropRegistry, itemRegistry);
        ItemCommand itemCommand = new ItemCommand(this, itemRegistry);

        registerCommand("ffarm", farmingCommand);
        registerCommand("fitem", itemCommand);

        getServer().getPluginManager().registerEvents(new FarmingListener(this, cropRegistry), this);
        getServer().getPluginManager().registerEvents(new ItemRegistryMenu(this, itemRegistry), this);

        getLogger().info("Loaded " + cropRegistry.size() + " crop types and " + itemRegistry.size() + " registered items.");
    }

    @Override
    public void onDisable() {
        if (itemRegistry != null) {
            itemRegistry.save();
        }
        if (cropRegistry != null) {
            cropRegistry.save();
        }
    }

    String farmingPrefix() {
        return color(getConfig().getString("messages.farming-prefix", "&8[&aFertilia Farming&8]&r "));
    }

    String itemsPrefix() {
        return color(getConfig().getString("messages.items-prefix", "&8[&bFertilia Items&8]&r "));
    }

    static String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value == null ? "" : value);
    }

    private void registerCommand(String name, BaseCommand command) {
        PluginCommand pluginCommand = getCommand(name);
        if (pluginCommand == null) {
            throw new IllegalStateException("Missing command in plugin.yml: " + name);
        }
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }
}
