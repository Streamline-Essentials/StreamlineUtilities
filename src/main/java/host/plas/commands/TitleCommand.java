package host.plas.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.objects.StreamlineTitle;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.MessageUtils;
import host.plas.StreamlineUtilities;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class TitleCommand extends ModuleCommand {
    @Getter
    private final String messageResultSender;
    @Getter
    private final String messageErrorNotPlayer;
    @Getter
    private final boolean sendResultSenderMessage;
    @Getter
    private final String subtitleSplitter;
    @Getter
    private final long subtitleDefaultFadeIn;
    @Getter
    private final long subtitleDefaultStay;
    @Getter
    private final long subtitleDefaultFadeOut;

    public TitleCommand() {
        super(StreamlineUtilities.getInstance(),
                "proxytitle",
                "streamline.command.title.default",
                "ptitle"
        );

        messageResultSender = getCommandResource().getOrSetDefault("messages.result.sender.message",
                "&eSent %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &ethis title&8:%newline%&r%this_title%%newline%%this_subtitle%");
        sendResultSenderMessage = getCommandResource().getOrSetDefault("messages.result.sender.enabled", true);

        messageErrorNotPlayer = getCommandResource().getOrSetDefault("messages.error.not-player", "&cThat user is not a player!");

        subtitleSplitter = getCommandResource().getOrSetDefault("title.splitter", "\\n");
        subtitleDefaultFadeIn = getCommandResource().getOrSetDefault("title.default.fade-in", 100L);
        subtitleDefaultStay = getCommandResource().getOrSetDefault("title.default.stay", 100L);
        subtitleDefaultFadeOut = getCommandResource().getOrSetDefault("title.default.fade-out", 100L);
    }

    @Override
    public void run(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length < 5) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }
        String username = strings[0];
        long fadeIn, stay, fadeOut;
        try {
            fadeIn = Long.parseLong(strings[1]);
        } catch (Exception e) {
            fadeIn = subtitleDefaultFadeIn;
        }
        try {
            stay = Long.parseLong(strings[2]);
        } catch (Exception e) {
            stay = subtitleDefaultStay;
        }
        try {
            fadeOut = Long.parseLong(strings[3]);
        } catch (Exception e) {
            fadeOut = subtitleDefaultFadeOut;
        }
        String message = ModuleUtils.argsToStringMinus(strings, 0, 1, 2, 3);

        String[] parts = message.split(subtitleSplitter, 2);
        String title, sub;
        if (parts.length <= 0) {
            title = "";
            sub = "";
        } else if (parts.length == 1) {
            MessageUtils.logInfo("");
            title = parts[0];
            sub = "";
        } else {
            title = parts[0];
            sub = parts[1];
        }

        StreamlineTitle streamlineTitle = new StreamlineTitle(title, sub);
        streamlineTitle.setFadeIn(fadeIn);
        streamlineTitle.setStay(stay);
        streamlineTitle.setFadeOut(fadeOut);

        StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
        if (other == null) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }
        if (! (other instanceof StreamlinePlayer)) {
            ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageErrorNotPlayer, other));
            return;
        }
        StreamlinePlayer player = (StreamlinePlayer) other;

        ModuleUtils.sendTitle(player, streamlineTitle);
        if (sendResultSenderMessage) ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultSender
                        .replace("%this_title%", title)
                        .replace("%this_subtitle%", sub)
                , other));
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        ConcurrentSkipListSet<String> online = ModuleUtils.getOnlinePlayerNames();

        if (strings.length <= 1) return online;
        if (strings.length == 2) return new ConcurrentSkipListSet<>(List.of("<fade-in>"));
        if (strings.length == 3) return new ConcurrentSkipListSet<>(List.of("<stay>"));
        if (strings.length == 4) return new ConcurrentSkipListSet<>(List.of("<fade-out>"));
        String message = ModuleUtils.argsToStringMinus(strings, 0, 1, 2, 3);
        if (! message.contains(subtitleSplitter)) online.add(subtitleSplitter);

        return online;
    }
}
