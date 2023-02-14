package tv.quaint.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineUtilities;
import tv.quaint.essentials.EssentialsManager;
import tv.quaint.essentials.TPARequest;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

public class TPACommand extends ModuleCommand {
    @Getter
    private final String messageResultSentTo;
    @Getter
    private final String messageResultSentFrom;
    @Getter
    private final String messageResultAcceptedTo;
    @Getter
    private final String messageResultAcceptedFrom;
    @Getter
    private final String messageResultDeniedTo;
    @Getter
    private final String messageResultDeniedFrom;
    @Getter
    private final String messageResultNonePendingSpecific;
    @Getter
    private final String messageResultNonePendingAll;

    public TPACommand() {
        super(StreamlineUtilities.getInstance(),
                "ptpa",
                "streamline.command.tpa.default",
                "pteleportask"
        );

        messageResultSentTo = getCommandResource().getOrSetDefault("messages.result.sent.to", "&eSent &d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*% &ea tpa request&8!");
        messageResultSentFrom = getCommandResource().getOrSetDefault("messages.result.sent.from", "&d%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*% &ewants to tpa to you&8!" +
                "&eType &7'&a/ptpa accept %this_from%&7' &eto accept&8!");

        messageResultAcceptedTo = getCommandResource().getOrSetDefault("messages.result.accepted.to",
                "&eAccepted &d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*%&e's tpa request&8!");
        messageResultAcceptedFrom = getCommandResource().getOrSetDefault("messages.result.accepted.from",
                "&d%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*% &ehas accepted your tpa request&8!");

        messageResultDeniedTo = getCommandResource().getOrSetDefault("messages.result.denied.to",
                "&eDenied &d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*%&e's tpa request&8!");
        messageResultDeniedFrom = getCommandResource().getOrSetDefault("messages.result.denied.from",
                "&d%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*% &ehas denied your tpa request&8!");

        messageResultNonePendingSpecific = getCommandResource().getOrSetDefault("messages.result.none_pending.specific",
                "&eYou have no pending tpa requests from &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8!");
        messageResultNonePendingAll = getCommandResource().getOrSetDefault("messages.result.none_pending.all",
                "&eYou have no pending tpa requests&8!");
    }

    @Override
    public void run(StreamlineUser sender, String[] strings) {
        if (strings.length < 1) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }

        if (! (sender instanceof StreamlinePlayer)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
            return;
        }
        StreamlinePlayer senderPlayer = (StreamlinePlayer) sender;

        String action = strings[0];

        switch (action) {
            case "request":
                if (strings.length < 2) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                    return;
                }
                String username = strings[1];
                StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
                if (other == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                if (! (other instanceof StreamlinePlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }
                StreamlinePlayer otherPlayer = (StreamlinePlayer) other;

                if (otherPlayer.getLocation() == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                } else {
                    if (otherPlayer.getLocation().isNull()) {
                        ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                        return;
                    }
                }

                if (senderPlayer.getLocation() == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
                    return;
                } else {
                    if (senderPlayer.getLocation().isNull()) {
                        ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
                        return;
                    }
                }

                EssentialsManager.requestTPA(senderPlayer, otherPlayer);

                ModuleUtils.sendMessage(sender, getWithOther(sender, sender, messageResultSentTo));
                ModuleUtils.sendMessage(other, getWithOther(sender, other, messageResultSentFrom));
                break;
            case "accept":
                String usernameAccept = "";
                if (strings.length > 1) {
                    usernameAccept = strings[1];
                } else {
                    TPARequest request = EssentialsManager.getLatestPendingTPARequest(senderPlayer, TPARequest.TransportType.SENDER_TO_RECEIVER);
                    if (request == null) {
                        ModuleUtils.sendMessage(sender, getWithOther(sender.getUuid(), messageResultNonePendingAll));
                        return;
                    }
                    usernameAccept = request.getSender().getLatestName();
                }

                StreamlineUser otherAccept = ModuleUtils.getOrGetUserByName(usernameAccept);
                if (otherAccept == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                if (! (otherAccept instanceof StreamlinePlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }
                StreamlinePlayer otherPlayerAccept = (StreamlinePlayer) otherAccept;

                TPARequest request = EssentialsManager.getTPARequest(otherPlayerAccept.getUuid(), senderPlayer.getUuid(), TPARequest.TransportType.SENDER_TO_RECEIVER);
                if (request == null) {
                    ModuleUtils.sendMessage(sender, getWithOther(sender, otherAccept, getWithOther(sender, messageResultNonePendingSpecific, otherAccept)));
                    return;
                }

                request.perform();

                ModuleUtils.sendMessage(sender, getWithOther(sender, sender, messageResultAcceptedTo));
                ModuleUtils.sendMessage(otherAccept, getWithOther(sender, otherAccept, messageResultAcceptedFrom));
                break;
            case "deny":
                String usernameDeny = "";
                if (strings.length > 1) {
                    usernameDeny = strings[1];
                } else {
                    TPARequest requestDeny = EssentialsManager.getLatestPendingTPARequest(senderPlayer, TPARequest.TransportType.SENDER_TO_RECEIVER);
                    if (requestDeny == null) {
                        ModuleUtils.sendMessage(sender, getWithOther(sender.getUuid(), messageResultNonePendingAll));
                        return;
                    }
                    usernameDeny = requestDeny.getSender().getLatestName();
                }

                StreamlineUser otherDeny = ModuleUtils.getOrGetUserByName(usernameDeny);
                if (otherDeny == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                if (! (otherDeny instanceof StreamlinePlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }
                StreamlinePlayer otherPlayerDeny = (StreamlinePlayer) otherDeny;

                TPARequest requestDeny = EssentialsManager.getTPARequest(otherPlayerDeny.getUuid(), senderPlayer.getUuid(), TPARequest.TransportType.SENDER_TO_RECEIVER);
                if (requestDeny == null) {
                    ModuleUtils.sendMessage(sender, getWithOther(sender, otherDeny, getWithOther(sender, messageResultNonePendingSpecific, otherDeny)));
                    return;
                }

                requestDeny.deny();

                ModuleUtils.sendMessage(sender, getWithOther(sender, sender, messageResultDeniedTo));
                ModuleUtils.sendMessage(otherDeny, getWithOther(sender, otherDeny, messageResultDeniedFrom));
                break;
            default:
                ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                break;
        }
    }

    public String getWithOther(StreamlineUser from, StreamlineUser to, String message) {
        message = message
                .replace("%this_from%", from.getLatestName())
                .replace("%this_to%", to.getLatestName());
        return getWithOther(from, message, to);
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length <= 1) return new ConcurrentSkipListSet<>(Arrays.asList("request", "accept", "deny"));
        if (strings.length == 2) return ModuleUtils.getOnlinePlayerNames();

        return new ConcurrentSkipListSet<>();
    }
}
