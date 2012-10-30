package org.necavi.minecraft.healthbar;

import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.player.SpoutPlayer;

public class HealthBarManaListener implements Runnable {
    private static HealthBar plugin;

    public HealthBarManaListener(HealthBar instance) {
        plugin = instance;
    }

    @Override
    public void run() {
        for (SpoutPlayer player : SpoutManager.getOnlinePlayers()) {
            if (!HealthBar.manaTracker.isEmpty() && HealthBar.manaTracker.containsKey(player)) {
				if (HealthBar.manaTracker.get(player) == HealthBarHeroes.characterManager.getHero(player).getMana()) {
					continue;
            	}
			}
            plugin.setTitle(player);
        }
    }
}
