package me.plornt.healthbar;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class HealthBarPlayerListener implements Listener {
    public static HealthBar plugin;

    public HealthBarPlayerListener(HealthBar instance) {
        plugin = instance;

    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.setTitle(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.setTitle(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!HealthBar.healthTracker.isEmpty()) {
            if (HealthBar.healthTracker.containsKey(event.getPlayer())) {
                HealthBar.healthTracker.remove(event.getPlayer());
            }
        }
    }
}
