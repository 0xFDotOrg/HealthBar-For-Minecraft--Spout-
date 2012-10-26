package org.necavi.minecraft.healthbar;

import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;

public class HealthBarPlayerListener implements Listener {
    private static HealthBar plugin;

    public HealthBarPlayerListener(HealthBar instance) {
        plugin = instance;

    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpoutcraftEnabled(SpoutCraftEnableEvent event) {
        plugin.setTitle(event.getPlayer());
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
