package tv.quaint.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineUtilities;
import tv.quaint.essentials.EssentialsManager;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

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

    public TPAHereCommand() {
        super(StreamlineUtilities.getInstance(),
                "ptpahere",
                "streamline.command.tpahere.default",
                "pteleportaskhere"
        );

        messageResultSentTo = getCommandResource().getOrSetDefault("messages.result.sent.to", "&eSent &d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*% &ea tpahere request&8!");
        messageResultSentFrom = getCommandResource().getOrSetDefault("messages.result.sent.from", "&d%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*% &ewants to tpahere to you&8!" +
                "&eType &7'&a/ptpahere accept %this_from%&7' &eto accept&8!");

        messageResultAcceptedTo = getCommandResource().getOrSetDefault("messages.result.accepted.to",
                "&eAccepted &d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*%&e's tpahere request&8!");
        messageResultAcceptedFrom = getCommandResource().getOrSetDefault("messages.result.accepted.from",
                "&d%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*% &ehas accepted your tpahere request&8!");

        messageResultDeniedTo = getCommandResource().getOrSetDefault("messages.result.denied.to",
                "&eDenied &d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*%&e's tpahere request&8!");
        messageResultDeniedFrom = getCommandResource().getOrSetDefault("messages.result.denied.from",
                "&d%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*% &ehas denied your tpahere request&8!");
    }

    @Override
    public void run(StreamlineUser sender, String[] strings) {
        if (strings.length < 2) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }

        String action = strings[0];
        String username = strings[1];

        switch (action) {
            case "request" -> {
                StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
                if (other == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                if (! (other instanceof StreamlinePlayer otherPlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }

                if (otherPlayer.getLocation() == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }

                if (! (sender instanceof StreamlinePlayer senderPlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
                    return;
                }

                if (senderPlayer.getLocation() == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
                    return;
                }

                EssentialsManager.requestTPAHere(senderPlayer, otherPlayer);

                ModuleUtils.sendMessage(sender, getWithOther(sender, other, messageResultSentTo));
                ModuleUtils.sendMessage(other, getWithOther(sender, other, messageResultSentFrom));
            }
            case "accept" -> {
                StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
                if (other == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                if (! (other instanceof StreamlinePlayer otherPlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }

                if (! (sender instanceof StreamlinePlayer senderPlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
                    return;
                }

                EssentialsManager.acceptTPA(otherPlayer, senderPlayer);

                ModuleUtils.sendMessage(sender, getWithOther(other, sender, messageResultAcceptedTo));
                ModuleUtils.sendMessage(other, getWithOther(other, sender, messageResultAcceptedFrom));
            }
            case "deny" -> {
                StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
                if (other == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                if (! (other instanceof StreamlinePlayer otherPlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                    return;
                }

                if (! (sender instanceof StreamlinePlayer senderPlayer)) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
                    return;
                }

                EssentialsManager.denyTPA(otherPlayer, senderPlayer);

                ModuleUtils.sendMessage(sender, getWithOther(other, sender, messageResultDeniedTo));
                ModuleUtils.sendMessage(other, getWithOther(other, sender, messageResultDeniedFrom));
            }
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
