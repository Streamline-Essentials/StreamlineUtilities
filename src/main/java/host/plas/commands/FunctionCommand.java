package host.plas.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.data.console.StreamSender;
import host.plas.StreamlineUtilities;
import host.plas.executables.ExecutableHandler;
import host.plas.executables.functions.StreamFunction;

import java.util.concurrent.ConcurrentSkipListSet;

@Getter
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
    public void run(StreamSender StreamSender, String[] strings) {
        if (strings.length == 1) {
            String arg = strings[0];
            if (arg.equals("reload")) {
                int reloaded = ExecutableHandler.reloadFunctions();
                ModuleUtils.sendMessage(StreamSender, messageResultReload.replace("%this_amount%", String.valueOf(reloaded)));
                return;
            }
        }

        if (strings.length < 2) {
            ModuleUtils.sendMessage(StreamSender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }

        String username = strings[0];

        StreamSender other = ModuleUtils.getOrGetUserByName(username).orElse(null);
        if (other == null) {
            ModuleUtils.sendMessage(StreamSender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        for (String identifier : ModuleUtils.argsMinus(strings, 0)) {
            StreamFunction function = ExecutableHandler.getEnabledFunction(identifier);
            if (function == null) {
                ModuleUtils.sendMessage(StreamSender, messageFunctionNotFound
                        .replace("%this_identifier%", identifier)
                        .replace("%this_other%", other.getCurrentName())
                );
                return;
            }

            function.runAs(other);

            ModuleUtils.sendMessage(StreamSender, messageResultSuccess
                    .replace("%this_identifier%", identifier)
                    .replace("%this_other%", other.getCurrentName())
            );
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamSender StreamSender, String[] strings) {
        if (strings.length <= 1) {
            ConcurrentSkipListSet<String> r = ModuleUtils.getOnlinePlayerNames();
            r.add("reload");
            return r;
        }

        return ExecutableHandler.getEnabledFunctionIdentifiers();
    }
}
