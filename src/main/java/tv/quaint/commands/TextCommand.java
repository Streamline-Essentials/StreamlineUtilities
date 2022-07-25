package tv.quaint.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.SavableUser;
import net.streamline.base.configs.MainMessagesHandler;
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
    public void run(SavableUser savableUser, String[] strings) {
        if (strings.length < 2) {
            ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }
        String username = strings[0];
        String message = ModuleUtils.argsToStringMinus(strings, 0);

        SavableUser other = ModuleUtils.getOrGetUserByName(username);
        if (other == null) {
            ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        ModuleUtils.sendMessage(other, message);
        if (sendResultSenderMessage) ModuleUtils.sendMessage(savableUser, getWithOther(savableUser, messageResultSender
                .replace("%this_message%", message)
                , other));
    }

    @Override
    public List<String> doTabComplete(SavableUser savableUser, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}
