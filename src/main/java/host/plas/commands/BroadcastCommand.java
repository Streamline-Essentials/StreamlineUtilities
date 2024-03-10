package host.plas.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.data.console.StreamSender;
import net.streamline.api.modules.ModuleUtils;
import host.plas.StreamlineUtilities;

import java.util.concurrent.ConcurrentSkipListSet;

@Getter
public class BroadcastCommand extends ModuleCommand {
    private final String messageResultAll;

    public BroadcastCommand() {
        super(StreamlineUtilities.getInstance(),
                "proxybroadcast",
                "streamline.command.broadcast.default",
                "pb", "proxyb", "pbroadcast"
        );

        messageResultAll = getCommandResource().getOrSetDefault("messages.result.all", "&c&lBROADCAST &7&l>> &r%this_message%");
    }

    @Override
    public void run(StreamSender StreamSender, String[] strings) {
        String message = ModuleUtils.argsToString(strings);

        boolean isCustom = false;
        if (message.startsWith("!")) {
            isCustom = true;
            message = message.substring(1);
        }

        final String finalMessage = message;
        boolean finalIsCustom = isCustom;
        ModuleUtils.getLoadedSendersSet().forEach(a -> {
            if (! finalIsCustom) {
                ModuleUtils.sendMessage(a, messageResultAll
                        .replace("%this_message%", finalMessage)
                        .replace("%this_sender%", StreamSender.getCurrentName())
                );
            } else {
                ModuleUtils.sendMessage(a, finalMessage
                        .replace("%this_sender%", StreamSender.getCurrentName())
                );
            }
        });
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamSender StreamSender, String[] strings) {
        return new ConcurrentSkipListSet<>();
    }
}
