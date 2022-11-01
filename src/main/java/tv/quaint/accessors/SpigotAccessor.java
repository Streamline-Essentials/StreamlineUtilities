package tv.quaint.accessors;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.streamline.api.SLAPI;
import net.streamline.api.interfaces.IStreamline;
import net.streamline.api.savables.users.StreamlinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tv.quaint.StreamlineUtilities;

import java.util.UUID;

public class SpigotAccessor {
    public static boolean ensureSafe() {
        return SLAPI.getInstance().getPlatform().getServerType().equals(IStreamline.ServerType.BACKEND);
    }

    public static Player getPlayer(String uuid) {
        if (! ensureSafe()) return null;

        return Bukkit.getPlayer(UUID.fromString(uuid));
    }

    public static void updateCustomName(StreamlinePlayer player, String name) {
        if (! ensureSafe()) return;

        updateCustomName(player, name, true);
    }


    public static void updateCustomName(StreamlinePlayer player, String name, boolean tabListAlso) {
        if (! ensureSafe()) return;

        updateNickCMI(player, name, false);
        updateNickEss(player, name);

        Player p = getPlayer(player.getUuid());
        if (p == null) return;

        p.setCustomName(name);
        p.setDisplayName(name);
        if (tabListAlso) updateTabList(player, name);
    }

    public static void updateTabList(StreamlinePlayer player, String name) {
        if (! ensureSafe()) return;

        Player p = getPlayer(player.getUuid());
        if (p == null) return;

        p.setPlayerListName(name);
    }

    public static boolean isCMI() {
        if (! ensureSafe()) return false;
        return Bukkit.getServer().getPluginManager().isPluginEnabled("CMI");
    }

    public static void updateNickCMI(StreamlinePlayer player, String name) {
        updateNickCMI(player, name, true);
    }

    public static void updateNickCMI(StreamlinePlayer player, String name, boolean updateTab) {
        if (! isCMI()) return;

        CMIUser user = CMI.getInstance().getPlayerManager().getUser(UUID.fromString(player.getUuid()));
        if (user == null) {
            StreamlineUtilities.getInstance().logWarning("Could not get CMIUser for player '" + player.getUuid() + "'. Skipping...");
            return;
        }

        user.setNickName(name, true);
        user.setDisplayName(name);
        if (updateTab) updateTabCMI();
    }

    public static void updateTabCMI() {
        if (! isCMI()) return;

        CMI.getInstance().getTabListManager().updateTabList();
    }

    public static boolean isEssX() {
        if (! ensureSafe()) return false;
        return Bukkit.getServer().getPluginManager().isPluginEnabled("Essentials");
    }

    public static void updateNickEss(StreamlinePlayer player, String name) {
        if (! isEssX()) return;

        Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");

        User user = essentials.getUser(UUID.fromString(player.getUuid()));
        if (user == null) {
            StreamlineUtilities.getInstance().logWarning("Could not get Essentials User for player '" + player.getUuid() + "'. Skipping...");
            return;
        }

        user.setNickname(name);
        user.setDisplayNick();
    }
}
