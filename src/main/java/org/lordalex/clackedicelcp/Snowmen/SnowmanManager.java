package org.lordalex.clackedicelcp.Snowmen;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.Utils.ConfigSnowman;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SnowmanManager {
    private final ClackedIceLCP plugin;
    private final List<SnowmanTrap> traps = new ArrayList<>();

    public SnowmanManager(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    public void loadAll(Map<String, ConfigSnowman> configs) {
        traps.clear();
        if (configs == null) return;

        for (Map.Entry<String, ConfigSnowman> entry : configs.entrySet()) {
            String id = entry.getKey();
            ConfigSnowman cfg = entry.getValue();
            World world = Bukkit.getWorld(cfg.getWorld());
            if (world == null) {
                plugin.getLogger().warning("World not found for snowman " + id);
                continue;
            }
            traps.add(new SnowmanTrap(plugin, world, cfg));
        }
        plugin.getLogger().info("Loaded snowmen: " + traps.size());
    }

    public void spawnAll() {
        for (SnowmanTrap trap : traps) trap.spawn();
    }

    public void removeAll() {
        for (SnowmanTrap trap : traps) trap.stop();
    }
}