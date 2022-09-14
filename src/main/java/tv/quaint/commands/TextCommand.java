package tv.quaint.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineUtilities;

import java.util.ArrayList;
import java.util.List;

public class TextCommand extends ModuleCommand {
    @Getter
    private final String messageResultSender;
    @Getter
    private final boolean sendResultSenderMessage;

    public TextCommand() {
        super(StreamlineUtilities.getInstance(),
                "proxytext",
                "streamline.command.text.default",
                "ptext"
        );

        messageResultSender = getCommandResource().getOrSetDefault("messages.result.sender.message",
                "&eSent %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &ethis message&8:%newline%&r%this_message%");
        sendResultSenderMessage = getCommandResource().getOrSetDefault("messages.result.sender.enabled", true);
    }

    @Override
    public void run(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length < 2) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }
        String username = strings[0];
        String message = ModuleUtils.argsToStringMinus(strings, 0);

        StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
        if (other == null) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        ModuleUtils.sendMessage(other, message);
        if (sendResultSenderMessage) ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultSender
                .replace("%this_message%", message)
                , other));
    }

    @Override
    public List<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}
