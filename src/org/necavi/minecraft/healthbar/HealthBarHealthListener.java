package org.necavi.minecraft.healthbar;

import org.bukkit.entity.Player;

public class HealthBarHealthListener implements Runnable {
    public static HealthBar plugin;

    public HealthBarHealthListener(HealthBar instance) {
        plugin = instance;

    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!HealthBar.healthTracker.isEmpty() && HealthBar.healthTracker.containsKey(player)) {
				if (HealthBar.healthTracker.get(player) == (HealthBar.useHeroes ? HealthBarHeroes.characterManager.getHero(player).getHealth() : player.getHealth())) {
					continue;
            	}
			}
			plugin.setTitle(player);
        }
    }
}
