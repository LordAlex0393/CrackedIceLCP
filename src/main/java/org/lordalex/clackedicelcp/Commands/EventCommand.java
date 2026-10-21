package org.lordalex.clackedicelcp.Commands;

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
            sender.sendMessage(ColorUtil.get("&a&l✔ &aПлатформы запущены."));
            return true;
        }
        if (args[0].equalsIgnoreCase("stop")) {
            plugin.getPlatformManager().stopAll();
            sender.sendMessage(ColorUtil.get("&c&l✖ &cПлатформы остановлены."));
            return true;
        }
        if (args[0].equalsIgnoreCase("reset")) {
            plugin.getPlatformManager().resetAll();
            sender.sendMessage(ColorUtil.get("&a&l✔ &aПлатформы сброшены в исходное состояние."));
            return true;
        }
        return true;
    }
}