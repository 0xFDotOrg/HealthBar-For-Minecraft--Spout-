package org.necavi.minecraft.healthbar;

import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.herocraftonline.heroes.characters.Hero;

public class HealthBar extends JavaPlugin {
    protected static HashMap<Player, Integer> healthTracker = new HashMap<Player, Integer>();
    private static Server server;
    protected static boolean useHeroes = false;
    private static String barFormat = "";
    private static double healthScale = 1.0;
    private static double manaScale = 1.0;
	private static String barCharacter;
    private final HealthBarPlayerListener playerListener = new HealthBarPlayerListener(this);
    private final HealthBarPluginListener pluginListener = new HealthBarPluginListener(this);
    protected final Logger logger = Logger.getLogger("Minecraft");
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof ConsoleCommandSender || sender.isOp() || (sender.hasPermission("healthbar.reload") && getConfig().getBoolean("system.usePermissions"))) {
            if (commandLabel.equalsIgnoreCase("HealthBar") && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                this.reloadConfig();
                this.parseConfig();
                for(Player player : getServer().getOnlinePlayers()) {
                	setTitle(player);
                }
                sender.sendMessage("§c[HealthBar] §9Reloaded Configuration");
                return true;
            }
        }
        return false;
    }

    public void setTitle(Player pl) {
    	int health;
    	int maxHealth;
    	if (HealthBar.useHeroes) {
    		Hero hero = HealthBarHeroes.characterManager.getHero(pl);
    		maxHealth = hero.getMaxHealth();
    		health = hero.getHealth();
    	} else {
    		health = pl.getHealth();
    		maxHealth = 20;
    	}
        if (health > 0 && health <= maxHealth) {
        	String titleBar = barFormat;
        	int healthChange = 0;
            if (!HealthBar.healthTracker.isEmpty() && HealthBar.healthTracker.containsKey(pl)) {
            	healthChange = HealthBar.healthTracker.get(pl) - health;
            }
            titleBar = titleBar.replaceAll("\\{health_bar}", StringUtils.repeat(barCharacter, (int) ((health) * healthScale) - ((healthChange < 0 && health != maxHealth && titleBar.contains("{gained_health_bar}")) ? healthChange * -1 : 0)));
            titleBar = titleBar.replaceAll("\\{max_health}", Integer.toString(maxHealth));
            titleBar = titleBar.replaceAll("\\{health_percent}", Integer.toString((int)(((double)health/maxHealth)*100.0)));
            titleBar = titleBar.replaceAll("\\{health}", Integer.toString(health));
        	if (health < maxHealth) {
            	titleBar = titleBar.replaceAll("\\{missing_health_bar}", StringUtils.repeat(barCharacter, (int) ((maxHealth - health - ((healthChange > 0 && titleBar.contains("{lost_health_bar}")) ? healthChange : 0)) * healthScale)));
            } else {
            	titleBar = titleBar.replaceAll("\\{missing_health_bar}", "");
            }
        	if(healthChange > 0) {
            	titleBar = titleBar.replaceAll("\\{lost_health_bar}", StringUtils.repeat(barCharacter, (int) (healthChange * healthScale)));
        	} else {
        		titleBar = titleBar.replaceAll("\\{lost_health_bar}", "");
        	}
        	if(healthChange > 0) {
            	titleBar = titleBar.replaceAll("\\{lost_health}", Integer.toString(healthChange));
            } else {
            	titleBar = titleBar.replaceAll("\\{lost_health}", "0");
            }
        	if(healthChange < 0 && health != maxHealth) {
            	titleBar = titleBar.replaceAll("\\{gained_health_bar}", StringUtils.repeat(barCharacter, (int) (healthChange * healthScale * -1)));
            } else {
            	titleBar = titleBar.replaceAll("\\{gained_health_bar}", "");
            }
        	if(healthChange < 0 && health != maxHealth) {
            	titleBar = titleBar.replaceAll("\\{gained_health}", Integer.toString(healthChange));
            } else {
            	titleBar = titleBar.replaceAll("\\{gained_health}", "0");
            }
            titleBar = titleBar.replaceAll("\\{missing_health}", Integer.toString(maxHealth - health));

        	if(HealthBar.useHeroes) {
        		Hero hero = HealthBarHeroes.characterManager.getHero(pl);
        		titleBar = titleBar.replaceAll("\\{mana_bar}", StringUtils.repeat(barCharacter, (int) (hero.getMana() * manaScale)));	
        		if(hero.getMaxMana() != hero.getMana()) {
        			titleBar = titleBar.replaceAll("\\{missing_mana_bar}", StringUtils.repeat(barCharacter, (int) ((hero.getMaxMana() - hero.getMana()) * manaScale)));	
        		} else {
        			titleBar = titleBar.replaceAll("\\{missing_mana_bar}", "");
        		}
                titleBar = titleBar.replaceAll("\\{mana_percent}", Integer.toString((int)(((double)hero.getMana()/hero.getMaxMana())*100.0)));
	    		titleBar = titleBar.replaceAll("\\{missing_mana}", Integer.toString(hero.getMaxMana() - hero.getMana()));
	    		titleBar = titleBar.replaceAll("\\{mana}", Integer.toString(hero.getMana()));
	    		titleBar = titleBar.replaceAll("\\{max_mana}", Integer.toString(hero.getMaxMana()));
        	}
        	if(pl instanceof SpoutPlayer) {
	            if(((SpoutPlayer) pl).getTitle().split("§e§c§e")[0] != null) {
		            titleBar = ((SpoutPlayer) pl).getTitle().split("§e§c§e")[0] + titleBar;
		            if (getConfig().getBoolean("Permissions.usePermissions")) {
		                for (Player player : getServer().getOnlinePlayers()) {
		                    if (player.hasPermission("healthbar.cansee") && player instanceof SpoutPlayer) {
		                        ((SpoutPlayer) player).setTitleFor((SpoutPlayer) pl, titleBar);
		                    }
		                }
		            } else {
		            	((SpoutPlayer) pl).setTitle(titleBar);
		            }
	            }
        	}
			HealthBar.healthTracker.put(pl, health);
        }
    }
    
    @Override
    public void onDisable() {
        this.logger.info("[HealthBar] Shutting Down");
    }

    public void parseConfig() {
    	barFormat = "§e§c§e\n" + getConfig().getString("bar.format").replaceAll("&&", "§");
		barCharacter = getConfig().getString("bar.character");
		healthScale = getConfig().getDouble("bar.healthScale");
		manaScale = getConfig().getDouble("bar.manaScale");
    }
    
    public void loadConfig() {
    	getConfig().addDefault("bar.character","|");
    	getConfig().addDefault("bar.format", "&&9[&&a{health_bar}&&b{gained_health_bar}&&6{lost_health_bar}&&c{missing_health_bar}&&9]");
    	getConfig().addDefault("bar.healthScale",1.0);
    	getConfig().addDefault("bar.manaScale",1.0);
    	getConfig().addDefault("system.usePermissions",false);
    	getConfig().options().copyDefaults(true);
    	saveConfig();
    	this.parseConfig();
    }

    public void checkHeroes() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("Heroes");
        if (plugin != null) {
            useHeroes = true;
            new HealthBarHeroes(plugin, this);
        }
    }

    @Override
    public void onEnable() {
        server = getServer();
        loadConfig();
        PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(this.playerListener, this);
        pluginManager.registerEvents(this.pluginListener, this);
        checkHeroes();
        server.getScheduler().scheduleSyncRepeatingTask(this, new HealthBarHealthListener(this), 0, 1);
    }
}