package tv.quaint.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class TeleportCommand extends ModuleCommand {
    @Getter
    private final String messageResult;

    public TeleportCommand() {
        super(StreamlineUtilities.getInstance(),
                "pteleport",
                "streamline.command.teleport.default",
                "ptele", "ptp"
        );

        messageResult = getCommandResource().getOrSetDefault("messages.result", "&eTeleported to %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8!");
    }

    @Override
    public void run(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length < 1) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        } else if (strings.length > 1) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
            return;
        }

        String username = strings[0];
        StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
        if (other == null) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        ModuleUtils.connect(StreamlineUser, other.getLatestServer());
        ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResult, other));
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}
