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

    public static void updateCustomName(StreamlinePlayer player) {
        if (! ensureSafe()) return;

        updateCustomName(player, true);
    }


    public static void updateCustomName(StreamlinePlayer player, boolean tabListAlso) {
        if (! ensureSafe()) return;

        updateNickCMI(player);
        updateNickEss(player);

        Player p = getPlayer(player.getUuid());
        if (p == null) return;

        p.setCustomName(player.getDisplayName());
        p.setDisplayName(player.getDisplayName());
        if (tabListAlso) updateTabList(player);
    }

    public static void updateTabList(StreamlinePlayer player) {
        if (! ensureSafe()) return;

        Player p = getPlayer(player.getUuid());
        if (p == null) return;

        p.setPlayerListName(player.getDisplayName());
    }

    public static boolean isCMI() {
        if (! ensureSafe()) return false;
        return Bukkit.getServer().getPluginManager().isPluginEnabled("CMI");
    }

    public static void updateNickCMI(StreamlinePlayer player) {
        updateNickCMI(player, true);
    }

    public static void updateNickCMI(StreamlinePlayer player, boolean updateTab) {
        if (! isCMI()) return;

        CMIUser user = CMI.getInstance().getPlayerManager().getUser(UUID.fromString(player.getUuid()));
        if (user == null) {
            StreamlineUtilities.getInstance().logWarning("Could not get CMIUser for player '" + player.getUuid() + "'. Skipping...");
            return;
        }

        user.setNickName(player.getDisplayName(), true);
        user.setDisplayName(player.getDisplayName());
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

    public static void updateNickEss(StreamlinePlayer player) {
        if (! isEssX()) return;

        Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");

        User user = essentials.getUser(UUID.fromString(player.getUuid()));
        if (user == null) {
            StreamlineUtilities.getInstance().logWarning("Could not get Essentials User for player '" + player.getUuid() + "'. Skipping...");
            return;
        }

        user.setNickname(player.getDisplayName());
        user.setDisplayNick();
    }
}
