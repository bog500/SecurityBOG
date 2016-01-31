package mc.securitybog;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import mc.securitybog.Updater.UpdateResult;
import mc.securitybog.Updater.UpdateType;

public class SecurityBOG extends JavaPlugin implements CommandExecutor, Listener {

	private final int CURSE_PROJECT_ID = 37377;
	private boolean disabled = true;
	private PluginDescriptionFile mPdfFile;
	protected static ConfigAccessor language;
	private JsonSecurityBOG security;
	private HashSet<Location> spawnLocations = new HashSet<Location>();
	private Boolean randomSpawn = false;
	private Random rand = new Random();
	private List<Map<?, ?>> spawnLocationsList;
	
	@Override
	public void onEnable() {
		mPdfFile = this.getDescription();
				
		getSavedConfig();
		
		new JUtility(this);
		
		callMetric();

		checkUpdates();
		
		this.disabled = false;
		
		// Save the config
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		loadLanguage();
		
		getServer().getPluginManager().registerEvents(this, this);

		// Done initializing, tell the world
		Logger.getLogger(mPdfFile.getName()).log(Level.INFO, mPdfFile.getName() + " version " + mPdfFile.getVersion() + " enabled");
				
		security = new JsonSecurityBOG(this);
	}
	
	private void getSavedConfig() {
		FileConfiguration config = getConfig();
		this.randomSpawn = config.getBoolean("randomFirstSpawn");
		
		spawnLocationsList = config.getMapList("spawnLocations");

		for (Map.Entry<?, ?> entry : spawnLocationsList.get(0).entrySet()) {
			String key = entry.getKey().toString().toLowerCase();
			if(Boolean.parseBoolean(entry.getValue().toString())) {
				String[] values = key.split(" ");
				World world = Bukkit.getServer().getWorld(values[0]);
				double x = Double.parseDouble(values[1]);
				double y = Double.parseDouble(values[2]);
				double z = Double.parseDouble(values[3]);
				Location l = new Location(world, x, y, z);
				this.getLogger().info(world.getName() + " " + x + " " + y + " " + z);
				if (Boolean.parseBoolean(entry.getValue().toString())) {
					this.getLogger().info("true");
					if (!spawnLocations.contains(l)) {
						this.getLogger().info("added");
						spawnLocations.add(l);
					}
				}
			}
		}
	}

	private void callMetric() {

		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private void checkUpdates() {
		try {
			Updater updater = new Updater(this, CURSE_PROJECT_ID, this.getFile(), UpdateType.NO_DOWNLOAD, true);
			if(updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
				this.getLogger().info("New version available! " + updater.getLatestName());
				this.getLogger().info("Download it from: " + updater.getLatestFileLink());
			}else {
				this.getLogger().info("SecurityBOG is up to date (" + this.getDescription().getVersion() + ")");
			}
		}catch(Exception ex) {
			this.getLogger().warning("An error occured while checking updates.  " + ex.getMessage());
		}
		
	}
	
	private void loadLanguage() {
		// Load the strings/localization
        String langFile = ("localization.{lang}.yml").replace("{lang}", getConfig().getString("lang"));
        language = new ConfigAccessor(this, langFile);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if(checkSecurity(p)) {
			if(!p.hasPlayedBefore()) {
				setSpawn(p);
			}
		}
    }
	
	private Location setSpawn(Player player) {
		if(randomSpawn) {
			JUtility.sendMessage(player, language.getConfig().getString("teleporting_player").replace("{player}", player.getName()) );
			
			Integer index = rand.nextInt(this.spawnLocations.size());
			Location[] l = new Location[this.spawnLocations.size()];
			Location[] spawnLocs = spawnLocations.toArray(l);
			Location spawnLoc = spawnLocs[index];
			player.teleport(spawnLoc);
			return spawnLoc;
		}
		return null;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if(!event.isBedSpawn() || player.getBedSpawnLocation() == null)
		{
			if (randomSpawn && (!player.hasPlayedBefore() || player.getBedSpawnLocation() == null )) {
				Location spawnLoc = setSpawn(player);
				event.setRespawnLocation(spawnLoc);
			}
		}		
	}
	
	private Boolean checkSecurity(Player p) {
		UserBOG user = security.fetchData(p.getName());
		if(user.getPlayername().equals(p.getName())) {
			if(user.isBanned()) {
				JUtility.sendMessage(p, language.getConfig().getString("banned"));
				p.kickPlayer(language.getConfig().getString("banned"));
				return false;
			}else {
				if(user.isEmailConfirmed()) {
					JUtility.sendMessage(p, language.getConfig().getString("account_registered").replace("{website}", this.getConfig().getString("website")).replace("{player}", p.getName()));
				}else {
					JUtility.sendMessage(p, language.getConfig().getString("account_registered_unconfirmed").replace("{website}", this.getConfig().getString("website")).replace("{player}", p.getName()));
				}
				return true;
			}
			
		}else {
			String msg = language.getConfig().getString("account_not_registered").replace("{website}", this.getConfig().getString("website")).replace("{player}", p.getName());
			JUtility.sendMessage(p, msg );
			p.kickPlayer(msg);
			return false;
		}
	}
	

	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("spawntp")) {
			if(args.length == 1) {
				return spawntp(sender, args);
			}
		}
		return false;
	}

	private boolean spawntp(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!player.hasPermission("spawntp")) {
				JUtility.sendMessage(player, language.getConfig().getString("no_permission") );
				return true;
			}
			
			if(args.length != 1) {
				return false;
			}
			int index = Integer.parseInt(args[0]);
			index--; // convert it to start at 0
			Location[] l = new Location[this.spawnLocations.size()];
			Location[] spawnLocs = spawnLocations.toArray(l);
			Location spawnLoc = spawnLocs[index];
			JUtility.sendMessage(player, language.getConfig().getString("teleporting_player").replace("{player}", player.getName()) );
			player.teleport(spawnLoc);
		}
		sender.sendMessage("This command can not be used in console");
		return true;
	}
	
}
