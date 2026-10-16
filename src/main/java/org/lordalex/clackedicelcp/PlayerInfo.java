package org.lordalex.clackedicelcp;

import org.bukkit.entity.Player;

public class PlayerInfo {
    private final Player player;
    private long startTime;

    public PlayerInfo(Player player) {
        this.player = player;
    }

    public Player getPlayer() { return player; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long t) { this.startTime = t; }
}