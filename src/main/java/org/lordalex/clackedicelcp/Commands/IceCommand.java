package org.lordalex.clackedicelcp.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.Maze;
import org.lordalex.clackedicelcp.Utils.ColorUtil;

public class IceCommand implements CommandExecutor {

    private final ClackedIceLCP plugin;

    public IceCommand(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.get("&e/icemaze reset [id] &7— сбросить лабиринт(ы)"));
            sender.sendMessage(ColorUtil.get("&e/icemaze list &7— список лабиринтов"));
            sender.sendMessage(ColorUtil.get("&e/icemaze time <on|off> &7— вкл/выкл отображение времени"));
            return true;
        }

        if (args[0].equalsIgnoreCase("time")) {
            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("on")) {
                    plugin.getGame().setShowTime(true);
                    sender.sendMessage(ColorUtil.get("&aОтображение времени &lВКЛЮЧЕНО&a."));
                } else if (args[1].equalsIgnoreCase("off")) {
                    plugin.getGame().setShowTime(false);
                    sender.sendMessage(ColorUtil.get("&cОтображение времени &lВЫКЛЮЧЕНО&c."));
                } else {
                    sender.sendMessage(ColorUtil.get("&cИспользуй: /icemaze time <on|off>"));
                }
            } else {
                sender.sendMessage(ColorUtil.get("&cИспользуй: /icemaze time <on|off>"));
            }
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
                plugin.getGame().resetMaze(args[1]);
                sender.sendMessage("§aЛабиринт §e" + args[1] + " §aсброшен. Время сброшено.");
            } else {
                plugin.getGame().resetAll();
                sender.sendMessage("§aВсе лабиринты сброшены. Время сброшено.");
            }
            return true;
        }

        return true;
    }
}