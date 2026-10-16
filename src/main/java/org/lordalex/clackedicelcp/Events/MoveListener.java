package org.lordalex.clackedicelcp.Events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.Game;
import org.lordalex.clackedicelcp.PlayerInfo;

public class MoveListener implements Listener {

    private final ClackedIceLCP plugin;

    public MoveListener(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;

        // реагируем только на смену блока
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player p = e.getPlayer();
        Block under = to.clone().subtract(0, 1, 0).getBlock();

        Game game = plugin.getGame();

        // === СЛУЧАЙ 1: другой игрок наступил на "обычный" лёд (прожатый) ===
        if (under.getType() == Material.ICE) {
            // если этот блок — часть лабиринта и уже прожат
            if (game.isMazeBlock(under) && game.isPressed(under)) {
                // проверяем: тот, кто наступил, — НЕ тот, кто первый стартовал?
                // по сути, любой игрок, который наступил на уже прожатый ICE — фейл.
                game.onIceStepByOther(p, under);
            }
            return;
        }

        // === СЛУЧАЙ 2: игрок наступил на плотный лёд — основная механика ===
        if (under.getType() == Material.PACKED_ICE) {
            if (game.isMazeBlock(under)) {
                game.onPlayerStep(p, under);
            }
        }
    }
}