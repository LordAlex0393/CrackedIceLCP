package org.lordalex.clackedicelcp.Snowmen;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.Utils.ConfigSnowman;
import org.lordalex.clackedicelcp.Utils.YmlParser;

public class SnowmanTrap {
    private final ClackedIceLCP plugin;
    private final Location location;
    private final int fireRateTicks;
    private final int range;
    private Snowman snowman;
    private BukkitRunnable task;

    public SnowmanTrap(ClackedIceLCP plugin, World world, ConfigSnowman cfg) {
        this.plugin = plugin;
        this.location = YmlParser.parseLocation(world, cfg.getLocation());
        this.fireRateTicks = Math.max(5, cfg.getFireRateTicks());
        this.range = cfg.getRange();
    }

    public void spawn() {
        // Защита от двойного спавна
        if (snowman != null && snowman.isValid()) {
            return;
        }

        snowman = (Snowman) location.getWorld().spawnEntity(location, EntityType.SNOWMAN);

        // Делаем его неуязвимым
        snowman.getEquipment().setHelmet(new ItemStack(Material.AIR));
        snowman.setFireTicks(0);

        startShooting();
    }

    private void startShooting() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (snowman == null || !snowman.isValid()) return;

                // Держим на месте
                snowman.setVelocity(new Vector(0, 0, 0));
                snowman.setFireTicks(0); // Страховка от таяния

                Player target = null;
                double closest = Double.MAX_VALUE;

                for (Player p : snowman.getWorld().getPlayers()) {
                    if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
                    if (p.getLocation().distanceSquared(snowman.getLocation()) <= range * range) {
                        double dist = p.getLocation().distanceSquared(snowman.getLocation());
                        if (dist < closest) {
                            closest = dist;
                            target = p;
                        }
                    }
                }

                if (target != null) {
                    Location loc = snowman.getLocation();
                    loc.setDirection(target.getLocation().subtract(loc).toVector());
                    snowman.teleport(loc);

                    org.bukkit.entity.Snowball snowball = snowman.launchProjectile(org.bukkit.entity.Snowball.class);
                    Vector dir = target.getLocation().add(0, 1, 0).toVector()
                            .subtract(snowman.getLocation().add(0, 1.5, 0).toVector()).normalize();
                    snowball.setVelocity(dir.multiply(1.5));
                }
            }
        };
        task.runTaskTimer(plugin, 20L, fireRateTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (snowman != null && snowman.isValid()) {
            snowman.remove();
            snowman = null;
        }
    }
}