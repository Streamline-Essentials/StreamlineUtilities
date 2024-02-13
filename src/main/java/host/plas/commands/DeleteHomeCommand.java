package host.plas.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.UserUtils;
import host.plas.StreamlineUtilities;
import host.plas.essentials.EssentialsManager;
import host.plas.essentials.users.StreamlineHome;
import host.plas.essentials.users.UtilitiesUser;

import java.util.concurrent.ConcurrentSkipListSet;

public class DeleteHomeCommand extends ModuleCommand {
    @Getter
    private final String messageResultRemove;

    @Getter
    private final String messageResultRemoveOther;

    @Getter
    private final String messageResultHomeNotExists;

    @Getter
    private final String permissionRemoveOther;

    public DeleteHomeCommand() {
        super(StreamlineUtilities.getInstance(),
                "streamline-delhome",
                "streamline.command.delhome.default",
                "sdelhome", "stdh"
        );

        messageResultRemove = getCommandResource().getOrSetDefault("messages.result.remove", "&eRemoved set home &7'&c%this_input%&7'&8!");

        messageResultRemoveOther = getCommandResource().getOrSetDefault("messages.result.remove-other", "&eRemoved set home &7'&c%this_input%&7' &efor &7'&c%this_other%&7'&8!");

        messageResultHomeNotExists = getCommandResource().getOrSetDefault("messages.result.home-not-exists", "&cHome &7'&c%this_input%&7' &cdoes not exists&8!");

        permissionRemoveOther = getCommandResource().getOrSetDefault("permissions.delhome.other", "streamline.command.delhome.other");
    }

    @Override
    public void run(StreamlineUser sender, String[] strings) {
        String homeName = "home";
        StreamlineUser targetUser = null;
        UtilitiesUser target = null;

        if (strings.length <= 1) {
            target = EssentialsManager.getOrGetUser(sender);
            targetUser = sender;
        }

        if (strings.length == 1) {
            homeName = strings[0];
        }

        if (strings.length == 2) {
            if (ModuleUtils.hasPermission(sender, permissionRemoveOther)) {
                targetUser = UserUtils.getOrGetUserByName(strings[0]);
                if (targetUser == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                target = EssentialsManager.getOrGetUser(targetUser);
            } else {
                ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PERMISSIONS.get());
                return;
            }
        }

        if (strings.length > 2) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
            return;
        }

        if (target == null) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        if (! (sender instanceof StreamlinePlayer)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
            return;
        }

        StreamlinePlayer player = (StreamlinePlayer) sender;

        StreamlineHome home = target.getHome(homeName);

        if (home == null) {
            ModuleUtils.sendMessage(sender, getWithOther(player, messageResultHomeNotExists
                    .replace("%this_input%", homeName), targetUser)
            );
            return;
        }

        target.removeHome(homeName);

        if (targetUser.equals(sender)) {
            ModuleUtils.sendMessage(sender, getWithOther(player, messageResultRemove
                    .replace("%this_input%", homeName), targetUser)
            );
        } else {
            ModuleUtils.sendMessage(sender, getWithOther(player, messageResultRemoveOther
                    .replace("%this_input%", homeName)
                    .replace("%this_other%", targetUser.getName()), targetUser)
            );
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length == 2) {
            if (ModuleUtils.hasPermission(StreamlineUser, permissionRemoveOther)) {
                return ModuleUtils.getOnlinePlayerNames();
            }
        }

        return new ConcurrentSkipListSet<>();
    }
}
