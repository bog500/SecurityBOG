package mc.securitybog;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
	
	@Override
	public void onEnable() {
		mPdfFile = this.getDescription();
				
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
		JUtility.sendMessage(player, language.getConfig().getString("teleporting_player").replace("{player}", player.getName()) );
		Location spawnLoc = new Location(Bukkit.getServer().getWorld("world"), 0, 0, 0);
		player.teleport(spawnLoc);
		return spawnLoc;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if(!event.isBedSpawn() || player.getBedSpawnLocation() == null)
		{
			if (!player.hasPlayedBefore() || player.getBedSpawnLocation() == null ) {
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
		return true;
	}
	
}
