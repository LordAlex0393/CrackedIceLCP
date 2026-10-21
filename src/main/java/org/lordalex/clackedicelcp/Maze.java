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

    private final List<Location> glassLocations = new ArrayList<>();
    private final List<Location> redstoneLocations = new ArrayList<>();

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
        glassLocations.clear();
        redstoneLocations.clear();
        timerRunning = false;
        state = GameState.WAITING;

        this.world = Bukkit.getWorld(cfg.getWorld());
        if (world == null) return;

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

        if (cfg.getGlassBlocks() != null) {
            for (String s : cfg.getGlassBlocks()) glassLocations.add(YmlParser.parseLocation(world, s));
        }
        if (cfg.getRedstoneBlocks() != null) {
            for (String s : cfg.getRedstoneBlocks()) redstoneLocations.add(YmlParser.parseLocation(world, s));
        }

        this.startLocation = YmlParser.parseLocation(world, cfg.getStart());
    }

    public boolean isMazeBlock(Block b) { return mazeBlocks.contains(new Game.BlockKey(b.getX(), b.getY(), b.getZ())); }
    public boolean isPressed(Block b) { return pressedBlocks.contains(new Game.BlockKey(b.getX(), b.getY(), b.getZ())); }

    public void onPlayerStep(Player p, Block under) {
        if (state == GameState.RESETTING) return;
        if (plugin.getGame().isIgnored(p.getUniqueId())) return;

        // В сюжетном моде, если лабиринт уже пройден, мы не запускаем его снова
        if (plugin.getGame().getGameMode() == GameMode.DEFAULT && state == GameState.WAITING) {
            // Но нам нужно понять, пройден ли он уже этим игроком
            if (plugin.getGame().getProgress(p.getUniqueId()).getTime(id) > 0) return;
        }

        activePlayers.computeIfAbsent(p.getUniqueId(), k -> new PlayerInfo(p));
        PlayerProgress prog = plugin.getGame().getProgress(p.getUniqueId());

        if (plugin.getGame().isFirstMazeInChain(this)) prog.startChainIfNeeded(System.currentTimeMillis());
        prog.startAttemptIfNeeded(id, System.currentTimeMillis());

        if (!timerRunning) startTimer(p);

        Game.BlockKey key = new Game.BlockKey(under.getX(), under.getY(), under.getZ());
        if (pressedBlocks.contains(key)) {
            failAll(p);
            return;
        }

        pressedBlocks.add(key);
        under.setType(Material.ICE);

        if (pressedBlocks.size() == mazeBlocks.size()) finishAll();
    }

    public void onIceStepByOther(Player other, Block under) {
        if (state != GameState.RUNNING) return;
        if (plugin.getGame().isIgnored(other.getUniqueId())) return;
        failAll(other);
    }

    private void startTimer(Player p) {
        if (state == GameState.RESETTING || timerRunning) return;
        this.startTime = System.currentTimeMillis();
        this.timerRunning = true;
        this.state = GameState.RUNNING;
        if (plugin.getGame().isShowTime()) p.sendMessage(ColorUtil.get("&eУровень " + id + " начался!"));
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

        long ignoreTime = System.currentTimeMillis() + 100;
        int mazeY = (int) startLocation.getY();

        for (PlayerInfo pi : new ArrayList<>(activePlayers.values())) {
            Player p = pi.getPlayer();
            if (p == null || !p.isOnline()) continue;
            if (Math.abs(p.getLocation().getBlockY() - mazeY) > 1) continue;
            p.teleport(startLocation);
            plugin.getGame().setIgnore(p.getUniqueId(), ignoreTime);
            if (!p.getUniqueId().equals(culprit.getUniqueId())) {
                p.sendMessage(ColorUtil.get("&cИгрок &e" + culprit.getName() + " &cсломал уровень."));
            }
        }

        softReset(); // При фейле сбрасываем лёд
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
                p.sendMessage(ColorUtil.get(cfg.getFinishMessage() + " &7(&e" + String.format("%.2f", elapsed / 1000.0) + "&7 сек.)"));
            } else {
                p.sendMessage(ColorUtil.get(cfg.getFinishMessage()));
            }
            plugin.getGame().onMazeFinished(p, this, elapsed, levelTotal);
            plugin.getScoreboardManager().updateBoard(p);
        }

        // В сюжетном моде ЛЁД НЕ РЕСЕТИТСЯ, поэтому мы просто чистим список активных
        if (plugin.getGame().getGameMode() == GameMode.DEFAULT) {
            activePlayers.clear();
        } else {
            // В остальных режимах лёд сбрасывается через 8 тиков
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                softReset();
                activePlayers.clear();
            }, 8L);
        }
    }

    // Сбрасывает ТОЛЬКО лёд (при фейле или повторном прохождении)
    public void softReset() {
        if (world == null) return;
        for (Game.BlockKey key : mazeBlocks) {
            world.getBlockAt(key.x, key.y, key.z).setType(Material.PACKED_ICE);
        }
        pressedBlocks.clear();
        timerRunning = false;
        state = GameState.WAITING;
    }

    // Сбрасывает ВСЁ (лёд, стекло, красные блоки) - только по команде
    public void hardReset() {
        softReset();

        for (Location loc : glassLocations){
            loc.getBlock().setType(Material.STAINED_GLASS);
            loc.getBlock().setData((byte) 14);
        }
        for (Location loc : redstoneLocations){
            loc.getBlock().setType(Material.STAINED_CLAY); // Ярко-красный
            loc.getBlock().setData((byte) 14);
        }
    }

    public void breakBarriers() {
        for (Location loc : glassLocations) loc.getBlock().setType(Material.AIR);
        for (Location loc : redstoneLocations){
            loc.getBlock().setType(Material.STAINED_CLAY); // Ярко-зеленый
            loc.getBlock().setData((byte) 13);
        }
    }

    public void teleportToStart(Player p) {
        if (startLocation != null) {
            p.teleport(startLocation);
            plugin.getGame().setIgnore(p.getUniqueId(), System.currentTimeMillis() + 100);
        }
    }

    public String getId() { return id; }
    public GameState getState() { return state; }
    public ConfigMaze getCfg() { return cfg; }
    public int getMazeBlocksCount() { return mazeBlocks.size(); }
    public Map<UUID, PlayerInfo> getActivePlayers() { return activePlayers; }
}