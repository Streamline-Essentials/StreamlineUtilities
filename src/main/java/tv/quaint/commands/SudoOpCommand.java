package tv.quaint.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.OperatorUser;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineUtilities;

import java.util.concurrent.ConcurrentSkipListSet;

public class SudoOpCommand extends ModuleCommand {
    @Getter
    private final String messageResult;

    public SudoOpCommand() {
        super(StreamlineUtilities.getInstance(),
                "proxysudoop",
                "streamline.command.sudo-op.default",
                "pso", "psudoop", "psuop"
        );

        messageResult = getCommandResource().getOrSetDefault("messages.result.sudoer", "&eYou have successfully ran the command &f%this_input%&e as &f%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &ewith operator privileges&8!");
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

        OperatorUser operatorUser = new OperatorUser(other);

        ModuleUtils.runAs(operatorUser, command);
        ModuleUtils.sendMessage(sender, getWithOther(sender, messageResult, other));
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}
