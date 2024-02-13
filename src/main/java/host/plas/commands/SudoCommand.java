package host.plas.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import host.plas.StreamlineUtilities;

import java.util.concurrent.ConcurrentSkipListSet;

public class SudoCommand extends ModuleCommand {
    @Getter
    private final String messageResult;

    public SudoCommand() {
        super(StreamlineUtilities.getInstance(),
                "proxysudo",
                "streamline.command.sudo.default",
                "ps", "psudo", "psu"
        );

        messageResult = getCommandResource().getOrSetDefault("messages.result.sudoer", "&eYou have successfully ran the command &f%this_input%&e as &f%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8!");
    }

    @Override
    public void run(StreamlineUser sender, String[] strings) {
        if (strings.length < 2) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }

        String username = strings[0];
        String command = ModuleUtils.argsToStringMinus(strings, 0);
        StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
        if (other == null) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        ModuleUtils.runAs(other, command);
        ModuleUtils.sendMessage(sender, getWithOther(sender, messageResult, other));
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}
