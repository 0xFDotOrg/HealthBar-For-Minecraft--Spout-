package me.plornt.healthbar;

import java.util.HashMap;
import java.util.logging.Logger;

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
    private final HealthBarPlayerListener playerListener = new HealthBarPlayerListener(this);
    private final HealthBarPluginListener pluginListener = new HealthBarPluginListener(this);
    public final Logger logger = Logger.getLogger("Minecraft");

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof ConsoleCommandSender || sender.isOp() && !getBoolean("Permissions.usePermissions") || (sender.hasPermission("healthbar.reload") && getBoolean("Permissions.usePermissions"))) {
            if (commandLabel.equalsIgnoreCase("HealthBar") && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                this.reloadConfig();
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
    	int health = pl.getHealth();
    	int maxHealth = 20;
    	if (HealthBar.useHeroes != false) {
    		maxHealth = (int) HealthBarHeroes.characterManager.getHero(pl).getMaxHealth();
    	}
        if (health >= 0 && health <= maxHealth) {
            String badHealth = "";
            String goodHealth = "";
            if (health > 0) {
                goodHealth = new String(new char[(int) (health * getDouble("Scale.barScale"))]).replace("\0", getString("Characters.barCharacter"));
            }
            if (health < maxHealth) {
                badHealth = new String(new char[(int) ((maxHealth - health) * getDouble("Scale.barScale"))]).replace("\0", getString("Characters.barCharacter"));
            }
            String playerHealthBar = "§e§c§e\n" + "§" + getString("Colors.containerColor") + getString("Characters.startCharacter")
            		+ "§" + getString("Colors.goodHealthColor") + goodHealth + "§" + getString("Colors.hurtHealthColor") + badHealth 
            		+ "§" + getString("Colors.containerColor") + getString("Characters.endCharacter");
        	if(pl instanceof SpoutPlayer) {
	            if(((SpoutPlayer) pl).getTitle().split("§e§c§e")[0] != null) {
		            playerHealthBar = ((SpoutPlayer) pl).getTitle().split("§e§c§e")[0] + playerHealthBar;
		            if (getBoolean("Permissions.usePermissions")) {
		                for (Player player : getServer().getOnlinePlayers()) {
		                    if (player.hasPermission("healthbar.cansee") && player instanceof SpoutPlayer) {
		                        ((SpoutPlayer) player).setTitleFor((SpoutPlayer) pl, playerHealthBar);
		                    }
		                }
		            } else {
		            	((SpoutPlayer) pl).setTitle(playerHealthBar);
		            }
	            }
        	}
        }
    }
    
    public String getString(String key){
        return getConfig().getString(key,"");
    }

    public boolean getBoolean(String key){
    	return getConfig().getBoolean(key, false);
    }
    
    public double getDouble(String key) {
    	return getConfig().getDouble(key);
    }
    
    @Override
    public void onDisable() {
        this.logger.info("[HealthBar] Shutting Down");
    }

    public void loadConfig() {
        getConfig().addDefault("Colors.goodHealthColor","a");
    	getConfig().addDefault("Colors.hurtHealthColor","c");
    	getConfig().addDefault("Colors.containerColor","9");
    	getConfig().addDefault("Characters.startCharacter","[");
    	getConfig().addDefault("Characters.endCharacter","]");
    	getConfig().addDefault("Characters.barCharacter","|");
    	getConfig().addDefault("Scale.barScale",1);
    	getConfig().addDefault("Permissions.usePermissions",false);
    	getConfig().options().copyDefaults(true);
    	saveConfig();
    }

    public void checkHeroes() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("Heroes");
        if (plugin != null) {
            useHeroes = true;
            new HealthBarHeroes(plugin);
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
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new HealthBarHealthListener(this), 0, 1);
    }
}