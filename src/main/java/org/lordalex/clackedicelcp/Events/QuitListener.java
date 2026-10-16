package org.lordalex.clackedicelcp.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lordalex.clackedicelcp.ClackedIceLCP;

public class QuitListener implements Listener {

    private final ClackedIceLCP plugin;

    public QuitListener(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getGame().getActivePlayers().remove(e.getPlayer().getUniqueId());
    }
}