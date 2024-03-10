package host.plas.listeners;

import net.streamline.api.SLAPI;
import net.streamline.api.data.console.StreamSender;
import net.streamline.api.data.players.StreamPlayer;
import net.streamline.api.events.server.LoginCompletedEvent;
import net.streamline.api.events.server.LoginReceivedEvent;
import net.streamline.api.events.server.LogoutEvent;
import net.streamline.api.events.server.StreamlineChatEvent;
import net.streamline.api.messages.events.ProxiedMessageEvent;
import net.streamline.api.modules.ModuleUtils;
import host.plas.StreamlineUtilities;
import host.plas.accessors.SpigotAccessor;
import host.plas.essentials.EssentialsManager;
import host.plas.essentials.users.UtilitiesUser;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseEventPriority;
import tv.quaint.events.processing.BaseProcessor;

public class MainListener implements BaseEventListener {
    @BaseProcessor
    public void onChat(StreamlineChatEvent chatEvent) {
        if (StreamlineUtilities.getConfigs().chatModifyEnabled()) {
            if (! ModuleUtils.hasPermission(chatEvent.getSender(), StreamlineUtilities.getConfigs().chatModifyPermission())) return;

            String message = ModuleUtils.replaceAllPlayerBungee(chatEvent.getSender(), chatEvent.getMessage());
            chatEvent.setMessage(message);
        }
    }

    @BaseProcessor
    public void onPreJoin(LoginReceivedEvent event) {
        if (event.isCancelled()) return;

        StreamSender player = event.getSender();
        if (player == null) return;

        if (StreamlineUtilities.getMaintenanceConfig().isModeEnabled()) {
            if (! StreamlineUtilities.getMaintenanceConfig().containsAllowed(player.getUuid())) {
                LoginReceivedEvent.ConnectionResult result = new LoginReceivedEvent.ConnectionResult();
                result.setCancelled(true);
                result.setDisconnectMessage(ModuleUtils.replaceAllPlayerBungee(player, StreamlineUtilities.getMaintenanceConfig().getModeKickMessage()));

                event.setCancelled(true);
                event.setResult(result);
            }
        }
    }

    @BaseProcessor
    public void onFullyJoin(LoginCompletedEvent event) {
        if (event.isCancelled()) return;

        StreamSender player = event.getSender();
        if (player == null) return;
        if (! (player instanceof StreamPlayer)) return;
        StreamPlayer streamPlayer = (StreamPlayer) player;

        if (StreamlineUtilities.getMaintenanceConfig().isModeEnabled()) {
            if (! StreamlineUtilities.getMaintenanceConfig().containsAllowed(player.getUuid())) {
                ModuleUtils.kick(streamPlayer, ModuleUtils.replaceAllPlayerBungee(player, StreamlineUtilities.getMaintenanceConfig().getModeKickMessage()));

                event.setCancelled(true);
            }
        }

        UtilitiesUser user = EssentialsManager.getOrGetUser(event.getSender().getUuid()).join().orElse(null);
        if (user == null) return;
        if (SLAPI.isProxy()) {
            if (StreamlineUtilities.getConfigs().lastServerEnabled()) {
                if (StreamlineUtilities.getConfigs().lastServerPermissionRequired()) {
                    if (ModuleUtils.hasPermission(event.getSender(), StreamlineUtilities.getConfigs().lastServerPermissionValue())) user.goToLastServer();
                } else user.goToLastServer();
            }
        }

        if (StreamlineUtilities.getConfigs().isNicknamesEnabled()) {
            SpigotAccessor.updateCustomName(streamPlayer, event.getSender().getDisplayName());
        }
    }

    @BaseProcessor
    public void onLeave(LogoutEvent event) {
        UtilitiesUser user = EssentialsManager.getOrGetUser(event.getSender().getUuid()).join().orElse(null);
        if (user == null) return;
        user.setLastServer(event.getSender().getServerName());
        user.save();
    }

    @BaseProcessor(priority = BaseEventPriority.LOWEST)
    public void onProxiedMessage(ProxiedMessageEvent event) {
//        if (event.getMessage().getSubChannel().equals(SavablePlayerMessageBuilder.getSubChannel())) {
//            SpigotAccessor.updateTabCMI();
//        }
    }
}
