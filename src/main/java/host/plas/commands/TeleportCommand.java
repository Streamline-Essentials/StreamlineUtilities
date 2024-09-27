package host.plas.commands;

import lombok.Getter;
import singularity.command.ModuleCommand;
import singularity.configs.given.MainMessagesHandler;
import singularity.messages.builders.TeleportMessageBuilder;
import singularity.messages.proxied.ProxiedMessage;
import singularity.modules.ModuleUtils;
import singularity.data.players.CosmicPlayer;
import singularity.data.console.CosmicSender;
import singularity.utils.UserUtils;
import host.plas.StreamlineUtilities;
import host.plas.essentials.EssentialsManager;

import java.util.concurrent.ConcurrentSkipListSet;

@Getter
public class TeleportCommand extends ModuleCommand {
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
    public void run(CosmicSender sender, String[] strings) {
        if (! (sender instanceof CosmicPlayer)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
            return;
        }
        CosmicPlayer player = (CosmicPlayer) sender;

        if (strings[0].equals("")) {
            ModuleUtils.sendMessage(player, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        } else if (strings.length > 1) {
            ModuleUtils.sendMessage(player, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
            return;
        }

        String username = strings[0];
        CosmicPlayer other = UserUtils.getOrCreatePlayerByName(username).orElse(null);
        if (other == null) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        ModuleUtils.connect(sender, other.getServerName());

        ProxiedMessage message = TeleportMessageBuilder.build(player, other.getLocation(), player);
        new EssentialsManager.TeleportRunner(message);

        ModuleUtils.sendMessage(sender, getWithOther(sender, messageResult, other));
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(CosmicSender CosmicSender, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}
