package org.lordalex.clackedicelcp.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.Maze;

public class IceCommand implements CommandExecutor {

    private final ClackedIceLCP plugin;

    public IceCommand(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/icemaze reset [id] — сбросить лабиринт(ы)");
            sender.sendMessage("§e/icemaze list — список лабиринтов");
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage("§eЛабиринты:");
            for (Maze m : plugin.getGame().getMazes()) {
                sender.sendMessage("§7- §f" + m.getId()
                        + " §7(блоков: §f" + m.getMazeBlocksCount() + "§7, состояние: §f"
                        + m.getState() + "§7)");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (args.length >= 2) {
                Maze m = plugin.getGame().getMazeById(args[1]);
                if (m == null) {
                    sender.sendMessage("§cЛабиринт не найден: " + args[1]);
                    return true;
                }
                m.reset();
                sender.sendMessage("§aЛабиринт §e" + args[1] + " §aсброшен.");
            } else {
                plugin.getGame().resetAll();
                sender.sendMessage("§aВсе лабиринты сброшены.");
            }
            return true;
        }

        return true;
    }
}