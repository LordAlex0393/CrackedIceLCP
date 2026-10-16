package org.lordalex.clackedicelcp;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
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

        // парсим конфиг как у тебя в TheBridge
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
            file = new File(getDataFolder(), "config.yml");
        }
        Config config = YmlParser.parseMapConfig(file);

        // создаём игру
        this.game = new Game(this, config.getMaze());

        // регистрируем события
        Bukkit.getPluginManager().registerEvents(new MoveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(this), this);

        // команда
        getCommand("icemaze").setExecutor(new IceCommand(this));

        // твой фиксированный день/погода
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            for (World world : Bukkit.getServer().getWorlds()) {
                world.setThundering(false);
                world.setStorm(false);
                world.setTime(3000);
            }
        }, 0L, 20L);

        getLogger().info("ClackedIceLCP включён. Блоков: " + game.getMazeBlocksCount());
    }

    @Override
    public void onDisable() {
        if (game != null) {
            game.resetMaze();
        }
        instance = null;
    }

    public static ClackedIceLCP getInstance() { return instance; }
    public Game getGame() { return game; }
}