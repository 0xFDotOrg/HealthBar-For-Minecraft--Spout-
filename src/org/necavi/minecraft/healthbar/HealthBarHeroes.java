package org.necavi.minecraft.healthbar;

import org.bukkit.plugin.Plugin;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
public class HealthBarHeroes {
    static CharacterManager characterManager;

    HealthBarHeroes(Plugin heroes) {
        characterManager = (CharacterManager) ((Heroes) heroes).getCharacterManager();
    }
}
