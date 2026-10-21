package org.lordalex.clackedicelcp.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.GameMode;
import org.lordalex.clackedicelcp.Maze;
import org.lordalex.clackedicelcp.Utils.ColorUtil;

public class IceCommand implements CommandExecutor {
    private final ClackedIceLCP plugin;

    public IceCommand(ClackedIceLCP plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.get("&8&m----------------------"));
            sender.sendMessage(ColorUtil.get("&e/icemaze time <on|off> &7— вкл/выкл время"));
            sender.sendMessage(ColorUtil.get("&e/icemaze mode <default|single|training> &7— сменить режим"));
            sender.sendMessage(ColorUtil.get("&e/icemaze list &7— список лабиринтов"));
            sender.sendMessage(ColorUtil.get("&e/icemaze reset [id] &7— сбросить"));
            sender.sendMessage(ColorUtil.get("&8&m----------------------"));
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(ColorUtil.get("&8&m--- Лабиринты ---"));
            for (Maze m : plugin.getGame().getMazes()) {
                sender.sendMessage(ColorUtil.get("&7- &f" + m.getId()
                        + " &7(Блоков: &f" + m.getMazeBlocksCount()
                        + "&7, Статус: &f" + m.getState().name() + "&7)"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("time")) {
            if (args.length >= 2) {
                boolean b = args[1].equalsIgnoreCase("on");
                plugin.getGame().setShowTime(b);
                if (b) sender.sendMessage(ColorUtil.get("&a&l✔ &aОтображение времени: &lВКЛЮЧЕНО"));
                else sender.sendMessage(ColorUtil.get("&c&l✖ &cОтображение времени: &lВЫКЛЮЧЕНО"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("mode")) {
            if (args.length >= 2) {
                try {
                    GameMode mode = GameMode.valueOf(args[1].toUpperCase());
                    plugin.getGame().setGameMode(mode);
                    plugin.getGame().resetAll(); // Смена мода = полный сброс

                    String modeName = "";
                    if (mode == GameMode.DEFAULT) modeName = "&aОбычный (сюжет)";
                    else if (mode == GameMode.SINGLE) modeName = "&eОдиночный (цикл)";
                    else if (mode == GameMode.TRAINING) modeName = "&bТренировка";

                    sender.sendMessage(ColorUtil.get("&8&m----------------------"));
                    sender.sendMessage(ColorUtil.get("&a&l✔ &aРежим успешно изменен!"));
                    sender.sendMessage(ColorUtil.get("&7Текущий режим: " + modeName));
                    sender.sendMessage(ColorUtil.get("&7Все лабиринты сброшены."));
                    sender.sendMessage(ColorUtil.get("&8&m----------------------"));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.get("&c&l✖ &cНеверный режим! Доступно: default, single, training"));
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (args.length >= 2) {
                Maze m = plugin.getGame().getMazeById(args[1]);
                if (m == null) {
                    sender.sendMessage(ColorUtil.get("&cЛабиринт не найден."));
                    return true;
                }
                plugin.getGame().resetMaze(args[1]);
                sender.sendMessage(ColorUtil.get("&a&l✔ &aЛабиринт &e" + args[1] + " &aсброшен."));
            } else {
                plugin.getGame().resetAll();
                sender.sendMessage(ColorUtil.get("&a&l✔ &aВсе лабиринты сброшены."));
            }
            return true;
        }
        return true;
    }
}