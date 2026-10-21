package org.lordalex.clackedicelcp.Scoreboard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lordalex.clackedicelcp.ClackedIceLCP;

public class ScoreboardListener implements Listener {
    private final ClackedIceLCP plugin;

    public ScoreboardListener(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getScoreboardManager().createBoard(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getScoreboardManager().removeBoard(e.getPlayer());
    }
}