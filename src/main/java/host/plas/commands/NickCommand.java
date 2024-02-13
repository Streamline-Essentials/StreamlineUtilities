package host.plas.commands;

import lombok.Getter;
import net.streamline.api.SLAPI;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.interfaces.IStreamline;
import net.streamline.api.messages.builders.SavablePlayerMessageBuilder;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.UserUtils;
import host.plas.StreamlineUtilities;
import host.plas.accessors.SpigotAccessor;
import host.plas.events.NicknameUpdateEvent;

import java.util.concurrent.ConcurrentSkipListSet;

public class NickCommand extends ModuleCommand {
    @Getter
    private final String messageResultCleared;
    @Getter
    private final String messageResultChanged;
    @Getter
    private final String messageResultCancelled;
    @Getter
    private final String permissionSetOthers;
    @Getter
    private final String permissionSetNonFormatted;

    public NickCommand() {
        super(StreamlineUtilities.getInstance(),
                "proxynickname",
                "streamline.command.nickname.default",
                "pn", "proxyn", "pnick", "pnickname"
        );

        messageResultCleared = getCommandResource().getOrSetDefault("messages.result.cleared",
                "&eChanged &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8'&es nickname to &b'%this_new%&b' &7(&efrom &b'%this_previous%&b'&7)");
        messageResultChanged = getCommandResource().getOrSetDefault("messages.result.changed",
                "&eChanged &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8'&es nickname to &b'%this_new%&b' &7(&efrom &b'%this_previous%&b'&7)");
        messageResultCancelled = getCommandResource().getOrSetDefault("messages.result.cancelled",
                "&cDid not change your nickname because a plugin / module cancelled it.");
        permissionSetOthers = getCommandResource().getOrSetDefault("permission.set.others", "streamline.command.nickname.set.others");
        permissionSetNonFormatted = getCommandResource().getOrSetDefault("permission.set.non-formatted", "streamline.command.nickname.set.non-formatted");
    }

    @Override
    public void run(StreamlineUser streamlineUser, String[] strings) {
        String message = ModuleUtils.argsToString(strings);

        StreamlineUser user = streamlineUser;

        if (message.startsWith("-p:")) {
            if (ModuleUtils.hasPermission(streamlineUser, getPermissionSetOthers())) {
                String name = strings[0].substring("-p:".length());
                user = ModuleUtils.getOrGetUserByName(name);
                message = message.substring(message.indexOf(' ') + 1);
            }
        }

        if (user == null) {
            ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        if (message.equals("")) {
            strings = new String[] { "-clear" };
            message = ModuleUtils.argsToString(strings);
        }

        if (strings.length <= 2) {
            if (strings[strings.length - 1].equals("-clear")) {
                String current = user.getDisplayName();
                NicknameUpdateEvent updateEvent =
                        new NicknameUpdateEvent(user, UserUtils.getFormattedDefaultNickname(user), current);
                ModuleUtils.fireEvent(updateEvent);
                if (updateEvent.isCancelled()) {
                    ModuleUtils.sendMessage(streamlineUser, getWithOther(streamlineUser, getMessageResultCancelled(), user)
                            .replace("%this_new%", message)
                            .replace("%this_previous%", current)
                    );
                    return;
                }

                user.setDisplayName(updateEvent.getChangeTo());
                if (user instanceof StreamlinePlayer) {
                    StreamlinePlayer player = (StreamlinePlayer) user;
                    SpigotAccessor.updateCustomName(player, updateEvent.getChangeTo());
                    if (SLAPI.getInstance().getPlatform().getServerType().equals(IStreamline.ServerType.PROXY)) {
                        ProxiedMessage proxiedMessage = SavablePlayerMessageBuilder.build(player, true);
                        proxiedMessage.setServer(player.getLatestServer());
                        SLAPI.getInstance().getProxyMessenger().sendMessage(proxiedMessage);
                    }
                }

                ModuleUtils.sendMessage(streamlineUser, getWithOther(streamlineUser, getMessageResultCleared(), user)
                        .replace("%this_new%", ModuleUtils.replaceAllPlayerBungee(user, "%streamline_user_formatted%"))
                        .replace("%this_previous%", current)
                );
                return;
            }
        }

        if (message.startsWith("!") && ModuleUtils.hasPermission(streamlineUser, getPermissionSetNonFormatted())) {
            message = message.substring("!".length());
        } else {
            message = StreamlineUtilities.getConfigs().getNicknamesFormat().replace("%this_input%", message);
        }

        String current = user.getDisplayName();
        NicknameUpdateEvent updateEvent = new NicknameUpdateEvent(user, message, current);
        ModuleUtils.fireEvent(updateEvent);
        if (updateEvent.isCancelled()) {
            ModuleUtils.sendMessage(streamlineUser, getWithOther(streamlineUser, getMessageResultCancelled(), user)
                    .replace("%this_new%", message)
                    .replace("%this_previous%", current)
            );
            return;
        }

        user.setDisplayName(updateEvent.getChangeTo());
        if (user instanceof StreamlinePlayer) {
            StreamlinePlayer player = (StreamlinePlayer) user;
            SpigotAccessor.updateCustomName(player, updateEvent.getChangeTo());
            if (SLAPI.getInstance().getPlatform().getServerType().equals(IStreamline.ServerType.PROXY)) {
                ProxiedMessage proxiedMessage = SavablePlayerMessageBuilder.build(player, true);
                proxiedMessage.setServer(player.getLatestServer());
                SLAPI.getInstance().getProxyMessenger().sendMessage(proxiedMessage);
            }
        }

        ModuleUtils.sendMessage(streamlineUser, getWithOther(streamlineUser, getMessageResultChanged(), user)
                .replace("%this_new%", message)
                .replace("%this_previous%", current)
        );
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length == 1) {
            if (strings[0].startsWith("-p:")) {
                ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();

                ModuleUtils.getOnlinePlayerNames().forEach(s -> {
                    r.add("-p:" + s);
                    r.add("!" + s);
                });
                r.add("-clear");
                r.add("!");

                return r;
            }

            ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();

            r.addAll(ModuleUtils.getOnlinePlayerNames());

            ModuleUtils.getOnlinePlayerNames().forEach(s -> {
                r.add("!" + s);
            });

            r.add("-clear");
            r.add("!");

            return r;
        }

        if (strings.length == 2) {
            if (strings[0].startsWith("-p:")) {
                ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();

                r.addAll(ModuleUtils.getOnlinePlayerNames());
                r.add("-clear");

                return r;
            }
        }

        if (strings.length >= 2) {
            if (strings[strings.length - 2].equals("-clear")) {
                return new ConcurrentSkipListSet<>();
            }
        }

        return ModuleUtils.getOnlinePlayerNames();
    }
}
