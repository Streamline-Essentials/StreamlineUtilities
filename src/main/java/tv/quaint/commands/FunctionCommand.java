package tv.quaint.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineUtilities;
import tv.quaint.executables.ExecutableHandler;
import tv.quaint.executables.functions.StreamFunction;

import java.util.List;

public class FunctionCommand extends ModuleCommand {
    private final String messageResultSuccess;
    private final String messageFunctionNotFound;
    private final String messageResultReload;

    public FunctionCommand() {
        super(StreamlineUtilities.getInstance(),
                "pfunction",
                "streamline.command.function",
                "pf", "streamlinefunction", "sf", "streamfunction"
        );

        this.messageResultSuccess = this.getCommandResource().getOrSetDefault("messages.result.success", "&eJust ran the function &7'&b%this_identifier%&7' &eon &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8!");
        this.messageFunctionNotFound = this.getCommandResource().getOrSetDefault("messages.function.not.found", "&cThe function '%this_identifier%' is not enabled!");
        this.messageResultReload = this.getCommandResource().getOrSetDefault("messages.result.reload", "&eJust reloaded &a%this_amount% &efunctions&8!");
    }

    @Override
    public void run(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length == 1) {
            String arg = strings[0];
            if (arg.equals("reload")) {
                int reloaded = ExecutableHandler.reloadFunctions();
                ModuleUtils.sendMessage(StreamlineUser, messageResultReload.replace("%this_amount%", String.valueOf(reloaded)));
                return;
            }
        }

        if (strings.length < 2) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }

        String username = strings[0];

        StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
        if (other == null) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        for (String identifier : ModuleUtils.argsMinus(strings, 0)) {
            StreamFunction function = ExecutableHandler.getEnabledFunction(identifier);
            if (function == null) {
                ModuleUtils.sendMessage(StreamlineUser, messageFunctionNotFound
                        .replace("%this_identifier%", identifier)
                        .replace("%this_other%", other.getName())
                );
                return;
            }

            function.runAs(other);

            ModuleUtils.sendMessage(StreamlineUser, messageResultSuccess
                    .replace("%this_identifier%", identifier)
                    .replace("%this_other%", other.getName())
            );
        }
    }

    @Override
    public List<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length <= 1) {
            List<String> r = ModuleUtils.getOnlinePlayerNames();
            r.add("reload");
            return r;
        }

        return ExecutableHandler.getEnabledFunctionIdentifiers();
    }
}
