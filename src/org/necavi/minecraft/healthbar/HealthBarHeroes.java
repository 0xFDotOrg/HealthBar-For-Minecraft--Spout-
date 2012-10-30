package org.necavi.minecraft.healthbar;

import org.bukkit.plugin.Plugin;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
public class HealthBarHeroes {
	protected static CharacterManager characterManager;

    HealthBarHeroes(Plugin heroes, HealthBar plugin) {
        characterManager = (CharacterManager) ((Heroes) heroes).getCharacterManager();
        plugin.logger.info("[HealthBar] Found heroes, using their health and mana systems.");
        plugin.getServer().getScheduler()
        	.scheduleSyncRepeatingTask(plugin, new HealthBarManaListener(plugin), 0, 1);
    }
}
