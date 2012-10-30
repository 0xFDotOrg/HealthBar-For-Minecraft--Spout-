package org.necavi.minecraft.healthbar;

import java.util.HashMap;
import java.util.TimerTask;
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
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.herocraftonline.heroes.characters.Hero;
import com.massivecraft.factions.FPlayer;
import com.tommytony.war.Team;

public class HealthBar extends JavaPlugin {
    protected static HashMap<Player, Integer> healthTracker = new HashMap<Player, Integer>();
    protected static HashMap<Player, Integer> manaTracker = new HashMap<Player, Integer>();
    private static Server server;
    protected static boolean useHeroes = false;
    protected static boolean useFactions = false;
    protected static boolean useWar = false;
    private static boolean hideDuringSneak = true;
    protected static boolean usePermissions = false;
    private static boolean overrideTitle = false;
    private static boolean setForEachPlayer = false;
    private static String barFormat = "";
    private static double healthScale = 1.0;
    private static double manaScale = 1.0;
	private static String barCharacter;
	private static String health75 = "";
	private static String health50 = "";
	private static String health25 = "";
	private static String health0 = "";
    private final HealthBarPlayerListener playerListener = new HealthBarPlayerListener(this);
    private final HealthBarPluginListener pluginListener = new HealthBarPluginListener(this);
    protected final Logger logger = Logger.getLogger("Minecraft");
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof ConsoleCommandSender || sender.isOp() || sender.hasPermission("healthbar.reload")) {
            if (commandLabel.equalsIgnoreCase("HealthBar") && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                this.reloadConfig();
                this.parseConfig();
                for(SpoutPlayer player : SpoutManager.getOnlinePlayers()) {
                	setTitle(player);
                }
                sender.sendMessage("§c[HealthBar] §9Reloaded Configuration");
                return true;
            }
        }
        return false;
    }
    
    public void delayedSetTitle(final SpoutPlayer player) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, new TimerTask() {
            @Override
            public void run() {
                setTitle(player);
            }
        });
    }
    
    public void setTitle(SpoutPlayer pl) {
    	int health;
    	int maxHealth;
    	if (HealthBar.useHeroes) {
    		Hero hero = HealthBarHeroes.characterManager.getHero(pl);
    		maxHealth = hero.getMaxHealth();
    		health = hero.getHealth();
    	} else {
    		health = pl.getHealth();
    		maxHealth = pl.getMaxHealth();
    	}
    	if(hideDuringSneak && pl.isSneaking()) {
			HealthBar.healthTracker.put(pl, health);
			return;
    	}
        if (health > 0 && health <= maxHealth) {
        	String titleBar = barFormat;
        	int healthChange = 0;
        	int healthPercent = (int)(((double)health/maxHealth)*100.0);
            if (!HealthBar.healthTracker.isEmpty() && HealthBar.healthTracker.containsKey(pl)) {
            	healthChange = HealthBar.healthTracker.get(pl) - health;
            }
            titleBar = titleBar.replaceAll("\\{health_bar}", StringUtils.repeat(barCharacter, (int) (health - ((healthChange < 0 && health != maxHealth && titleBar.contains("{gained_health_bar}")) ? healthChange * -1 : 0) * healthScale)));
            titleBar = titleBar.replaceAll("\\{max_health}", Integer.toString(maxHealth));
            titleBar = titleBar.replaceAll("\\{health_percent}", Integer.toString(healthPercent));
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
            String color = "";
            if(healthPercent > 75) {
            	color = health75;
            } else if(healthPercent > 50) {
            	color = health50;
            } else if(healthPercent > 25) {
            	color = health25;
            } else {
            	color = health0;
            }
            titleBar = titleBar.replaceAll("\\{health_color}", color);
			HealthBar.healthTracker.put(pl, health);
        	if(HealthBar.useHeroes) {
        		Hero hero = HealthBarHeroes.characterManager.getHero(pl);
        		titleBar = titleBar.replaceAll("\\{mana_bar}", StringUtils.repeat(barCharacter, (int) (hero.getMana() * manaScale)));	
        		int mana = hero.getMana();
        		int maxMana = hero.getMaxMana();
        		if(maxMana != mana) {
        			titleBar = titleBar.replaceAll("\\{missing_mana_bar}", StringUtils.repeat(barCharacter, (int) ((maxMana - mana) * manaScale)));	
        		} else {
        			titleBar = titleBar.replaceAll("\\{missing_mana_bar}", "");
        		}
                titleBar = titleBar.replaceAll("\\{mana_percent}", Integer.toString((int)(((double)mana/maxMana)*100.0)));
	    		titleBar = titleBar.replaceAll("\\{missing_mana}", Integer.toString(maxMana - mana));
	    		titleBar = titleBar.replaceAll("\\{mana}", Integer.toString(mana));
	    		titleBar = titleBar.replaceAll("\\{max_mana}", Integer.toString(maxMana));
				HealthBar.manaTracker.put(pl, mana);
        	}
    		titleBar.replaceAll("\\{name}", pl.getDisplayName());
        	if(!overrideTitle) {
        		titleBar = pl.getTitle().split("§e§c§e")[0] + titleBar;
        	}
        	if(useWar) {
        		titleBar.replaceAll("\\{relation_color}", Team.getTeamByPlayerName(pl.getName()).getKind().getColor().toString());
        	}
		    if (usePermissions || setForEachPlayer) {
		    	String tempTitle;
		        for (SpoutPlayer player : SpoutManager.getOnlinePlayers()) {
		            if ((usePermissions && player.hasPermission("healthbar.cansee") || setForEachPlayer)) {
		                if(useFactions) {
		                	tempTitle = titleBar.replaceAll("\\{relation_color}", ((FPlayer) player).getColorTo((FPlayer) pl).toString());
		                } else {
		                	tempTitle = titleBar.replaceAll("\\{relation_color}", "");
		                }
		                player.setTitleFor(pl, tempTitle);
		            }
		        }
		    } else {
		    	pl.setTitle(titleBar.replaceAll("\\{relation_color}", ""));
	        }
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
		hideDuringSneak = getConfig().getBoolean("system.hideDuringSneak");
		usePermissions = getConfig().getBoolean("system.usePermissions");
		setForEachPlayer = getConfig().getBoolean("system.setForEachPlayer");
    	health75 = getConfig().getString("colors.health75").replaceAll("&&", "§");
    	health50 = getConfig().getString("colors.health50").replaceAll("&&", "§");
    	health25 = getConfig().getString("colors.health25").replaceAll("&&", "§");
    	health0 = getConfig().getString("colors.health0").replaceAll("&&", "§");
		overrideTitle = getConfig().getBoolean("system.overrideTitle");
    }
    
    public void loadConfig() {
    	getConfig().addDefault("bar.character","|");
    	getConfig().addDefault("bar.format", "&&9[&&a{health_bar}&&b{gained_health_bar}&&6{lost_health_bar}&&c{missing_health_bar}&&9]");
    	getConfig().addDefault("bar.healthScale",1.0);
    	getConfig().addDefault("bar.manaScale",1.0);
    	getConfig().addDefault("colors.health75", "&&a");
    	getConfig().addDefault("colors.health50", "&&2");
    	getConfig().addDefault("colors.health25", "&&c");
    	getConfig().addDefault("colors.health0", "&&0");
    	getConfig().addDefault("system.usePermissions",false);
    	getConfig().addDefault("system.hideDuringSneak",true);
    	getConfig().addDefault("system.setForEachPlayer", false);
    	getConfig().addDefault("system.overrideTitle", false);
    	getConfig().options().copyDefaults(true);
    	saveConfig();
    	this.parseConfig();
    }

    public void checkPlugins() {
    	PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin plugin = pluginManager.getPlugin("Heroes");
        if (plugin != null) {
            useHeroes = true;
            new HealthBarHeroes(plugin, this);
        }
        plugin = pluginManager.getPlugin("Factions");
        if (plugin != null) {
            useFactions = true;
            new HealthBarFactions(this);
        }
        plugin = pluginManager.getPlugin("War");
        if (plugin != null) {
            useWar = true;
            new HealthBarWar(this);
        }
    }
    @Override
    public void onEnable() {
        server = getServer();
        loadConfig();
        PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(this.playerListener, this);
        pluginManager.registerEvents(this.pluginListener, this);
        checkPlugins();
        server.getScheduler().scheduleSyncRepeatingTask(this, new HealthBarHealthListener(this), 0, 1);
    }
}