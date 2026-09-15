package com.fertilia.farming;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class ItemRegistryMenu implements Listener {
    private static final String TITLE = "§8Fertilia Item Registry";
    private static final int PAGE_SIZE = 45;
    private final FertiliaPublicFarmingPlugin plugin;
    private final ItemRegistry itemRegistry;

    ItemRegistryMenu(FertiliaPublicFarmingPlugin plugin, ItemRegistry itemRegistry) {
        this.plugin = plugin;
        this.itemRegistry = itemRegistry;
    }

    static void open(Player player, ItemRegistry registry, int page) {
        int safePage = Math.max(1, page);
        Inventory inventory = Bukkit.createInventory(player, 54, TITLE + " §7" + safePage);
        List<String> keys = registry.keys().stream().sorted(Comparator.naturalOrder()).toList();
        int start = (safePage - 1) * PAGE_SIZE;
        for (int slot = 0; slot < PAGE_SIZE && start + slot < keys.size(); slot++) {
            String key = keys.get(start + slot);
            ItemStack item = registry.get(key).orElse(new ItemStack(Material.BARRIER));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("§7Key: §f" + key);
                lore.add("");
                lore.add("§eLeft click: receive one");
                lore.add("§cShift right click: delete");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot, item);
        }
        inventory.setItem(45, named(Material.ARROW, "§fPrevious page"));
        inventory.setItem(49, named(Material.PAPER, "§fPage " + safePage));
        inventory.setItem(53, named(Material.ARROW, "§fNext page"));
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!event.getView().getTitle().startsWith(TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (!player.hasPermission("fertilia.items.admin")) {
            player.closeInventory();
            return;
        }
        int page = currentPage(event.getView().getTitle());
        if (event.getRawSlot() == 45) {
            open(player, itemRegistry, page - 1);
            return;
        }
        if (event.getRawSlot() == 53) {
            open(player, itemRegistry, page + 1);
            return;
        }
        if (event.getRawSlot() < 0 || event.getRawSlot() >= PAGE_SIZE) {
            return;
        }
        List<String> keys = itemRegistry.keys().stream().sorted(Comparator.naturalOrder()).toList();
        int index = (page - 1) * PAGE_SIZE + event.getRawSlot();
        if (index >= keys.size()) {
            return;
        }
        String key = keys.get(index);
        if (event.getClick() == ClickType.LEFT) {
            itemRegistry.get(key).ifPresent(item -> player.getInventory().addItem(item).values()
                    .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover)));
            player.sendMessage(plugin.itemsPrefix() + "§aGave one §f" + key + "§a.");
        } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
            itemRegistry.remove(key);
            player.sendMessage(plugin.itemsPrefix() + "§eDeleted §f" + key + "§e.");
            open(player, itemRegistry, page);
        }
    }

    private static ItemStack named(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static int currentPage(String title) {
        String[] parts = title.split(" ");
        try {
            return Math.max(1, Integer.parseInt(parts[parts.length - 1].replace("§7", "")));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }
}
