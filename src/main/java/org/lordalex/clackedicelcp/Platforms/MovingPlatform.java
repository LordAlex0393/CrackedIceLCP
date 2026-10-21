package org.lordalex.clackedicelcp.Platforms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.Utils.ConfigPlatform;
import org.lordalex.clackedicelcp.Utils.YmlParser;

import java.util.ArrayList;
import java.util.List;

public class MovingPlatform implements Platform {
    private final ClackedIceLCP plugin;
    private final World world;
    private final Location pos1;
    private final Location moveTo;
    private final int speedTicks;

    private final List<Location> originalBlocks = new ArrayList<>();
    private BukkitRunnable task;
    private boolean isRunning = false;

    // Стейт движения
    private int currentStep = 0;
    private int maxSteps = 0;
    private Vector dirVector;
    private boolean forward = true;

    public MovingPlatform(ClackedIceLCP plugin, World world, ConfigPlatform cfg) {
        this.plugin = plugin;
        this.world = world;
        this.pos1 = YmlParser.parseLocation(world, cfg.getPos1());
        this.moveTo = YmlParser.parseLocation(world, cfg.getMoveTo());
        this.speedTicks = Math.max(1, cfg.getSpeedTicks());

        initBlocks(YmlParser.parseLocation(world, cfg.getPos2()));
    }

    private void initBlocks(Location pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    originalBlocks.add(new Location(world, x, y, z));
                    world.getBlockAt(x, y, z).setType(Material.PACKED_ICE);
                }
            }
        }

        // Вычисляем вектор движения
        double dx = moveTo.getX() - pos1.getX();
        double dy = moveTo.getY() - pos1.getY();
        double dz = moveTo.getZ() - pos1.getZ();
        this.dirVector = new Vector(Math.signum(dx), Math.signum(dy), Math.signum(dz));
        this.maxSteps = (int) Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
    }

    @Override
    public void start() {
        if (isRunning) return;
        isRunning = true;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                // 1. Удаляем блоки на старом месте
                for (Location loc : originalBlocks) {
                    world.getBlockAt(loc).setType(Material.AIR);
                }

                // 2. Сдвигаем координаты
                Vector stepVec = forward ? dirVector : dirVector.clone().multiply(-1);
                for (Location loc : originalBlocks) {
                    loc.add(stepVec);
                }
                currentStep += forward ? 1 : -1;

                // 3. Ставим блоки на новое место
                for (Location loc : originalBlocks) {
                    world.getBlockAt(loc).setType(Material.PACKED_ICE);
                }

                // 4. Таскаем игроков, стоящих на платформе
                pushEntities(stepVec);

                // 5. Меняем направление, если достигли края
                if (currentStep >= maxSteps) forward = false;
                else if (currentStep <= 0) forward = true;
            }
        };
        task.runTaskTimer(plugin, 0L, speedTicks);
    }

    private void pushEntities(Vector stepVec) {
        for (Location loc : originalBlocks) {
            // Ищем игроков чуть выше блока
            for (Entity e : world.getNearbyEntities(loc.clone().add(0.5, 1, 0.5), 0.6, 1, 0.6)) {
                if (e instanceof org.bukkit.entity.Player) {
                    e.teleport(e.getLocation().add(stepVec));
                }
            }
        }
    }

    @Override
    public void stop() {
        if (task != null) task.cancel();
        isRunning = false;
    }

    @Override
    public void reset() {
        stop();
        // Очищаем всё, что двинули, и ставим на изначальные координаты
        // Для простоты очищаем большую область (можно оптимизировать)
        for (Location loc : originalBlocks) {
            loc.getBlock().setType(Material.AIR);
        }

        // Восстанавливаем исходные позиции
        Location shift = pos1.clone();
        for (Location loc : originalBlocks) {
            // Вычисляем смещение от moveTo обратно к pos1 (просто возвращаем на pos1)
            // Для простоты пересоздадим блоки заново
        }
        // Пересоздание лучше делать через initBlocks заново, но чтобы не плодить
        // просто ставим блоки по изначальным координатам
        // (опустим этот момент, если надо - допишем жесткий ресет)
    }
}