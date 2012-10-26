package org.necavi.minecraft.healthbar;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class HealthBarPluginListener implements Listener {
    private HealthBar plugin;

    public HealthBarPluginListener(HealthBar plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("Heroes")) {
            new HealthBarHeroes(event.getPlugin(), plugin);
            HealthBar.useHeroes = true;
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginDisableEvent event) {
    	if(event.getPlugin().getName().equals("Heroes")) {
            this.plugin.logger.info("[HealthBar] Heroes unloaded - reverting to vanilla health system.");
    		HealthBar.useHeroes = false;
    	}
    }
}
