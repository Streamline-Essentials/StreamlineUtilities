package host.plas.accessors;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.streamline.api.SLAPI;
import singularity.data.players.CosmicPlayer;
import singularity.data.players.location.CosmicLocation;
import singularity.interfaces.ISingularityExtension;
import singularity.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import host.plas.StreamlineUtilities;

import java.util.UUID;

public class SpigotAccessor {
    public static boolean ensureSafe() {
        return SLAPI.getInstance().getPlatform().getServerType().equals(ISingularityExtension.ServerType.BACKEND);
    }

    public static Player getPlayer(String uuid) {
        if (! ensureSafe()) return null;

        return Bukkit.getPlayer(UUID.fromString(uuid));
    }

    public static void updateCustomName(CosmicPlayer player, String name) {
        if (! ensureSafe()) return;

        updateCustomName(player, name, true);
    }


    public static void updateCustomName(CosmicPlayer player, String name, boolean tabListAlso) {
        if (! ensureSafe()) return;

        updateNickCMI(player, name, false);
        updateNickEss(player, name);

        Player p = getPlayer(player.getUuid());
        if (p == null) return;

        p.setCustomName(name);
        p.setDisplayName(name);
        if (tabListAlso) updateTabList(player, name);
    }

    public static void updateTabList(CosmicPlayer player, String name) {
        if (! ensureSafe()) return;

        Player p = getPlayer(player.getUuid());
        if (p == null) return;

        p.setPlayerListName(name);
    }

    public static boolean isCMI() {
        if (! ensureSafe()) return false;
        return Bukkit.getServer().getPluginManager().isPluginEnabled("CMI");
    }

    public static void updateNickCMI(CosmicPlayer player, String name) {
        updateNickCMI(player, name, true);
    }

    public static void updateNickCMI(CosmicPlayer player, String name, boolean updateTab) {
        if (! isCMI()) return;

        try {
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(UUID.fromString(player.getUuid()));
            if (user == null) {
                StreamlineUtilities.getInstance().logWarning("Could not get CMIUser for player '" + player.getUuid() + "'. Skipping...");
                return;
            }

            user.setNickName(name, true);
            user.setDisplayName(name);
            if (updateTab) updateTabCMI();
        } catch (Exception e) {
            StreamlineUtilities.getInstance().logWarning("Could not get CMIUser for player '" + player.getUuid() + "' due to error. Skipping...");
            MessageUtils.logDebug("Error:", e);
        }
    }

    public static void updateTabCMI() {
        if (! isCMI()) return;

        try {
            CMI.getInstance().getTabListManager().updateTabList();
        } catch (Exception e) {
            StreamlineUtilities.getInstance().logWarning("Could not update CMI TabList due to error. Skipping...");
            MessageUtils.logDebug("Error:", e);
        }
    }

    public static boolean isEssX() {
        if (! ensureSafe()) return false;
        return Bukkit.getServer().getPluginManager().isPluginEnabled("Essentials");
    }

    public static void updateNickEss(CosmicPlayer player, String name) {
        if (! isEssX()) return;

        try {
            Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");

            User user = essentials.getUser(UUID.fromString(player.getUuid()));
            if (user == null) {
                StreamlineUtilities.getInstance().logWarning("Could not get Essentials User for player '" + player.getUuid() + "'. Skipping...");
                return;
            }

            user.setNickname(name);
            user.setDisplayNick();
        } catch (Exception e) {
            StreamlineUtilities.getInstance().logWarning("Could not get Essentials User for player '" + player.getUuid() + "' due to error. Skipping...");
            MessageUtils.logDebug("Error:", e);
        }
    }

    public static void teleport(CosmicPlayer player, CosmicLocation location) {
        Player p = getPlayer(player.getUuid());
        if (p == null) return;
        Location l = new Location(Bukkit.getWorld(location.getWorldName()), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        p.teleport(l);
    }

    public static void teleport(CosmicPlayer player, CosmicPlayer to) {
        teleport(player, to.getLocation());
    }
}
