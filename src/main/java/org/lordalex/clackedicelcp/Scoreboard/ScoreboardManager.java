package org.lordalex.clackedicelcp.Scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.PlayerStats;
import org.lordalex.clackedicelcp.Utils.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    private final ClackedIceLCP plugin;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardManager(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    public void createBoard(Player p) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("game", "dummy");

        String title = plugin.getConfig().getString("scoreboard.title", "&b&lБелый Холод");
        obj.setDisplayName(ColorUtil.get(title));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        boards.put(p.getUniqueId(), board);
        p.setScoreboard(board);
        updateBoard(p);
    }

    public void updateBoard(Player p) {
        Scoreboard board = boards.get(p.getUniqueId());
        if (board == null) return;
        Objective obj = board.getObjective(DisplaySlot.SIDEBAR);
        if (obj == null) return;

        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        PlayerStats stats = plugin.getGame().getStats(p.getUniqueId());
        int progressPercent = Math.min(100, (stats.getKills() * 100) / 10);

        String serverName = plugin.getConfig().getString("scoreboard.server_name", "VimeWorld");
        String patchVersion = plugin.getConfig().getString("scoreboard.patch", "1.3");
        int maxPlayers = plugin.getConfig().getInt("scoreboard.max_players", 6);
        String levelName = plugin.getConfig().getString("scoreboard.level_name", "Лавопад");

        // Получаем статус льда (сколько уровней пройдено из 3)
        int icePassed = plugin.getGame().getProgress(p.getUniqueId()).getNextIndex();
        int totalIce = plugin.getGame().getOrderedMazeIds().size();

        // Статус ивента
        String eventStatus = plugin.getGame().isEventRunning() ? "&aЗапущен" : "&cОстановлен";

        // --- ОТРИСОВКА (Строго по твоей схеме) ---
        setLine(obj, 14,  " ");
        setLine(obj, 13,  "&fУровень: &6" + levelName + " &7(" + progressPercent + "%)");
        setLine(obj, 12,  "  ");

        setLine(obj, 11,  "&fЗадания:");
        // Показываем прогресс льда вместо квестов
        setLine(obj, 10,  "&e Колотый лед: &6" + icePassed + "/" + totalIce);
        setLine(obj, 9,   "&e Ивент: " + eventStatus);
        setLine(obj, 8,   "   ");

        setLine(obj, 7,   "&fУбийств: &e" + stats.getKills());
        setLine(obj, 6,   "&fПодарки: &e" + stats.getGifts());
        setLine(obj, 5,   "&fСундуки: &e" + stats.getChests());
        setLine(obj, 4,   "&fЗолото: &e" + stats.getGold());
        setLine(obj, 3,   "&fЖизней: " + getHearts(stats.getLives()));

        setLine(obj, 2,   "    ");
        setLine(obj, 1,   "&fИграет: &e" + Bukkit.getOnlinePlayers().size() + "/" + maxPlayers);

        // В 1.8.8 максимум 15 строк. Поэтому сервер и патч объединяем в одну строку.
        // Если хочешь их строго раздельно, освободи место (убери пустые строки).
        setLine(obj, 0,   "&a&l" + serverName + " &7(Патч: " + patchVersion + ")");
    }

    private void setLine(Objective obj, int score, String text) {
        String translated = ChatColor.translateAlternateColorCodes('&', text);

        // В 1.8.8 цвета добавляют невидимые символы, чтобы строки не склеивались
        // Например " " и "  " склеятся, а "§0 " и "§1  " — нет.
        String uniqueText = translated + ChatColor.values()[score % 16].toString() + ChatColor.RESET;

        if (uniqueText.length() > 32) {
            uniqueText = uniqueText.substring(0, 32);
        }
        obj.getScore(uniqueText).setScore(score);
    }

    private String getHearts(int lives) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i < lives) sb.append("&c❤");
            else sb.append("&8❤");
        }
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public void removeBoard(Player p) {
        boards.remove(p.getUniqueId());
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}