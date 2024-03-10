package host.plas.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.data.console.StreamSender;
import net.streamline.api.data.players.StreamPlayer;
import net.streamline.api.data.players.location.PlayerLocation;
import net.streamline.api.modules.ModuleUtils;
import host.plas.StreamlineUtilities;
import host.plas.essentials.EssentialsManager;
import host.plas.essentials.TPARequest;
import host.plas.essentials.configured.ConfiguredBlacklist;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class TPAHereCommand extends ModuleCommand {
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
    @Getter
    private final String messageResultBlacklistedServerFrom;
    @Getter
    private final String messageResultBlacklistedWorldFrom;


    public TPAHereCommand() {
        super(StreamlineUtilities.getInstance(),
                "ptpahere",
                "streamline.command.tpahere.default",
                "pteleportaskhere"
        );

        messageResultSentTo = getCommandResource().getOrSetDefault("messages.result.sent.to", "&eSent &d%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*% &ea tpahere request&8!");
        messageResultSentFrom = getCommandResource().getOrSetDefault("messages.result.sent.from", "&d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*% &ewants to tpahere to you&8!" +
                "&eType &7'&a/ptpahere accept %this_from%&7' &eto accept&8!");

        messageResultAcceptedTo = getCommandResource().getOrSetDefault("messages.result.accepted.to",
                "&eAccepted &d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*%&e's tpahere request&8!");
        messageResultAcceptedFrom = getCommandResource().getOrSetDefault("messages.result.accepted.from",
                "&d%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*% &ehas accepted your tpahere request&8!");

        messageResultDeniedTo = getCommandResource().getOrSetDefault("messages.result.denied.to",
                "&eDenied &d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*%&e's tpahere request&8!");
        messageResultDeniedFrom = getCommandResource().getOrSetDefault("messages.result.denied.from",
                "&d%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*% &ehas denied your tpahere request&8!");

        messageResultNonePendingSpecific = getCommandResource().getOrSetDefault("messages.result.none_pending.specific",
                "&eYou have no pending tpahere requests from &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8!");
        messageResultNonePendingAll = getCommandResource().getOrSetDefault("messages.result.none_pending.all",
                "&eYou have no pending tpahere requests&8!");

        messageResultBlacklistedServerFrom = getCommandResource().getOrSetDefault("messages.result.blocked.server.from",
                "&eYou cannot tpahere to &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &ebecause you are on a disallowed server&8!");
        messageResultBlacklistedWorldFrom = getCommandResource().getOrSetDefault("messages.result.blocked.world.from",
                "&eYou cannot tpahere to &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &ebecause you are in a disallowed world&8!");
    }

    @Override
    public void run(StreamSender sender, String[] strings) {
        if (strings.length < 1) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }

        if (! (sender instanceof StreamPlayer)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
            return;
        }
        StreamPlayer senderPlayer = (StreamPlayer) sender;

        String action = strings[0];

        switch (action) {
            case "request":
                if (strings.length < 2) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                    return;
                }
                String username = strings[1];
                StreamSender other = ModuleUtils.getOrGetUserByName(username).orElse(null);
                if (other == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                if (! (other instanceof StreamPlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }
                StreamPlayer otherPlayer = (StreamPlayer) other;

                if (otherPlayer.getLocation() == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }

                if (senderPlayer.getLocation() == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
                    return;
                }

                PlayerLocation senderLocation = senderPlayer.getLocation();
                PlayerLocation otherLocation = otherPlayer.getLocation();

                ConfiguredBlacklist configuredBlacklist = StreamlineUtilities.getConfigs().getTPABlacklist();

                if (configuredBlacklist != null) {
                    AtomicBoolean isServerBlacklisted = new AtomicBoolean(false);
                    configuredBlacklist.getServers().forEach(server -> {
                        if (configuredBlacklist.isAsWhitelist()) {
                            if (! server.equals(senderPlayer.getServerName())) {
                                isServerBlacklisted.set(true);
                            }
                        } else {
                            if (server.equals(senderPlayer.getServerName())) {
                                isServerBlacklisted.set(true);
                            }
                        }
                    });
                    AtomicBoolean isWorldBlacklisted = new AtomicBoolean(false);
                    configuredBlacklist.getWorlds().forEach(world -> {
                        if (configuredBlacklist.isAsWhitelist()) {
                            if (! world.equals(senderLocation.getWorld())) {
                                isWorldBlacklisted.set(true);
                            }
                        } else {
                            if (world.equals(senderLocation.getWorld())) {
                                isWorldBlacklisted.set(true);
                            }
                        }
                    });

                    if (isServerBlacklisted.get()) {
                        ModuleUtils.sendMessage(sender, getWithOther(sender, messageResultBlacklistedServerFrom, otherPlayer));
                        return;
                    }

                    if (isWorldBlacklisted.get()) {
                        ModuleUtils.sendMessage(sender, getWithOther(sender, messageResultBlacklistedWorldFrom, otherPlayer));
                        return;
                    }
                }

                EssentialsManager.requestTPAHere(senderPlayer, otherPlayer);

                ModuleUtils.sendMessage(sender, getWithOther(sender, sender, messageResultSentTo));
                ModuleUtils.sendMessage(other, getWithOther(sender, other, messageResultSentFrom));
                break;
            case "accept":
                String usernameAccept = "";
                if (strings.length > 1) {
                    usernameAccept = strings[1];
                } else {
                    TPARequest request = EssentialsManager.getLatestPendingTPARequest(senderPlayer, TPARequest.TransportType.RECEIVER_TO_SENDER);
                    if (request == null) {
                        ModuleUtils.sendMessage(sender, getWithOther(sender.getUuid(), messageResultNonePendingAll));
                        return;
                    }
                    usernameAccept = request.getSender().getCurrentName();
                }

                StreamSender otherAccept = ModuleUtils.getOrGetUserByName(usernameAccept).orElse(null);
                if (otherAccept == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                if (! (otherAccept instanceof StreamPlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }
                StreamPlayer otherPlayerAccept = (StreamPlayer) otherAccept;

                TPARequest request = EssentialsManager.getTPARequest(otherPlayerAccept.getUuid(), senderPlayer.getUuid(), TPARequest.TransportType.RECEIVER_TO_SENDER);
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
                    TPARequest requestDeny = EssentialsManager.getLatestPendingTPARequest(senderPlayer, TPARequest.TransportType.RECEIVER_TO_SENDER);
                    if (requestDeny == null) {
                        ModuleUtils.sendMessage(sender, getWithOther(sender.getUuid(), messageResultNonePendingAll));
                        return;
                    }
                    usernameDeny = requestDeny.getSender().getCurrentName();
                }

                StreamSender otherDeny = ModuleUtils.getOrGetUserByName(usernameDeny).orElse(null);
                if (otherDeny == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                if (! (otherDeny instanceof StreamPlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }
                StreamPlayer otherPlayerDeny = (StreamPlayer) otherDeny;

                TPARequest requestDeny = EssentialsManager.getTPARequest(otherPlayerDeny.getUuid(), senderPlayer.getUuid(), TPARequest.TransportType.RECEIVER_TO_SENDER);
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

    public String getWithOther(StreamSender from, StreamSender to, String message) {
        message = message
                .replace("%this_from%", from.getCurrentName())
                .replace("%this_to%", to.getCurrentName());
        return getWithOther(from, message, to);
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamSender StreamSender, String[] strings) {
        if (strings.length <= 1) return new ConcurrentSkipListSet<>(Arrays.asList("request", "accept", "deny"));
        if (strings.length == 2) return ModuleUtils.getOnlinePlayerNames();

        return new ConcurrentSkipListSet<>();
    }
}
