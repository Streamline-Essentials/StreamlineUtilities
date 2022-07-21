package tv.quaint.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.BundledModule;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.SavableUser;
import net.streamline.base.configs.MainMessagesHandler;
import net.streamline.utils.UUIDUtils;
import tv.quaint.StreamlineUtilities;
import tv.quaint.executables.functions.FunctionHandler;
import tv.quaint.executables.functions.StreamlineFunction;

import java.util.List;

public class FunctionCommand extends ModuleCommand {
    private final String messageResult;
    private final String messageFunctionNotFound;

    public FunctionCommand() {
        super(StreamlineUtilities.getInstance(),
                "pfunction",
                "streamline.command.function",
                "pf", "streamlinefunction", "sf", "streamfunction"
        );

        this.messageResult = this.getCommandResource().getOrSetDefault("messages.result", "&eJust ran the function &7'&b%this_identifier%&7' &eon &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8!");
        this.messageFunctionNotFound = this.getCommandResource().getOrSetDefault("messages.function.not.found", "&cThe function '%this_identifier%' is not enabled!");
    }

    @Override
    public void run(SavableUser savableUser, String[] strings) {
        if (strings.length < 2) {
            ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }

        String username = strings[0];

        SavableUser other = ModuleUtils.getOrGetUserByName(username);
        if (other == null) {
            ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        for (String identifier : ModuleUtils.argsMinus(strings, 0)) {
            StreamlineFunction function = FunctionHandler.getEnabledFunction(identifier);
            if (function == null) {
                ModuleUtils.sendMessage(savableUser, messageFunctionNotFound
                        .replace("%this_identifier%", identifier)
                        .replace("%this_other%", other.getName())
                );
                return;
            }

            function.runAs(other);

            ModuleUtils.sendMessage(savableUser, messageResult
                    .replace("%this_identifier%", identifier)
                    .replace("%this_other%", other.getName())
            );
        }
    }

    @Override
    public List<String> doTabComplete(SavableUser savableUser, String[] strings) {
        if (strings.length <= 1) {
            return ModuleUtils.getOnlinePlayerNames();
        }

        return FunctionHandler.getEnabledFunctionIdentifiers();
    }
}
