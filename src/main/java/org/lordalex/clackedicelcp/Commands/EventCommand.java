package org.lordalex.clackedicelcp.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.Utils.ColorUtil;

public class EventCommand implements CommandExecutor {
    private final ClackedIceLCP plugin;

    public EventCommand(ClackedIceLCP plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.get("&e/event start|stop|reset"));
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            plugin.getPlatformManager().startAll();
            plugin.getSnowmanManager().spawnAll();

            plugin.getGame().setEventRunning(true); // Ставим флаг

            // ДОБАВИТЬ ЭТО: Обновляем табло всем игрокам
            for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                plugin.getScoreboardManager().updateBoard(p);
            }

            sender.sendMessage(ColorUtil.get("&a&l✔ &aИвент запущен."));
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            plugin.getPlatformManager().stopAll();
            plugin.getSnowmanManager().removeAll();

            plugin.getGame().setEventRunning(false); // Снимаем флаг

            // ДОБАВИТЬ ЭТО: Обновляем табло всем игрокам
            for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                plugin.getScoreboardManager().updateBoard(p);
            }

            sender.sendMessage(ColorUtil.get("&c&l✖ &cИвент остановлен."));
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            plugin.getPlatformManager().resetAll();
            plugin.getSnowmanManager().removeAll();

            plugin.getGame().setEventRunning(false); // Снимаем флаг

            // ДОБАВИТЬ ЭТО: Обновляем табло всем игрокам
            for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                plugin.getScoreboardManager().updateBoard(p);
            }

            sender.sendMessage(ColorUtil.get("&a&l✔ &aИвент сброшен."));
            return true;
        }
        return true;
    }
}