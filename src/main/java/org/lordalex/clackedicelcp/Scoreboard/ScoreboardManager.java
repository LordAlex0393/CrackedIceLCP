package org.lordalex.clackedicelcp.Scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
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

    // Настройки (можно вынести в конфиг позже)
    private final String serverName = "@Lord_Alex";
    private final String patchVersion = "0.1";

    public ScoreboardManager(ClackedIceLCP plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    public void createBoard(Player p) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("game", "dummy");
        obj.setDisplayName(ColorUtil.get("&b&lБелый Холод"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        boards.put(p.getUniqueId(), board);
        p.setScoreboard(board);
    }

    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!boards.containsKey(p.getUniqueId())) {
                        createBoard(p);
                    }
                    updateBoard(p, boards.get(p.getUniqueId()));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Обновление раз в секунду
    }

    private void updateBoard(Player p, Scoreboard board) {
        Objective obj = board.getObjective(DisplaySlot.SIDEBAR);
        if (obj == null) return;

        // Очищаем старые строки (1.8.8 требует ручной очистки)
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        PlayerStats stats = plugin.getGame().getStats(p.getUniqueId());

        // Считаем прогресс (пример: 8 убийств из 10 для 80%)
        int progressPercent = Math.min(100, (stats.getKills() * 100) / 10);

        // Устанавливаем строки (снизу вверх)

        setLine(obj, 18,  "      ");
        setLine(obj, 17,  "&fУровень:");
        setLine(obj, 16,  "&f &6Лавопад &7 - (" + progressPercent + "%)");
        setLine(obj, 15,  "     ");
        setLine(obj, 14,  "&fЗадания:");
        setLine(obj, 13,  "&6 " + plugin.getGame().getCurrentQuestName());
        setLine(obj, 12,  "    ");
        setLine(obj, 11,  "&fЖизней: " + getHearts(stats.getLives()));
        setLine(obj, 10,  "   ");
        setLine(obj, 9,  "&fУбийств: &e" + stats.getKills());
        setLine(obj, 8,  "&fПодарки: &e" + stats.getGifts());
        setLine(obj, 7,  "&fСундуки: &e" + stats.getChests());
        setLine(obj, 6, "&fЗолото: &e" + stats.getGold());
        setLine(obj, 5,  "  ");
        setLine(obj, 4, "&fИграет: &e" + Bukkit.getOnlinePlayers().size() + "/6");
        setLine(obj, 3, "&fПатч: &7" + patchVersion);
        setLine(obj, 2,  " ");
        setLine(obj, 1, "&a&l" + serverName);
    }

    private void setLine(Objective obj, int score, String text) {
        // В 1.8.8 строка не может быть длиннее 32 символов,
        // а цветовые коды занимают место. Используем ChatColor.translateAlternateColorCodes.
        String translated = ChatColor.translateAlternateColorCodes('&', text);

        // Если строка длиннее 32 символов, обрезаем (защита от краша)
        if (translated.length() > 32) {
            translated = translated.substring(0, 32);
        }

        obj.getScore(translated).setScore(score);
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