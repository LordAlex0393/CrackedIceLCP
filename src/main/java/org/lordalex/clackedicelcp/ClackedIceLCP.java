package org.lordalex.clackedicelcp;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.lordalex.clackedicelcp.Commands.IceCommand;
import org.lordalex.clackedicelcp.Events.MoveListener;
import org.lordalex.clackedicelcp.Events.QuitListener;
import org.lordalex.clackedicelcp.Utils.Config;
import org.lordalex.clackedicelcp.Utils.YmlParser;

import java.io.File;

public final class ClackedIceLCP extends JavaPlugin {
    private static ClackedIceLCP instance;
    private Game game;

    @Override
    public void onEnable() {
        instance = this;

        try {
            File file = new File(getDataFolder(), "config.yml");
            if (!getDataFolder().exists()) getDataFolder().mkdirs();
            if (!file.exists()) saveResource("config.yml", false);

            Config config = YmlParser.parseMapConfig(file);
            if (config == null || config.getMazes() == null) {
                getLogger().severe("Config or 'mazes' section is null!");
                return;
            }

            this.game = new Game(this);
            game.loadAll(config.getMazes());

            Bukkit.getPluginManager().registerEvents(new MoveListener(this), this);
            Bukkit.getPluginManager().registerEvents(new QuitListener(this), this);

            if (getCommand("icemaze") != null) {
                getCommand("icemaze").setExecutor(new IceCommand(this));
            } else {
                getLogger().severe("Command 'icemaze' not found in plugin.yml!");
            }

            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncRepeatingTask(this, () -> {
                for (World world : Bukkit.getServer().getWorlds()) {
                    world.setThundering(false);
                    world.setStorm(false);
                    world.setTime(3000);
                }
            }, 0L, 20L);

            getLogger().info("ClackedIceLCP enabled. Mazes: " + game.getMazes().size());
        } catch (Exception ex) {
            getLogger().severe("Enable error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (game != null) game.resetAll();
        instance = null;
    }

    public static ClackedIceLCP getInstance() { return instance; }
    public Game getGame() { return game; }
}