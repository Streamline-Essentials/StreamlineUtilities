package host.plas.listeners;

import net.streamline.api.SLAPI;
import net.streamline.api.events.server.LoginCompletedEvent;
import net.streamline.api.events.server.LoginReceivedEvent;
import net.streamline.api.events.server.LogoutEvent;
import net.streamline.api.events.server.StreamlineChatEvent;
import net.streamline.api.messages.builders.SavablePlayerMessageBuilder;
import net.streamline.api.messages.events.ProxiedMessageEvent;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.events.UserNameUpdateByOtherEvent;
import net.streamline.api.savables.events.UserNameUpdateEvent;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.UserUtils;
import host.plas.StreamlineUtilities;
import host.plas.accessors.SpigotAccessor;
import host.plas.essentials.EssentialsManager;
import host.plas.essentials.users.UtilitiesUser;
import tv.quaint.events.BaseEventListener;
import host.plas.events.NicknameUpdateEvent;
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

    @BaseProcessor
    public void onFullyJoin(LoginCompletedEvent event) {
        if (event.isCancelled()) return;

        StreamlinePlayer player = event.getResource();
        if (player == null) return;

        if (StreamlineUtilities.getMaintenanceConfig().isModeEnabled()) {
            if (! StreamlineUtilities.getMaintenanceConfig().containsAllowed(player.getUuid())) {
                ModuleUtils.kick(player, ModuleUtils.replaceAllPlayerBungee(player, StreamlineUtilities.getMaintenanceConfig().getModeKickMessage()));

                event.setCancelled(true);
            }
        }

        UtilitiesUser user = EssentialsManager.getOrGetUser(event.getResource().getUuid());
        if (SLAPI.isProxy()) {
            if (StreamlineUtilities.getConfigs().lastServerEnabled()) {
                if (StreamlineUtilities.getConfigs().lastServerPermissionRequired()) {
                    if (ModuleUtils.hasPermission(event.getResource(), StreamlineUtilities.getConfigs().lastServerPermissionValue())) user.goToLastServer();
                } else user.goToLastServer();
            }
        }

        if (StreamlineUtilities.getConfigs().isNicknamesEnabled()) {
            SpigotAccessor.updateCustomName(event.getResource(), event.getResource().getDisplayName());
        }
    }

    @BaseProcessor
    public void onLeave(LogoutEvent event) {
        UtilitiesUser user = EssentialsManager.getOrGetUser(event.getResource().getUuid());
        user.setLastServer(event.getResource().getLatestServer());
        user.saveAll();
        EssentialsManager.syncUser(user);
    }

    @BaseProcessor
    public void onNameUpdate(UserNameUpdateEvent event) {
        if (event.isCancelled()) return;

        if (StreamlineUtilities.getConfigs().isNicknamesEnabled()) {
            if (! (event instanceof NicknameUpdateEvent) && ! (event instanceof UserNameUpdateByOtherEvent)) {
                if (SLAPI.isProxy()) {
                    event.setCancelled(true);
                } else {
                    String name;
                    if (SLAPI.isProxiedServer()) {
                        name = ModuleUtils.parseOnProxy(event.getResource(), StreamlineUtilities.getConfigs().getNicknamesFormat()
                                .replace("%this_input%", event.getResource().getLatestName()));
                        if (name == null) return;
                    } else {
                        name = ModuleUtils.replaceAllPlayerBungee(event.getResource(), StreamlineUtilities.getConfigs().getNicknamesFormat()
                                .replace("%this_input%", event.getResource().getLatestName()));
                        if (name == null) return;
                    }
                    event.setChangeTo(name);
                    if (event.getResource().updateOnline()) {
                        StreamlineUser user = UserUtils.getOrGetUser(event.getResource().getUuid());
                        if (user != null) {
                            if (user instanceof StreamlinePlayer) {
                                SpigotAccessor.updateCustomName((StreamlinePlayer) user, name);
                            }
                        }
                    }
                }
            }
        }
    }

    @BaseProcessor(priority = BaseEventPriority.LOWEST)
    public void onProxiedMessage(ProxiedMessageEvent event) {
        if (event.getMessage().getSubChannel().equals(SavablePlayerMessageBuilder.getSubChannel())) {
            SpigotAccessor.updateTabCMI();
        }
    }
}
