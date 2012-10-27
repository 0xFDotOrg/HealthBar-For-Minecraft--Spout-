package org.necavi.minecraft.healthbar;

import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.player.SpoutPlayer;

public class HealthBarHealthListener implements Runnable {
    private static HealthBar plugin;

    public HealthBarHealthListener(HealthBar instance) {
        plugin = instance;

    }

    @Override
    public void run() {
        for (SpoutPlayer player : SpoutManager.getOnlinePlayers()) {
            if (!HealthBar.healthTracker.isEmpty() && HealthBar.healthTracker.containsKey(player)) {
				if (HealthBar.healthTracker.get(player) == (HealthBar.useHeroes ? HealthBarHeroes.characterManager.getHero(player).getHealth() : player.getHealth())) {
					continue;
            	}
			}
            plugin.setTitle(player);
        }
    }
}
