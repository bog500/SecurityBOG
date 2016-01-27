package mc.securitybog;


import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class JUtility {
    // Define variables
    private static SecurityBOG plugin = null;
    private static final ConcurrentMap<String, ConcurrentMap<String, Object>> save = new ConcurrentHashMap<>();

    private static String messagePrefix;
    
    JUtility(SecurityBOG instance) {
        plugin = instance;
        messagePrefix = ChatColor.ITALIC + "" + ChatColor.GRAY + "[" + ChatColor.GREEN + plugin.getDescription().getName() + ChatColor.GRAY + "] " + ChatColor.RESET;
    }

    /**
     * Sends a server-wide message.
     *
     * @param msg the message to send.
     */
    public static void serverMsg(String msg) {
        if (plugin.getConfig().getBoolean("tagmessages")) {
            Bukkit.getServer().broadcastMessage(messagePrefix + msg);
        } else {
            Bukkit.getServer().broadcastMessage(msg);
        }

    }

    /**
     * Sends a message to a player prepended with the plugin name.
     *
     * @param 			 player the player to message.
     * @param msg    	 the message to send.
     */
    public static void sendMessage(CommandSender player, String msg) {
        sendMessage(player, msg, plugin.getConfig().getBoolean("tagmessages"));
    }
    
    
    /**
     * Sends a message to a player prepended with the plugin name.
     *
     * @param 			 player the player to message.
     * @param msg    	 the message to send.
     * @param showTag    show or hide plugin name tag.
     */
    public static void sendMessage(CommandSender player, String msg, boolean showTag) {
        if (showTag) {
            player.sendMessage(messagePrefix + " " + msg);
        } else {
            player.sendMessage(msg);
        }
    }






    /**
     * Returns true if <code>player</code> has the permission called <code>permission</code>.
     *
     * @param player     the player to check.
     * @param permission the permission to check for.
     * @return boolean
     */
    public static boolean hasPermission(OfflinePlayer player, String permission) {
        return player == null || player.getPlayer().hasPermission(permission);
    }

    /**
     * Returns true if <code>player</code> has the permission called <code>permission</code> or is an OP.
     *
     * @param player     the player to check.
     * @param permission the permission to check for.
     * @return boolean
     */
    public static boolean hasPermissionOrOP(OfflinePlayer player, String permission) {
        return player == null || player.isOp() || player.getPlayer().hasPermission(permission);
    }



    /**
     * Saves <code>data</code> under the key <code>name</code> to <code>player</code>.
     *
     * @param player the player to save data to.
     * @param name   the name of the data.
     * @param data   the data to save.
     */
    public static void saveData(OfflinePlayer player, String name, Object data) {
        // Create new save for the player if one doesn't already exist
        if (!save.containsKey(player.getName())) {
            save.put(player.getName(), new ConcurrentHashMap<>());
        }

        // Prepend the data with "jafk" to avoid plugin collisions and save the data
        save.get(player.getName()).put(name.toLowerCase(), data);
    }

    /**
     * Returns the data with the key <code>name</code> from <code>player</code>'s HashMap.
     *
     * @param player the player to check.
     * @param name   the key to grab.
     */
    public static Optional<Object> getData(OfflinePlayer player, String name) {
        if (save.containsKey(player.getName())) {
            return Optional.ofNullable(save.get(player.getName()).getOrDefault(name, null));
        }
        return Optional.empty();
    }

    /**
     * Removes the data with the key <code>name</code> from <code>player</code>.
     *
     * @param player the player to remove data from.
     * @param name   the key of the data to remove.
     */
    public static void removeData(OfflinePlayer player, String name) {
        if (save.containsKey(player.getName())) save.get(player.getName()).remove(name.toLowerCase());
    }

    /**
     * Removes all data for the <code>player</code>.
     *
     * @param player the player whose data to remove.
     */
    public static void removeAllData(OfflinePlayer player) {
        save.remove(player.getName());
    }

}
