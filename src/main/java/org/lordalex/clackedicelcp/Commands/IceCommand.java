package org.lordalex.clackedicelcp.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.lordalex.clackedicelcp.ClackedIceLCP;

public class IceCommand implements CommandExecutor {

    private final ClackedIceLCP plugin;

    public IceCommand(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/icemaze reload — перезагрузить конфиг");
            sender.sendMessage("§e/icemaze reset — сбросить лабиринт");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getGame().loadMaze();
            sender.sendMessage("§aКонфиг перезагружен.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            plugin.getGame().resetMaze();
            sender.sendMessage("§aЛабиринт сброшен.");
            return true;
        }

        return true;
    }
}