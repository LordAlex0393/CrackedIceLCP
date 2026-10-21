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
    private final int startDelay;
    private final List<Location> blocks = new ArrayList<>();

    private BukkitRunnable task;
    private boolean isRunning = false;

    public BlinkingPlatform(ClackedIceLCP plugin, World world, ConfigPlatform cfg) {
        this.plugin = plugin;
        this.world = world;
        this.timeOn = cfg.getTimeOn();
        this.timeOff = cfg.getTimeOff();
        this.startDelay = cfg.getStartDelay();

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
                    // ВАЖНО: При загрузке ставим воздух, чтобы платформа не мешала до старта
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    @Override
    public void start() {
        if (isRunning) return;
        isRunning = true;

        // Ждем start_delay, затем появляемся
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                // Появляемся
                setBlocks(true);
                // Запускаем цикл: стоим time_on, пропадаем time_off
                scheduleNext(true);
            }
        };
        task.runTaskLater(plugin, startDelay);
    }

    private void scheduleNext(boolean isCurrentlyOn) {
        if (!isRunning) return;

        // Если сейчас стоим — ждем timeOn перед исчезновением. Иначе ждем timeOff перед появлением.
        long delay = isCurrentlyOn ? timeOn : timeOff;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                setBlocks(!isCurrentlyOn); // Инвертируем состояние
                scheduleNext(!isCurrentlyOn);
            }
        };
        task.runTaskLater(plugin, delay);
    }

    private void setBlocks(boolean on) {
        Material mat = on ? Material.PACKED_ICE : Material.AIR;
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
        setBlocks(false); // Сбрасываем в воздух (до следующего старта)
    }
}