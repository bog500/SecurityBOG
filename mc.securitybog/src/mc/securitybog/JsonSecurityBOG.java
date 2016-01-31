package mc.securitybog;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;




public class JsonSecurityBOG {
	
	private JsonObject data;
	private JavaPlugin plugin;
	private String url;
	
	public JsonSecurityBOG(JavaPlugin plugin) {
		this.plugin = plugin;
		this.url = plugin.getConfig().getString("securityUrl");
	}
	
	public UserBOG fetchData(String playername) {
		String urlGetUser = url.replace("{playername}", playername);
        String apidata = fetchContent(urlGetUser);
        JsonParser parser = new JsonParser();
        if (apidata.equalsIgnoreCase("nothing.")) {
            data = parser.parse("{}").getAsJsonObject();
        } else {
            data = parser.parse(apidata).getAsJsonObject();
        }
        
        String playernameData = getPlayername();
    	Boolean isMod = getIsMod();
    	Boolean isAdmin = getIsAdmin();
    	Boolean isBanned = getIsBanned();
    	Boolean isEmailConfirmed = getIsEmailConfirmed();
        
        UserBOG user = new UserBOG(playernameData,isMod, isAdmin, isBanned, isEmailConfirmed);
        return user;
    }
	
	public String getPlayername() {
        if (!data.has("Playername")) {
            return "N\\A";
        }
        return data.get("Playername").getAsString();
    }
	
	public Boolean getIsMod() {
        if (!data.has("IsMod")) {
            return false;
        }
        return data.get("IsMod").getAsBoolean();
    }
	
	public Boolean getIsBanned() {
        if (!data.has("IsBanned")) {
            return false;
        }
        return data.get("IsBanned").getAsBoolean();
    }
	
	public Boolean getIsAdmin() {
        if (!data.has("IsAdmin")) {
            return false;
        }
        return data.get("IsAdmin").getAsBoolean();
    }
	
	public Boolean getIsEmailConfirmed() {
        if (!data.has("IsEmailConfirmed")) {
            return false;
        }
        return data.get("IsEmailConfirmed").getAsBoolean();
    }

    public String fetchContent(String url) {
        try {
            return Resources.toString(new URL(url), Charsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().warning("Couldn't fetch data from security API!");
        }
        return "nothing.";
    }
}
