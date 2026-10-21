package org.lordalex.clackedicelcp.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.Maze;

public class QuitListener implements Listener {

    private final ClackedIceLCP plugin;

    public QuitListener(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        for (Maze m : plugin.getGame().getMazes()) {
            m.getActivePlayers().remove(e.getPlayer().getUniqueId());
        }
    }
}