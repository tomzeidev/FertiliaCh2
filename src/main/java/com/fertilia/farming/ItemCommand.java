package com.fertilia.farming;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ItemCommand implements BaseCommand {
    private final FertiliaPublicFarmingPlugin plugin;
    private final ItemRegistry itemRegistry;

    ItemCommand(FertiliaPublicFarmingPlugin plugin, ItemRegistry itemRegistry) {
        this.plugin = plugin;
        this.itemRegistry = itemRegistry;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be used in-game.");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            help(player);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("save")) {
            save(player, args);
            return true;
        }
        if (sub.equals("give")) {
            give(player, args);
            return true;
        }
        if (sub.equals("delete")) {
            delete(player, args);
            return true;
        }
        if (sub.equals("menu")) {
            ItemRegistryMenu.open(player, itemRegistry, 1);
            return true;
        }
        player.sendMessage(plugin.itemsPrefix() + "§cUnknown subcommand. Use §f/fitem help§c.");
        return true;
    }

    private void help(Player player) {
        player.sendMessage(plugin.itemsPrefix() + "§f/fitem save <key> §7- save your held item");
        player.sendMessage(plugin.itemsPrefix() + "§f/fitem give <key> [player]");
        player.sendMessage(plugin.itemsPrefix() + "§f/fitem delete <key>");
        player.sendMessage(plugin.itemsPrefix() + "§f/fitem menu");
    }

    private void save(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.itemsPrefix() + "§cUsage: /fitem save <key>");
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) {
            player.sendMessage(plugin.itemsPrefix() + "§cHold the item you want to register.");
            return;
        }
        itemRegistry.put(args[1], held);
        player.sendMessage(plugin.itemsPrefix() + "§aSaved held item as §f" + CropType.normalizeId(args[1]) + "§a.");
    }

    private void give(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.itemsPrefix() + "§cUsage: /fitem give <key> [player]");
            return;
        }
        Player target = player;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                player.sendMessage(plugin.itemsPrefix() + "§cThat player is not online.");
                return;
            }
        }
        ItemStack item = itemRegistry.get(args[1]).orElse(null);
        if (item == null) {
            player.sendMessage(plugin.itemsPrefix() + "§cUnknown registry key.");
            return;
        }
        Player finalTarget = target;
        finalTarget.getInventory().addItem(item).values().forEach(leftover ->
                finalTarget.getWorld().dropItemNaturally(finalTarget.getLocation(), leftover));
        player.sendMessage(plugin.itemsPrefix() + "§aGave §f" + CropType.normalizeId(args[1]) + " §ato §f" + finalTarget.getName() + "§a.");
    }

    private void delete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.itemsPrefix() + "§cUsage: /fitem delete <key>");
            return;
        }
        itemRegistry.remove(args[1]);
        player.sendMessage(plugin.itemsPrefix() + "§eDeleted §f" + CropType.normalizeId(args[1]) + "§e.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return startsWith(args[0], List.of("help", "save", "give", "delete", "menu"));
        }
        if (args.length == 2 && List.of("give", "delete").contains(args[0].toLowerCase(Locale.ROOT))) {
            return startsWith(args[1], new ArrayList<>(itemRegistry.keys()));
        }
        return List.of();
    }

    private List<String> startsWith(String prefix, List<String> values) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }
}
