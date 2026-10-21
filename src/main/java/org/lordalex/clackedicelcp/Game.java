package org.lordalex.clackedicelcp;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.lordalex.clackedicelcp.Utils.ConfigMaze;

import java.util.*;

public class Game {

    private final ClackedIceLCP plugin;
    private final Map<String, Maze> mazes = new LinkedHashMap<>();
    private final Map<UUID, Long> ignoreUntil = new HashMap<>();
    private final Map<UUID, PlayerProgress> progressMap = new HashMap<>();
    private final List<String> orderedMazeIds = new ArrayList<>();  // порядок уровней

    public Game(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    public void loadAll(Map<String, ConfigMaze> configs) {
        mazes.clear();
        orderedMazeIds.clear();

        for (Map.Entry<String, ConfigMaze> e : configs.entrySet()) {
            Maze m = new Maze(plugin, e.getKey(), e.getValue());
            mazes.put(e.getKey(), m);
            orderedMazeIds.add(e.getKey());   // порядок = порядок в YAML
        }
        plugin.getLogger().info("Loaded mazes: " + mazes.size() + " | order: " + orderedMazeIds);
    }

    public PlayerProgress getProgress(UUID uuid) {
        return progressMap.computeIfAbsent(uuid, k -> new PlayerProgress());
    }

    public List<String> getOrderedMazeIds() {
        return orderedMazeIds;
    }

    public Maze getMazeByBlock(Block b) {
        for (Maze m : mazes.values()) {
            if (m.isMazeBlock(b)) return m;
        }
        return null;
    }

    public Maze getMazeById(String id) {
        return mazes.get(id);
    }

    public Collection<Maze> getMazes() {
        return mazes.values();
    }

    // ===================== IGNORE =====================

    public void setIgnore(UUID uuid, long until) {
        ignoreUntil.put(uuid, until);
    }

    public boolean isIgnored(UUID uuid) {
        Long until = ignoreUntil.get(uuid);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            ignoreUntil.remove(uuid);
            return false;
        }
        return true;
    }

    // ===================== RESET ALL =====================

    public void resetAll() {
        for (Maze m : mazes.values()) {
            m.reset();
        }
    }

    // ===================== КЛЮЧ БЛОКА =====================

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

    /**
     * Вызывается, когда игрок успешно прошёл лабиринт.
     * Возвращает true, если это был "правильный" следующий уровень в цепочке.
     */
    public boolean onMazeFinished(Player p, Maze maze, long elapsed) {
        UUID uuid = p.getUniqueId();
        PlayerProgress prog = getProgress(uuid);

        List<String> order = orderedMazeIds;
        int expectedIndex = prog.getNextIndex();

        // если уровней меньше, чем ожидается — сбрасываем
        if (expectedIndex >= order.size()) {
            prog.reset();
            expectedIndex = 0;
        }

        String expectedId = order.get(expectedIndex);

        if (!maze.getId().equals(expectedId)) {
            // прошёл не тот уровень — сбрасываем прогресс, начинаем заново с этого
            prog.reset();
            prog.addTime(maze.getId(), elapsed);
            prog.setNextIndex(indexOf(maze.getId()) + 1);

            p.sendMessage(org.lordalex.clackedicelcp.Utils.ColorUtil.get(
                    "&7Порядок нарушен. Прогресс сброшен. Начни с &e" + order.get(0)));
            return false;
        }

        // правильный уровень
        prog.addTime(maze.getId(), elapsed);
        prog.setNextIndex(expectedIndex + 1);

        // если прошёл ВСЕ уровни по порядку — выводим суммарную статистику
        if (prog.getNextIndex() >= order.size()) {
            printSummary(p, prog);
            prog.reset();
            return true;
        }

        return true;
    }

    /**
     * Игрок ошибся в лабиринте — сбрасываем его прогресс по цепочке.
     */
    public void onMazeFailed(Player p) {
        PlayerProgress prog = getProgress(p.getUniqueId());
        prog.reset();
    }

    private int indexOf(String id) {
        for (int i = 0; i < orderedMazeIds.size(); i++) {
            if (orderedMazeIds.get(i).equals(id)) return i;
        }
        return -1;
    }

    private void printSummary(Player p, PlayerProgress prog) {
        StringBuilder sb = new StringBuilder();
        sb.append("&6&l=== ИТОГ ===\n");
        for (String id : orderedMazeIds) {
            long t = prog.getTime(id);
            sb.append("&e").append(id).append("&7: &f")
                    .append(String.format("%.2f", t / 1000.0)).append(" сек.\n");
        }
        sb.append("&a&lСУММА: &e")
                .append(String.format("%.2f", prog.getTotalTime() / 1000.0))
                .append(" сек.");

        p.sendMessage(org.lordalex.clackedicelcp.Utils.ColorUtil.get(sb.toString()));
    }
}