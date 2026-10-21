package org.lordalex.clackedicelcp.Platforms;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.Utils.ConfigPlatform;
import org.lordalex.clackedicelcp.Utils.YmlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlatformManager {
    private final ClackedIceLCP plugin;
    private final List<Platform> platforms = new ArrayList<>();

    public PlatformManager(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    public void loadAll(Map<String, ConfigPlatform> configs) {
        platforms.clear();
        for (Map.Entry<String, ConfigPlatform> entry : configs.entrySet()) {
            String id = entry.getKey();
            ConfigPlatform cfg = entry.getValue();
            World world = Bukkit.getWorld(cfg.getWorld());
            if (world == null) {
                plugin.getLogger().warning("World not found for platform " + id);
                continue;
            }

            if ("moving".equalsIgnoreCase(cfg.getType())) {
                platforms.add(new MovingPlatform(plugin, world, cfg));
            } else if ("blinking".equalsIgnoreCase(cfg.getType())) {
                platforms.add(new BlinkingPlatform(plugin, world, cfg));
            }
        }
        plugin.getLogger().info("Loaded platforms: " + platforms.size());
    }

    public void startAll() { for (Platform p : platforms) p.start(); }
    public void stopAll() { for (Platform p : platforms) p.stop(); }
    public void resetAll() { for (Platform p : platforms) p.reset(); }
}