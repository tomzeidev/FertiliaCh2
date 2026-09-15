package com.fertilia.farming;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

final class FarmingListener implements Listener {
    private final FertiliaPublicFarmingPlugin plugin;
    private final CropRegistry cropRegistry;

    FarmingListener(FertiliaPublicFarmingPlugin plugin, CropRegistry cropRegistry) {
        this.plugin = plugin;
        this.cropRegistry = cropRegistry;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        if (plugin.getConfig().getBoolean("harvest.require-use-permission", true)
                && !event.getPlayer().hasPermission("fertilia.farming.use")) {
            return;
        }
        cropRegistry.byBlock(event.getClickedBlock()).ifPresent(crop -> {
            if (!crop.enabled()) {
                return;
            }
            event.setCancelled(true);
            cropRegistry.giveReward(crop, event.getPlayer());
            cropRegistry.replant(crop, event.getClickedBlock());
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("harvest.protect-registered-crops-from-breaking", true)) {
            return;
        }
        cropRegistry.byBlock(event.getBlock()).ifPresent(crop -> {
            if (!event.getPlayer().hasPermission("fertilia.farming.admin")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.farmingPrefix() + "§cUse right click to harvest public crops.");
            }
        });
    }
}
