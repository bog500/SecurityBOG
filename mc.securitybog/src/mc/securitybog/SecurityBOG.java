package mc.securitybog;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import mc.securitybog.Updater.UpdateResult;
import mc.securitybog.Updater.UpdateType;

public class SecurityBOG extends JavaPlugin implements Listener {

	private final int CURSE_PROJECT_ID = 37377;
	private boolean disabled = true;
	private PluginDescriptionFile mPdfFile;
	protected static ConfigAccessor language;
	
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

		// Done initializing, tell the world
		Logger.getLogger(mPdfFile.getName()).log(Level.INFO, mPdfFile.getName() + " version " + mPdfFile.getVersion() + " enabled");
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
				this.getLogger().info("PlayerMarkers is up to date (" + this.getDescription().getVersion() + ")");
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
	
	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
	}
	
}