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
import org.lordalex.clackedicelcp.GameState;
import org.lordalex.clackedicelcp.Maze;

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

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player p = e.getPlayer();
        Game game = plugin.getGame();

        if (game.isIgnored(p.getUniqueId())) return;

        int steps = Math.max(
                Math.abs(to.getBlockX() - from.getBlockX()),
                Math.max(
                        Math.abs(to.getBlockY() - from.getBlockY()),
                        Math.abs(to.getBlockZ() - from.getBlockZ())
                )
        );
        if (steps <= 0) steps = 1;

        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            double x = from.getX() + (to.getX() - from.getX()) * t;
            double y = from.getY() + (to.getY() - from.getY()) * t;
            double z = from.getZ() + (to.getZ() - from.getZ()) * t;

            Block under = new Location(p.getWorld(),
                    Math.floor(x), Math.floor(y) - 1, Math.floor(z)).getBlock();

            Maze maze = game.getMazeByBlock(under);
            if (maze == null) continue;

            if (under.getType() == Material.PACKED_ICE) {
                maze.onPlayerStep(p, under);
            } else if (under.getType() == Material.ICE) {
                maze.onIceStepByOther(p, under);
            }

            if (maze.getState() == GameState.RESETTING || maze.getState() == GameState.WAITING) {
                break;
            }
        }
    }
}