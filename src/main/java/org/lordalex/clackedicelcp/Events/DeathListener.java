package org.lordalex.clackedicelcp.Events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.lordalex.clackedicelcp.ClackedIceLCP;
import org.lordalex.clackedicelcp.PlayerStats;
import org.lordalex.clackedicelcp.Utils.ColorUtil;

public class DeathListener implements Listener {
    private final ClackedIceLCP plugin;

    public DeathListener(ClackedIceLCP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        PlayerStats stats = plugin.getGame().getStats(p.getUniqueId());

        // Отнимаем жизнь
        stats.removeLife();

        // Очищаем дроп при смерти, чтобы вещи не валялись на арене
        e.getDrops().clear();
        e.setDroppedExp(0);

        // Респавн через 1 тик (чтобы сервер успел завершить смерть)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) {
                // Принудительный респавн
                p.spigot().respawn();

                // Телепорт на глобальный спавн
                Location spawn = plugin.getGame().getLevelSpawn();
                if (spawn != null) {
                    p.teleport(spawn);
                }

                // Обновляем табло
                plugin.getScoreboardManager().updateBoard(p);

                // Сообщение игроку
                p.sendMessage(ColorUtil.get("&cВы погибли! Осталось жизней: &e" + stats.getLives()));

                // Если жизни закончились (тут можно добавить логику проигрыша)
                if (stats.getLives() <= 0) {
                    p.sendMessage(ColorUtil.get("&4&lВы проиграли!"));
                    // p.kickPlayer("Вы проиграли!");
                }
            }
        }, 1L);
    }
}