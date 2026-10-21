package org.lordalex.clackedicelcp;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.lordalex.clackedicelcp.Utils.ColorUtil;
import org.lordalex.clackedicelcp.Utils.ConfigMaze;
import java.util.*;

public class Game {
    private final ClackedIceLCP plugin;
    private final Map<String, Maze> mazes = new LinkedHashMap<>();
    private final List<String> orderedMazeIds = new ArrayList<>();
    private final Map<UUID, Long> ignoreUntil = new HashMap<>();
    private final Map<UUID, PlayerProgress> progressMap = new HashMap<>();
    private boolean showTime = false;
    private GameMode gameMode = GameMode.DEFAULT;

    private final Map<UUID, PlayerStats> statsMap = new HashMap<>();

    public PlayerStats getStats(UUID uuid) {
        return statsMap.computeIfAbsent(uuid, k -> new PlayerStats());
    }

    // Временный метод для теста (потом заменишь на реальные квесты)
    public String getCurrentQuestName() {
        return "Убить босса Зомби Мороза";
    }

    public Game(ClackedIceLCP plugin) { this.plugin = plugin; }

    public void loadAll(Map<String, ConfigMaze> configs, boolean showTime) {
        this.showTime = showTime;
        mazes.clear();
        orderedMazeIds.clear();
        for (Map.Entry<String, ConfigMaze> e : configs.entrySet()) {
            mazes.put(e.getKey(), new Maze(plugin, e.getKey(), e.getValue()));
            orderedMazeIds.add(e.getKey());
        }
    }

    public boolean isShowTime() { return showTime; }
    public void setShowTime(boolean b) { this.showTime = b; }
    public GameMode getGameMode() { return gameMode; }
    public void setGameMode(GameMode mode) { this.gameMode = mode; }

    public Collection<Maze> getMazes() { return mazes.values(); }
    public Maze getMazeById(String id) { return mazes.get(id); }
    public List<String> getOrderedMazeIds() { return orderedMazeIds; }
    public PlayerProgress getProgress(UUID uuid) { return progressMap.computeIfAbsent(uuid, k -> new PlayerProgress()); }
    public boolean isFirstMazeInChain(Maze maze) { return !orderedMazeIds.isEmpty() && orderedMazeIds.get(0).equals(maze.getId()); }

    public Maze getMazeByBlock(Block b) {
        for (Maze m : mazes.values()) if (m.isMazeBlock(b)) return m;
        return null;
    }

    public void setIgnore(UUID uuid, long until) { ignoreUntil.put(uuid, until); }
    public boolean isIgnored(UUID uuid) {
        Long until = ignoreUntil.get(uuid);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) { ignoreUntil.remove(uuid); return false; }
        return true;
    }

    // Полный сброс (восстанавливает лёд, стекло и красные блоки)
    public void resetAll() {
        for (Maze m : mazes.values()) {
            m.hardReset(); // <--- Теперь используем hardReset
        }
        progressMap.clear();
    }

    public void resetMaze(String id) {
        Maze m = mazes.get(id);
        if (m != null) {
            m.hardReset();
            progressMap.clear();
        }
    }

    public boolean onMazeFinished(Player p, Maze maze, long elapsed, long levelTotal) {
        UUID uuid = p.getUniqueId();
        PlayerProgress prog = getProgress(uuid);
        List<String> order = orderedMazeIds;

        if (gameMode == GameMode.SINGLE) {
            maze.teleportToStart(p);
            maze.softReset(); // Сбрасываем только лёд для повторного прохождения
            return true;
        }

        int expectedIndex = prog.getNextIndex();
        if (expectedIndex >= order.size()) { prog.reset(); expectedIndex = 0; }
        String expectedId = order.get(expectedIndex);

        if (gameMode == GameMode.DEFAULT && !maze.getId().equals(expectedId)) {
            prog.reset();
            p.sendMessage(ColorUtil.get("&7Порядок нарушен. Начни с level1"));
            return false;
        }

        prog.addTime(maze.getId(), elapsed);
        prog.addLevelTotalTime(maze.getId(), levelTotal);
        prog.setNextIndex(expectedIndex + 1);

        // Стекло ломается и в сюжете, и в тренировке
        if (gameMode == GameMode.DEFAULT || gameMode == GameMode.TRAINING) {
            maze.breakBarriers();
        }

        if (prog.getNextIndex() >= order.size()) {
            printSummary(p, prog);
            prog.reset();
            if (gameMode == GameMode.TRAINING) {
                Maze first = mazes.get(order.get(0));
                if (first != null) first.teleportToStart(p);
            }
            return true;
        }
        return true;
    }

    private void printSummary(Player p, PlayerProgress prog) {
        if (!showTime) return;
        StringBuilder sb = new StringBuilder("&6&l=== ИТОГ ===\n");
        for (String id : orderedMazeIds) {
            sb.append("&e").append(id).append("&7: &f")
                    .append(String.format("%.2f", prog.getTime(id) / 1000.0)).append(" сек. &7(&f")
                    .append(String.format("%.2f", prog.getLevelTotalTime(id) / 1000.0)).append("&7)\n");
        }
        long totalRealTime = System.currentTimeMillis() - prog.getChainStart();
        sb.append("&6&lСУММА: &e").append(String.format("%.2f", prog.getTotalTime() / 1000.0))
                .append(" сек. &7(&f").append(String.format("%.2f", totalRealTime / 1000.0)).append("&7)");
        p.sendMessage(ColorUtil.get(sb.toString()));
    }

    public static class BlockKey {
        public final int x, y, z;
        public BlockKey(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof BlockKey)) return false;
            BlockKey k = (BlockKey) o;
            return x == k.x && y == k.y && z == k.z;
        }
        @Override public int hashCode() { return (x * 31 + y) * 31 + z; }
    }
}