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

public class Game {

    private final ClackedIceLCP plugin;
    private GameState state;

    // Все блоки лабиринта в исходном состоянии
    private final Set<BlockKey> mazeBlocks = new HashSet<>();
    // Уже прожатые блоки
    private final Set<BlockKey> pressedBlocks = new HashSet<>();

    // Игроки в зоне мини-игры (UUID → PlayerInfo)
    private final Map<UUID, PlayerInfo> activePlayers = new HashMap<>();

    private Location startLocation;
    private World world;
    private ConfigMaze cfg;

    // таймер: тики (1 тик = 50 мс)
    private long startTime;
    private boolean timerRunning;

    public Game(ClackedIceLCP plugin, ConfigMaze cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.state = GameState.WAITING;
        loadMaze();
    }

    public void loadMaze() {
        // очистка перед повторной загрузкой
        mazeBlocks.clear();
        pressedBlocks.clear();
        activePlayers.clear();
        timerRunning = false;
        state = GameState.WAITING;

        this.world = Bukkit.getWorld(cfg.getWorld());
        if (world == null) {
            plugin.getLogger().severe("Мир не найден: " + cfg.getWorld());
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
                        mazeBlocks.add(new BlockKey(x, y, z));
                    }
                }
            }
        }

        this.startLocation = YmlParser.parseLocation(world, cfg.getStart());
        plugin.getLogger().info("Лабиринт загружен: " + mazeBlocks.size() + " блоков");
    }

    public boolean isMazeBlock(Block b) {
        return mazeBlocks.contains(new BlockKey(b.getX(), b.getY(), b.getZ()));
    }

    public boolean isPressed(Block b) {
        return pressedBlocks.contains(new BlockKey(b.getX(), b.getY(), b.getZ()));
    }

    // ===================== ЛОГИКА ШАГА =====================

    public void onPlayerStep(Player p, Block under) {
        PlayerInfo info = activePlayers.computeIfAbsent(p.getUniqueId(), k -> new PlayerInfo(p));

        // Если таймер еще не стартовал — старт
        if (!timerRunning) {
            startTimer();
        }

        BlockKey key = new BlockKey(under.getX(), under.getY(), under.getZ());

        if (pressedBlocks.contains(key)) {
            // наступил второй раз — фейл
            failAll(p);
            return;
        }

        pressedBlocks.add(key);
        under.setType(Material.ICE);

        // проверка финиша
        if (pressedBlocks.size() == mazeBlocks.size()) {
            finishAll();
        }
    }

    // ===================== ТАЙМЕР =====================

    private void startTimer() {
        this.startTime = System.currentTimeMillis();
        this.timerRunning = true;
        this.state = GameState.RUNNING;
        Bukkit.broadcastMessage(ColorUtil.get("&eМини-игра началась!"));
    }

    private long stopTimer() {
        if (!timerRunning) return 0;
        long elapsed = System.currentTimeMillis() - startTime;
        this.timerRunning = false;
        return elapsed;
    }

    // ===================== ФЕЙЛ / ФИНИШ =====================

    private void failAll(Player culprit) {
        long elapsed = stopTimer();
        state = GameState.RESETTING;

        // сообщение тому, кто ошибся
        culprit.sendMessage(ColorUtil.get(cfg.getFailMessage()));

        // телепорт всех активных игроков в начало
        for (PlayerInfo pi : new ArrayList<>(activePlayers.values())) {
            Player p = pi.getPlayer();
            if (p != null && p.isOnline()) {
                p.teleport(startLocation);
                if (!p.getUniqueId().equals(culprit.getUniqueId())) {
                    p.sendMessage(ColorUtil.get("&cИгрок &e" + culprit.getName() + " &cсломал лабиринт. Рестарт."));
                }
            }
        }

        resetMaze();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activePlayers.clear();
            state = GameState.WAITING;
        }, 5L);
    }

    private void finishAll() {
        long elapsed = stopTimer();
        state = GameState.WAITING;

        String time = String.format("%.2f", elapsed / 1000.0);
        String msg = cfg.getFinishMessage().replace("%time%", time);

        for (PlayerInfo pi : new ArrayList<>(activePlayers.values())) {
            Player p = pi.getPlayer();
            if (p != null && p.isOnline()) {
                p.sendMessage(ColorUtil.get(msg));
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activePlayers.clear();
            resetMaze();
        }, 60L); // через 3 сек — авто-сброс
    }

    // ===================== СБРОС =====================

    public void resetMaze() {
        for (BlockKey key : mazeBlocks) {
            Block b = world.getBlockAt(key.x, key.y, key.z);
            b.setType(Material.PACKED_ICE);
        }
        pressedBlocks.clear();
        timerRunning = false;
        state = GameState.WAITING;
    }

    // ===================== ГЕТТЕРЫ =====================

    public GameState getState() { return state; }
    public ConfigMaze getCfg() { return cfg; }
    public Map<UUID, PlayerInfo> getActivePlayers() { return activePlayers; }

    public int getMazeBlocksCount() {
        return mazeBlocks.size();
    }

    // ===================== КЛЮЧ БЛОКА =====================

    public static class BlockKey {
        final int x, y, z;
        public BlockKey(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof BlockKey)) return false;
            BlockKey k = (BlockKey) o;
            return x == k.x && y == k.y && z == k.z;
        }
        @Override public int hashCode() { return (x * 31 + y) * 31 + z; }
    }

    public void onIceStepByOther(Player other, Block under) {
        // Если игра не идёт — игнорируем
        if (state != GameState.RUNNING) return;

        // Если это тот же игрок, что и стартовал — не считаем (он мог переступить)
        // Но по твоей логике: он же наступил второй раз → тоже фейл.
        // Значит просто фейлим всегда.
        failAll(other);
    }


}