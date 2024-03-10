package host.plas.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.data.console.StreamSender;
import host.plas.StreamlineUtilities;

import java.util.concurrent.ConcurrentSkipListSet;

@Getter
public class TextCommand extends ModuleCommand {
    private final String messageResultSender;
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
    public void run(StreamSender StreamSender, String[] strings) {
        if (strings.length < 2) {
            ModuleUtils.sendMessage(StreamSender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }
        String username = strings[0];
        String message = ModuleUtils.argsToStringMinus(strings, 0);

        StreamSender other = ModuleUtils.getOrGetUserByName(username).orElse(null);
        if (other == null) {
            ModuleUtils.sendMessage(StreamSender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        ModuleUtils.sendMessage(other, message);
        if (sendResultSenderMessage) ModuleUtils.sendMessage(StreamSender, getWithOther(StreamSender, messageResultSender
                .replace("%this_message%", message)
                , other));
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamSender StreamSender, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}
