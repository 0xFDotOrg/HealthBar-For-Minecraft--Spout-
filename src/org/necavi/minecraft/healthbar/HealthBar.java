package org.necavi.minecraft.healthbar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.player.SpoutPlayer;

public class HealthBar extends JavaPlugin {
    public static HashMap<Player, Integer> healthTracker = new HashMap<Player, Integer>();
    public static HealthBar plugin;
    public static Server server;
    public static boolean useHeroes = false;
    public static HashSet<String> configValues = new HashSet<String>();
    public static String barFormat = "";
    public static double barScale = 1.0;
    public static Pattern pattern = Pattern.compile("\\{[a-z_]+}");
    private final HealthBarPlayerListener playerListener = new HealthBarPlayerListener(this);
    private final HealthBarPluginListener pluginListener = new HealthBarPluginListener(this);
    public final Logger logger = Logger.getLogger("Minecraft");
	private String barCharacter;
    
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
    	if (HealthBar.useHeroes != false) {
    		maxHealth = (int) HealthBarHeroes.characterManager.getHero(pl).getMaxHealth();
    		health = HealthBarHeroes.characterManager.getHero(pl).getHealth();
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
        	if(HealthBar.configValues.contains("lost_health_bar")) {
        		if(healthChange > 0) {
            		titleBar = titleBar.replaceAll("\\{lost_health_bar}", new String(new char[(int) (healthChange * barScale)]).replace("\0", barCharacter));
        		} else {
        			titleBar = titleBar.replaceAll("\\{lost_health_bar}", "");
        		}
        	}
        	if(HealthBar.configValues.contains("lost_health")) {
        		if(healthChange > 0) {
            		titleBar = titleBar.replaceAll("\\{lost_health}", Integer.toString(healthChange));
            	} else {
            		titleBar = titleBar.replaceAll("\\{lost_health}", "0");
            	}
            }
        	if(HealthBar.configValues.contains("gained_health_bar")) {
        		if(healthChange < 0 && health != maxHealth) {
            		titleBar = titleBar.replaceAll("\\{gained_health_bar}", new String(new char[(int) (healthChange * barScale * -1)]).replace("\0", barCharacter));
            	} else {
            		titleBar = titleBar.replaceAll("\\{gained_health_bar}", "");
            	}
        	}
            if(HealthBar.configValues.contains("gained_health")) {
        		if(healthChange < 0 && health != maxHealth) {
            		titleBar = titleBar.replaceAll("\\{gained_health}", Integer.toString(healthChange));
            	} else {
            		titleBar = titleBar.replaceAll("\\{gained_health}", "0");
            	}
            }
        	if(HealthBar.configValues.contains("missing_health_bar")) {
        		if (health < maxHealth) {
            		titleBar = titleBar.replaceAll("\\{missing_health_bar}", new String(new char[(int) ((maxHealth - health - (healthChange > 0 ? healthChange : 0)) * barScale)]).replace("\0", barCharacter));
            	} else {
            		titleBar = titleBar.replaceAll("\\{missing_health_bar}", "");
            	}
        	}
            if(HealthBar.configValues.contains("missing_health")) {
        		if (health < maxHealth) {
            		titleBar = titleBar.replaceAll("\\{missing_health}", Integer.toString(maxHealth - health));
            	} else {
            		titleBar = titleBar.replaceAll("\\{missing_health}", "0");
            	}
            }
            if(HealthBar.configValues.contains("max_health")) {
            	titleBar = titleBar.replaceAll("\\{max_health}", Integer.toString(maxHealth));
            }
            if(HealthBar.configValues.contains("health_percent")) {
            	titleBar = titleBar.replaceAll("\\{health_percent}", Integer.toString((int)(((double)health/maxHealth)*100.0)));
            }
            if(HealthBar.configValues.contains("health_bar")) {
            	titleBar = titleBar.replaceAll("\\{health_bar}", new String(new char[(int) ((health) * barScale) - ((healthChange < 0 && health != maxHealth) ? healthChange * -1 : 0)]).replace("\0", barCharacter));
            }
            if(HealthBar.configValues.contains("health")) {
            	titleBar = titleBar.replaceAll("\\{health}", Integer.toString(health));
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
    	HealthBar.configValues.clear();
		Matcher matcher = pattern.matcher(barFormat);
		while(matcher.find()) {
			HealthBar.configValues.add(matcher.group().replace("{","").replace("}",""));
		}
		barCharacter = getConfig().getString("bar.character");
    }
    
    public void loadConfig() {
    	getConfig().addDefault("bar.character","|");
    	getConfig().addDefault("bar.format", "&&9[&&a{health_bar}&&6{lost_health_bar}&&b{gained_health_bar}&&c{missing_health_bar}&&9]");
    	getConfig().addDefault("bar.scale",1);
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