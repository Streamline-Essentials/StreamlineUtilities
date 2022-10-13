package tv.quaint.listeners;

import net.streamline.api.SLAPI;
import net.streamline.api.events.EventPriority;
import net.streamline.api.events.EventProcessor;
import net.streamline.api.events.StreamlineEvent;
import net.streamline.api.events.StreamlineListener;
import net.streamline.api.events.server.LoginCompletedEvent;
import net.streamline.api.events.server.LoginEvent;
import net.streamline.api.events.server.LoginReceivedEvent;
import net.streamline.api.events.server.StreamlineChatEvent;
import net.streamline.api.interfaces.IStreamline;
import net.streamline.api.messages.builders.SavablePlayerMessageBuilder;
import net.streamline.api.messages.events.ProxiedMessageEvent;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.events.UserNameUpdateEvent;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.utils.UserUtils;
import tv.quaint.StreamlineUtilities;
import tv.quaint.accessors.SpigotAccessor;
import tv.quaint.events.NicknameUpdateEvent;

public class MainListener extends StreamlineListener {
    @EventProcessor
    public void onChat(StreamlineChatEvent chatEvent) {
        if (StreamlineUtilities.getConfigs().chatModifyEnabled()) {
            if (! ModuleUtils.hasPermission(chatEvent.getSender(), StreamlineUtilities.getConfigs().chatModifyPermission())) return;

            String message = ModuleUtils.replaceAllPlayerBungee(chatEvent.getSender(), chatEvent.getMessage());
            chatEvent.setMessage(message);
        }
    }

    @EventProcessor
    public void onPreJoin(LoginReceivedEvent event) {
        if (event.isCancelled()) return;

        StreamlinePlayer player = event.getResource();
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

    @EventProcessor
    public void onFullyJoin(LoginCompletedEvent event) {
        if (event.isCancelled()) return;
        if (StreamlineUtilities.getConfigs().isNicknamesEnabled()) {
            SpigotAccessor.updateCustomName(event.getResource());
        }
    }

    @EventProcessor
    public void onNameUpdate(UserNameUpdateEvent event) {
        if (event.isCancelled()) return;

        if (StreamlineUtilities.getConfigs().isNicknamesEnabled()) {
            if (! (event instanceof NicknameUpdateEvent)) {
                if (SLAPI.getInstance().getPlatform().getServerType().equals(IStreamline.ServerType.PROXY)) {
                    event.setCancelled(true);
                } else {
                    String name = ModuleUtils.parseOnProxy("%streamline_user_formatted%");
                    event.setChangeTo(name);
                    if (event.getResource().updateOnline()) {
                        StreamlinePlayer player = UserUtils.getOrGetPlayer(event.getResource().getUuid());
                        if (player != null) {
                            SpigotAccessor.updateCustomName(player, false);
                        }
                    }
                }
            }
        }
    }

    @EventProcessor(priority = EventPriority.LOWEST)
    public void onProxiedMessage(ProxiedMessageEvent event) {
        if (event.getMessage().getSubChannel().equals(SavablePlayerMessageBuilder.getSubChannel())) {
            SpigotAccessor.updateTabCMI();
        }
    }
}
