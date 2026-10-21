package org.lordalex.clackedicelcp.Snowmen;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Snowman;

public class SnowmanListener implements Listener {

    @EventHandler
    public void onSnowmanDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Snowman) {
            // Полностью очищаем дроп (снежки и снег)
            event.getDrops().clear();
            // Опционально: убираем опыт (если он выпадает)
            event.setDroppedExp(0);
        }
    }
}