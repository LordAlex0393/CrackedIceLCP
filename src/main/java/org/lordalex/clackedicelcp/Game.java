package org.lordalex.clackedicelcp;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.lordalex.clackedicelcp.Utils.ConfigMaze;

import java.util.*;

public class Game {

    private final ClackedIceLCP plugin;
    private final Map<String, Maze> mazes = new LinkedHashMap<>();
    private final List<String> orderedMazeIds = new ArrayList<>();
    private final Map<UUID, Long> ignoreUntil = new HashMap<>();
    private final Map<UUID, PlayerProgress> progressMap = new HashMap<>();
    private boolean showTime = false;

    public Game(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    public void loadAll(Map<String, ConfigMaze> configs, boolean showTime) {
        this.showTime = showTime;
        mazes.clear();
        orderedMazeIds.clear();

        for (Map.Entry<String, ConfigMaze> e : configs.entrySet()) {
            Maze m = new Maze(plugin, e.getKey(), e.getValue());
            mazes.put(e.getKey(), m);
            orderedMazeIds.add(e.getKey());
        }
        plugin.getLogger().info("Loaded mazes: " + mazes.size() + " | showTime: " + showTime);
    }

    public boolean isShowTime() { return showTime; }
    public Collection<Maze> getMazes() { return mazes.values(); }
    public Maze getMazeById(String id) { return mazes.get(id); }
    public List<String> getOrderedMazeIds() { return orderedMazeIds; }

    public Maze getMazeByBlock(Block b) {
        for (Maze m : mazes.values()) {
            if (m.isMazeBlock(b)) return m;
        }
        return null;
    }

    public boolean isFirstMazeInChain(Maze maze) {
        if (orderedMazeIds.isEmpty()) return false;
        return orderedMazeIds.get(0).equals(maze.getId());
    }

    public PlayerProgress getProgress(UUID uuid) {
        return progressMap.computeIfAbsent(uuid, k -> new PlayerProgress());
    }

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

    public void resetAll() {
        for (Maze m : mazes.values()) m.reset();
    }

    public boolean onMazeFinished(Player p, Maze maze, long elapsed, long levelTotal) {
        UUID uuid = p.getUniqueId();
        PlayerProgress prog = getProgress(uuid);

        List<String> order = orderedMazeIds;
        int expectedIndex = prog.getNextIndex();

        if (expectedIndex >= order.size()) {
            prog.reset();
            expectedIndex = 0;
        }

        String expectedId = order.get(expectedIndex);

        if (!maze.getId().equals(expectedId)) {
            prog.reset();
            prog.addTime(maze.getId(), elapsed);
            prog.addLevelTotalTime(maze.getId(), levelTotal);
            prog.setNextIndex(indexOf(maze.getId()) + 1);

            p.sendMessage(org.lordalex.clackedicelcp.Utils.ColorUtil.get(
                    "&7Порядок нарушен. Прогресс сброшен. Начни с &e" + order.get(0)));
            return false;
        }

        prog.addTime(maze.getId(), elapsed);
        prog.addLevelTotalTime(maze.getId(), levelTotal);
        prog.setNextIndex(expectedIndex + 1);

        if (prog.getNextIndex() >= order.size()) {
            printSummary(p, prog);
            prog.reset();
            return true;
        }

        return true;
    }

    private int indexOf(String id) {
        for (int i = 0; i < orderedMazeIds.size(); i++) {
            if (orderedMazeIds.get(i).equals(id)) return i;
        }
        return -1;
    }

    private void printSummary(Player p, PlayerProgress prog) {
        // Если флаг выключен — вообще ничего не пишем
        if (!showTime) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("&6&l=== ИТОГ ===\n");

        for (String id : orderedMazeIds) {
            long t = prog.getTime(id);
            long lt = prog.getLevelTotalTime(id);
            sb.append("&e").append(id).append("&7: &f")
                    .append(String.format("%.2f", t / 1000.0))
                    .append(" сек. &7(&f")
                    .append(String.format("%.2f", lt / 1000.0))
                    .append("&7)\n");
        }

        // Общее время от первого шага на первом уровне до последнего шага на последнем
        long chainStart = prog.getChainStart();
        long totalRealTime = System.currentTimeMillis() - chainStart;

        sb.append("&6&lСУММА: &e")
                .append(String.format("%.2f", prog.getTotalTime() / 1000.0))
                .append(" сек. &7(&f")
                .append(String.format("%.2f", totalRealTime / 1000.0))
                .append("&7)");

        p.sendMessage(org.lordalex.clackedicelcp.Utils.ColorUtil.get(sb.toString()));
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