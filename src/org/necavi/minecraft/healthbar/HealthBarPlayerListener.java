package org.necavi.minecraft.healthbar;

import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.player.SpoutPlayer;

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
    public void onSneakToggle(PlayerToggleSneakEvent event) {
    	SpoutPlayer pl = SpoutManager.getPlayer(event.getPlayer());
    	if(event.isSneaking()) {
        	String title = null;
	        if((title = pl.getTitle().split("§e§c§e")[0]) != null) {
		        if (HealthBar.usePermissions) {
		            for (SpoutPlayer player : SpoutManager.getOnlinePlayers()) {
		                if (player.hasPermission("healthbar.cansee")) {
		                    player.setTitleFor(pl, title);
		                }
		            }
		        } else {
		        	pl.setTitle(title);
	            }
        	}
    	} else {
    		plugin.setTitle(pl);
    	}
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.setTitle(SpoutManager.getPlayer(event.getPlayer()));
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
