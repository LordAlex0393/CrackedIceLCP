package org.lordalex.clackedicelcp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.lordalex.clackedicelcp.Utils.ColorUtil;
import org.lordalex.clackedicelcp.Utils.ConfigMaze;
import org.lordalex.clackedicelcp.Utils.YmlParser;

import java.util.*;

public class Maze {

    private final ClackedIceLCP plugin;
    private final String id;
    private final ConfigMaze cfg;

    private final Set<Game.BlockKey> mazeBlocks = new HashSet<>();
    private final Set<Game.BlockKey> pressedBlocks = new HashSet<>();
    private final Map<UUID, PlayerInfo> activePlayers = new HashMap<>();

    private World world;
    private Location startLocation;

    private long startTime;
    private boolean timerRunning;
    private GameState state = GameState.WAITING;

    public Maze(ClackedIceLCP plugin, String id, ConfigMaze cfg) {
        this.plugin = plugin;
        this.id = id;
        this.cfg = cfg;
        load();
    }

    public void load() {
        mazeBlocks.clear();
        pressedBlocks.clear();
        activePlayers.clear();
        timerRunning = false;
        state = GameState.WAITING;

        this.world = Bukkit.getWorld(cfg.getWorld());
        if (world == null) {
            plugin.getLogger().severe("[Maze " + id + "] World not found: " + cfg.getWorld());
            return;
        }

        Location c1 = YmlParser.parseLocation(world, cfg.getCorner1());
        Location c2 = YmlParser.parseLocation(world, cfg.getCorner2());
        Location c3 = YmlParser.parseLocation(world, cfg.getCorner3());
        Location c4 = YmlParser.parseLocation(world, cfg.getCorner4());

        int minX = (int) Math.min(Math.min(c1.getX(), c2.getX()), Math.min(c3.getX(), c4.getX()));
        int maxX = (int) Math.max(Math.max(c1.getX(), c2.getX()), Math.max(c3.getX(), c4.getX()));
        int minY = (int) Math.min(Math.min(c1.getY(), c2.getY()), Math.min(c3.getY(), c4.getY()));
        int minZ = (int) Math.min(Math.min(c1.getZ(), c2.getZ()), Math.min(c3.getZ(), c4.getZ()));
        int maxZ = (int) Math.max(Math.max(c1.getZ(), c2.getZ()), Math.max(c3.getZ(), c4.getZ()));

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y < minY + cfg.getHeight(); y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() == Material.PACKED_ICE || b.getType() == Material.ICE) {
                        b.setType(Material.PACKED_ICE);
                        mazeBlocks.add(new Game.BlockKey(x, y, z));
                    }
                }
            }
        }

        this.startLocation = YmlParser.parseLocation(world, cfg.getStart());
        plugin.getLogger().info("[Maze " + id + "] Loaded: " + mazeBlocks.size() + " blocks");
    }

    public boolean isMazeBlock(Block b) {
        return mazeBlocks.contains(new Game.BlockKey(b.getX(), b.getY(), b.getZ()));
    }

    public boolean isPressed(Block b) {
        return pressedBlocks.contains(new Game.BlockKey(b.getX(), b.getY(), b.getZ()));
    }

    public void onPlayerStep(Player p, Block under) {
        if (state == GameState.RESETTING) return;
        if (plugin.getGame().isIgnored(p.getUniqueId())) return;

        activePlayers.computeIfAbsent(p.getUniqueId(), k -> new PlayerInfo(p));

        PlayerProgress prog = plugin.getGame().getProgress(p.getUniqueId());

        if (plugin.getGame().isFirstMazeInChain(this)) {
            prog.startChainIfNeeded(System.currentTimeMillis());
        }

        prog.startAttemptIfNeeded(id, System.currentTimeMillis());

        if (!timerRunning) {
            startTimer(p, prog);
        }

        Game.BlockKey key = new Game.BlockKey(under.getX(), under.getY(), under.getZ());

        if (pressedBlocks.contains(key)) {
            failAll(p);
            return;
        }

        pressedBlocks.add(key);
        under.setType(Material.ICE);

        if (pressedBlocks.size() == mazeBlocks.size()) {
            finishAll();
        }
    }

    public void onIceStepByOther(Player other, Block under) {
        if (state != GameState.RUNNING) return;
        if (plugin.getGame().isIgnored(other.getUniqueId())) return;
        failAll(other);
    }

    private void startTimer(Player p, PlayerProgress prog) {
        if (state == GameState.RESETTING) return;
        if (timerRunning) return;

        this.startTime = System.currentTimeMillis();
        this.timerRunning = true;
        this.state = GameState.RUNNING;

        if (plugin.getGame().isShowTime()) {
            p.sendMessage(ColorUtil.get("&eУровень " + id + " начался!"));
        }
    }

    private long stopTimer() {
        if (!timerRunning) return 0;
        long elapsed = System.currentTimeMillis() - startTime;
        this.timerRunning = false;
        return elapsed;
    }

    public void failAll(Player culprit) {
        stopTimer();
        state = GameState.RESETTING;

        culprit.sendMessage(ColorUtil.get(cfg.getFailMessage()));

        // Изменено на +100, чтобы рестарт проходил быстрее
        long ignoreTime = System.currentTimeMillis() + 100;
        int mazeY = (int) startLocation.getY();

        for (PlayerInfo pi : new ArrayList<>(activePlayers.values())) {
            Player p = pi.getPlayer();
            if (p == null || !p.isOnline()) continue;

            if (Math.abs(p.getLocation().getBlockY() - mazeY) > 1) continue;

            p.teleport(startLocation);
            plugin.getGame().setIgnore(p.getUniqueId(), ignoreTime);

            if (!p.getUniqueId().equals(culprit.getUniqueId())) {
                p.sendMessage(ColorUtil.get("&cИгрок &e" + culprit.getName()
                        + " &cсломал уровень " + id + ". Рестарт."));
            }
        }

        reset();
        activePlayers.clear();
        state = GameState.WAITING;
    }

    private void finishAll() {
        long now = System.currentTimeMillis();
        long elapsed = stopTimer();
        state = GameState.WAITING;

        for (PlayerInfo pi : new ArrayList<>(activePlayers.values())) {
            Player p = pi.getPlayer();
            if (p == null || !p.isOnline()) continue;

            PlayerProgress prog = plugin.getGame().getProgress(p.getUniqueId());
            long levelTotal = now - prog.getAttemptStart(id);

            if (plugin.getGame().isShowTime()) {
                String timeStr = String.format("%.2f", elapsed / 1000.0);
                p.sendMessage(ColorUtil.get(cfg.getFinishMessage() + " &7(&e" + timeStr + "&7 сек.)"));
            } else {
                p.sendMessage(ColorUtil.get(cfg.getFinishMessage()));
            }

            plugin.getGame().onMazeFinished(p, this, elapsed, levelTotal);
        }

        // Изменено на 8L тиков (0.4 сек), чтобы лабиринт быстрее восстанавливался
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            reset();
            activePlayers.clear();
        }, 8L);
    }
    public void reset() {
        if (world == null) return;
        for (Game.BlockKey key : mazeBlocks) {
            Block b = world.getBlockAt(key.x, key.y, key.z);
            b.setType(Material.PACKED_ICE);
        }
        pressedBlocks.clear();
        timerRunning = false;
        state = GameState.WAITING;
    }

    public String getId() { return id; }
    public GameState getState() { return state; }
    public ConfigMaze getCfg() { return cfg; }
    public Set<Game.BlockKey> getMazeBlocks() { return mazeBlocks; }
    public Map<UUID, PlayerInfo> getActivePlayers() { return activePlayers; }
    public int getMazeBlocksCount() { return mazeBlocks.size(); }
    public Location getStartLocation() { return startLocation; }
}