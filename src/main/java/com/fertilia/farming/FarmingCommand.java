package com.fertilia.farming;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class FarmingCommand implements BaseCommand {
    private final FertiliaPublicFarmingPlugin plugin;
    private final CropRegistry cropRegistry;
    private final ItemRegistry itemRegistry;

    FarmingCommand(FertiliaPublicFarmingPlugin plugin, CropRegistry cropRegistry, ItemRegistry itemRegistry) {
        this.plugin = plugin;
        this.cropRegistry = cropRegistry;
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
        if (sub.equals("register")) {
            register(player, args);
            return true;
        }
        if (sub.equals("replant")) {
            replant(player, args);
            return true;
        }
        if (sub.equals("reward")) {
            reward(player, args);
            return true;
        }
        if (sub.equals("toggle")) {
            toggle(player, args);
            return true;
        }
        if (sub.equals("list")) {
            list(player);
            return true;
        }

        player.sendMessage(plugin.farmingPrefix() + "§cUnknown subcommand. Use §f/ffarm help§c.");
        return true;
    }

    private void help(Player player) {
        player.sendMessage(plugin.farmingPrefix() + "§f/ffarm register <id> §7- capture the block state you are looking at");
        player.sendMessage(plugin.farmingPrefix() + "§f/ffarm replant <id> §7- capture the reset state you are looking at");
        player.sendMessage(plugin.farmingPrefix() + "§f/ffarm reward <id> <item-key|held> [min] [max]");
        player.sendMessage(plugin.farmingPrefix() + "§f/ffarm toggle <id>");
        player.sendMessage(plugin.farmingPrefix() + "§f/ffarm list");
    }

    private void register(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.farmingPrefix() + "§cUsage: /ffarm register <id>");
            return;
        }
        Block block = player.getTargetBlockExact(8);
        if (block == null || block.getType() == Material.AIR) {
            player.sendMessage(plugin.farmingPrefix() + "§cLook at the crop block state you want to register.");
            return;
        }
        CropType crop = cropRegistry.registerFromBlock(args[1], block);
        player.sendMessage(plugin.farmingPrefix() + "§aRegistered §f" + crop.id() + " §afrom §7" + crop.matureBlockData());
    }

    private void replant(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.farmingPrefix() + "§cUsage: /ffarm replant <id>");
            return;
        }
        Optional<CropType> crop = cropRegistry.byId(args[1]);
        if (crop.isEmpty()) {
            player.sendMessage(plugin.farmingPrefix() + "§cUnknown crop type.");
            return;
        }
        Block block = player.getTargetBlockExact(8);
        if (block == null || block.getType() == Material.AIR) {
            player.sendMessage(plugin.farmingPrefix() + "§cLook at the block state to restore after harvest.");
            return;
        }
        cropRegistry.setReplant(crop.get(), block);
        player.sendMessage(plugin.farmingPrefix() + "§aUpdated replant state for §f" + crop.get().id() + "§a.");
    }

    private void reward(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.farmingPrefix() + "§cUsage: /ffarm reward <id> <item-key|held> [min] [max]");
            return;
        }
        Optional<CropType> crop = cropRegistry.byId(args[1]);
        if (crop.isEmpty()) {
            player.sendMessage(plugin.farmingPrefix() + "§cUnknown crop type.");
            return;
        }
        int min = parseInt(args, 3, 1);
        int max = parseInt(args, 4, min);
        if (args[2].equalsIgnoreCase("held")) {
            ItemStack held = player.getInventory().getItemInMainHand();
            if (held.getType() == Material.AIR) {
                player.sendMessage(plugin.farmingPrefix() + "§cHold the reward item first.");
                return;
            }
            crop.get().setRewardItem(held, min, max);
        } else {
            if (!itemRegistry.contains(args[2])) {
                player.sendMessage(plugin.farmingPrefix() + "§cUnknown item registry key.");
                return;
            }
            crop.get().setRewardKey(args[2], min, max);
        }
        cropRegistry.save();
        player.sendMessage(plugin.farmingPrefix() + "§aUpdated reward for §f" + crop.get().id() + "§a.");
    }

    private void toggle(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.farmingPrefix() + "§cUsage: /ffarm toggle <id>");
            return;
        }
        Optional<CropType> crop = cropRegistry.byId(args[1]);
        if (crop.isEmpty()) {
            player.sendMessage(plugin.farmingPrefix() + "§cUnknown crop type.");
            return;
        }
        crop.get().setEnabled(!crop.get().enabled());
        cropRegistry.save();
        player.sendMessage(plugin.farmingPrefix() + (crop.get().enabled() ? "§aEnabled " : "§eDisabled ") + "§f" + crop.get().id());
    }

    private void list(Player player) {
        player.sendMessage(plugin.farmingPrefix() + "§fRegistered public crop types:");
        for (CropType crop : cropRegistry.all()) {
            player.sendMessage(" §8- §f" + crop.id() + " §7" + crop.material() + " " + (crop.enabled() ? "§aenabled" : "§cdisabled"));
        }
    }

    private int parseInt(String[] args, int index, int fallback) {
        if (args.length <= index) {
            return fallback;
        }
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return startsWith(args[0], List.of("help", "register", "replant", "reward", "toggle", "list"));
        }
        if (args.length == 2 && List.of("replant", "reward", "toggle").contains(args[0].toLowerCase(Locale.ROOT))) {
            return startsWith(args[1], cropRegistry.all().stream().map(CropType::id).toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("reward")) {
            List<String> values = new ArrayList<>(itemRegistry.keys());
            values.add("held");
            return startsWith(args[2], values);
        }
        return List.of();
    }

    private List<String> startsWith(String prefix, List<String> values) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }
}
