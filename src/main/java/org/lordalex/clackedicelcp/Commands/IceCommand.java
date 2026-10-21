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
            GameMode mode = plugin.getGame().getGameMode();
            boolean time = plugin.getGame().isShowTime();

            String modeName = mode == GameMode.DEFAULT ? "&aСюжетный" : (mode == GameMode.SINGLE ? "&eОдиночный" : "&bТренировка");

            sender.sendMessage(ColorUtil.get("&8&m----------------------------------"));
            sender.sendMessage(ColorUtil.get("&6&l       УПРАВЛЕНИЕ ЛАБИРИНТОМ"));
            sender.sendMessage(ColorUtil.get("&7 Текущий режим: " + modeName));
            sender.sendMessage(ColorUtil.get("&7 Отображение времени: " + (time ? "&aВКЛ" : "&cВЫКЛ")));
            sender.sendMessage(ColorUtil.get("&8&m----------------------------------"));
            sender.sendMessage(ColorUtil.get("&e/icemaze time <on|off> &7— переключить время"));
            sender.sendMessage(ColorUtil.get("&e/icemaze mode <default|single|training> &7— сменить режим"));
            sender.sendMessage(ColorUtil.get("&e/icemaze list &7— список всех лабиринтов"));
            sender.sendMessage(ColorUtil.get("&e/icemaze reset [id] &7— полный сброс"));
            sender.sendMessage(ColorUtil.get("&8&m----------------------------------"));
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(ColorUtil.get("&8&m---&6&l Список лабиринтов &8&m---"));
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
                if (b) sender.sendMessage(ColorUtil.get("&a&l✔ &aВремя: ВКЛЮЧЕНО"));
                else sender.sendMessage(ColorUtil.get("&c&l✖ &cВремя: ВЫКЛЮЧЕНО"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("mode")) {
            if (args.length >= 2) {
                try {
                    GameMode mode = GameMode.valueOf(args[1].toUpperCase());
                    plugin.getGame().setGameMode(mode);
                    plugin.getGame().resetAll();

                    String modeName = mode == GameMode.DEFAULT ? "&aОбычный" : (mode == GameMode.SINGLE ? "&eОдиночный" : "&bТренировка");

                    sender.sendMessage(ColorUtil.get("&8&m----------------------"));
                    sender.sendMessage(ColorUtil.get("&a&l✔ &aРежим изменен на: " + modeName));
                    sender.sendMessage(ColorUtil.get("&7Все лабиринты полностью сброшены."));
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
                sender.sendMessage(ColorUtil.get("&a&l✔ &aЛабиринт &e" + args[1] + " &aполностью сброшен."));
            } else {
                plugin.getGame().resetAll();
                sender.sendMessage(ColorUtil.get("&a&l✔ &aВсе лабиринты полностью сброшены."));
            }
            return true;
        }
        return true;
    }
}