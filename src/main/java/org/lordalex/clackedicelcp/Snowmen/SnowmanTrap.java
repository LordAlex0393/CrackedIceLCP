package org.lordalex.clackedicelcp.Snowmen;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    private final double health;
    private Snowman snowman;
    private BukkitRunnable task;

    public SnowmanTrap(ClackedIceLCP plugin, World world, ConfigSnowman cfg) {
        this.plugin = plugin;
        this.location = YmlParser.parseLocation(world, cfg.getLocation());
        this.fireRateTicks = Math.max(5, cfg.getFireRateTicks());
        this.range = cfg.getRange();
        this.health = cfg.getHealth();
    }

    public void spawn() {
        if (snowman != null && snowman.isValid()) return;

        snowman = (Snowman) location.getWorld().spawnEntity(location, EntityType.SNOWMAN);

        snowman.setMaxHealth(health);
        snowman.setHealth(health);

        // Очищаем ВСЮ экипировку (тыкву и снежки в руке)
        snowman.getEquipment().clear();

        // Замедление навсегда
        snowman.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false));

        startShooting();
    }

    private void startShooting() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (snowman == null || !snowman.isValid()) return;

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
                    // Поворачиваем через телепортацию (в 1.8.8 это единственный способ)
                    Location loc = snowman.getLocation();
                    Vector direction = target.getLocation().toVector().subtract(loc.toVector());
                    loc.setDirection(direction);
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