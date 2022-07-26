package tv.quaint.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.SavableUser;
import tv.quaint.StreamlineUtilities;

import java.util.ArrayList;
import java.util.List;

public class BroadcastCommand extends ModuleCommand {
    @Getter
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
    public void run(SavableUser savableUser, String[] strings) {
        String message = ModuleUtils.argsToString(strings);

        boolean isCustom = false;
        if (message.startsWith("!")) {
            isCustom = true;
            message = message.substring(1);
        }

        final String finalMessage = message;
        ModuleUtils.getLoadedUsers().forEach(a -> {
            ModuleUtils.sendMessage(a, messageResultAll
                    .replace("%this_message%", finalMessage)
                    .replace("%this_sender%", savableUser.getName())
            );
        });
    }

    @Override
    public List<String> doTabComplete(SavableUser savableUser, String[] strings) {
        return new ArrayList<>();
    }
}
