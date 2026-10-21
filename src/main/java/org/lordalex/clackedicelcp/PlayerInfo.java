package org.lordalex.clackedicelcp;

import org.bukkit.entity.Player;

public class PlayerInfo {
    private final Player player;

    public PlayerInfo(Player player) {
        this.player = player;
    }

    public Player getPlayer() { return player; }
}