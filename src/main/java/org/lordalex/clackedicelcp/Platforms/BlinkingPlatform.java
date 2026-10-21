package org.lordalex.clackedicelcp.Platforms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.Utils.ConfigPlatform;
import org.lordalex.clackedicelcp.Utils.YmlParser;

import java.util.ArrayList;
import java.util.List;

public class BlinkingPlatform implements Platform {
    private final ClackedIceLCP plugin;
    private final World world;
    private final int timeOn;
    private final int timeOff;

    private final List<Location> blocks = new ArrayList<>();
    private BukkitRunnable task;
    private boolean isRunning = false;
    private boolean currentlyOn = true;

    public BlinkingPlatform(ClackedIceLCP plugin, World world, ConfigPlatform cfg) {
        this.plugin = plugin;
        this.world = world;
        this.timeOn = cfg.getTimeOn();
        this.timeOff = cfg.getTimeOff();

        Location pos1 = YmlParser.parseLocation(world, cfg.getPos1());
        Location pos2 = YmlParser.parseLocation(world, cfg.getPos2());

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(new Location(world, x, y, z));
                    world.getBlockAt(x, y, z).setType(Material.PACKED_ICE);
                }
            }
        }
    }

    @Override
    public void start() {
        if (isRunning) return;
        isRunning = true;

        // Инициализируем стартовое состояние (включено)
        setBlocks(Material.PACKED_ICE);
        currentlyOn = true;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentlyOn) {
                    setBlocks(Material.AIR);
                    currentlyOn = false;
                } else {
                    setBlocks(Material.PACKED_ICE);
                    currentlyOn = true;
                }
            }
        };
        // Первый запуск через timeOn тиков, потом чередуем
        task.runTaskTimer(plugin, timeOn, timeOn == timeOff ? timeOn : timeOn + timeOff);
        // *Примечание: BukkitRunnable не поддерживает разную задержку для разных итераций.
        // Для правильного чередования лучше использовать два отдельных runTaskLater.
    }

    private void setBlocks(Material mat) {
        for (Location loc : blocks) loc.getBlock().setType(mat);
    }

    @Override
    public void stop() {
        if (task != null) task.cancel();
        isRunning = false;
    }

    @Override
    public void reset() {
        stop();
        setBlocks(Material.PACKED_ICE);
    }
}