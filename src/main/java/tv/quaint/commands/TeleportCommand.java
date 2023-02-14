package tv.quaint.commands;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.interfaces.ModuleLike;
import net.streamline.api.messages.builders.TeleportMessageBuilder;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.scheduler.ModuleDelayedRunnable;
import net.streamline.api.utils.UserUtils;
import tv.quaint.StreamlineUtilities;
import tv.quaint.essentials.EssentialsManager;

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
    public void run(StreamlineUser sender, String[] strings) {
        if (! (sender instanceof StreamlinePlayer)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
            return;
        }
        StreamlinePlayer player = (StreamlinePlayer) sender;

        if (strings[0].equals("")) {
            ModuleUtils.sendMessage(player, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        } else if (strings.length > 1) {
            ModuleUtils.sendMessage(player, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
            return;
        }

        String username = strings[0];
        StreamlinePlayer other = UserUtils.getOrGetPlayerByName(username);
        if (other == null) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        ModuleUtils.connect(sender, other.getLatestServer());

        ProxiedMessage message = TeleportMessageBuilder.build(player, other.getLocation(), player);
        new EssentialsManager.TeleportRunner(message);

        ModuleUtils.sendMessage(sender, getWithOther(sender, messageResult, other));
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}
