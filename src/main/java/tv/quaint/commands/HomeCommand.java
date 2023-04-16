package tv.quaint.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.UserUtils;
import tv.quaint.StreamlineUtilities;
import tv.quaint.essentials.EssentialsManager;
import tv.quaint.essentials.configured.ConfiguredBlacklist;
import tv.quaint.essentials.users.StreamlineHome;
import tv.quaint.essentials.users.UtilitiesUser;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeCommand extends ModuleCommand {
    @Getter
    private final String messageResult;

    @Getter
    private final String messageResultOther;

    @Getter
    private final String messageResultHomeNotExists;
    @Getter
    private final String messageResultBlacklistedServer;
    @Getter
    private final String messageResultBlacklistedWorld;

    @Getter
    private final String permissionOther;

    public HomeCommand() {
        super(StreamlineUtilities.getInstance(),
                "streamline-home",
                "streamline.command.home.default",
                "shome", "sth"
        );

        messageResult = getCommandResource().getOrSetDefault("messages.result.home", "&eTeleporting to home &7'&c%this_input%&7'&8!");

        messageResultOther = getCommandResource().getOrSetDefault("messages.result.home-other", "&eTeleporting to home &7'&c%this_input%&7' &efor &7'&c%this_other%&7'&8!");

        messageResultHomeNotExists = getCommandResource().getOrSetDefault("messages.result.home-not-exists", "&cHome &7'&c%this_input%&7' &cdoes not exists&8!");

        messageResultBlacklistedServer = getCommandResource().getOrSetDefault("messages.result.blocked.server", "&cYou cannot teleport to this server&8!");

        messageResultBlacklistedWorld = getCommandResource().getOrSetDefault("messages.result.blocked.world", "&cYou cannot teleport to this world&8!");

        permissionOther = getCommandResource().getOrSetDefault("permissions.home.other", "streamline.command.home.other");
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
            if (ModuleUtils.hasPermission(sender, permissionOther)) {
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

        ConfiguredBlacklist configuredBlacklist = StreamlineUtilities.getConfigs().getHomesBlacklist();

        if (configuredBlacklist != null) {
            AtomicBoolean isServerBlacklisted = new AtomicBoolean(false);
            configuredBlacklist.getServers().forEach(server -> {
                if (configuredBlacklist.isAsWhitelist()) {
                    if (!server.equals(home.getServer())) {
                        isServerBlacklisted.set(true);
                    }
                } else {
                    if (server.equals(home.getServer())) {
                        isServerBlacklisted.set(true);
                    }
                }
            });
            AtomicBoolean isWorldBlacklisted = new AtomicBoolean(false);
            configuredBlacklist.getWorlds().forEach(world -> {
                if (configuredBlacklist.isAsWhitelist()) {
                    if (!world.equals(home.getWorld())) {
                        isWorldBlacklisted.set(true);
                    }
                } else {
                    if (world.equals(home.getWorld())) {
                        isWorldBlacklisted.set(true);
                    }
                }
            });

            if (isServerBlacklisted.get()) {
                ModuleUtils.sendMessage(sender, getWithOther(sender, messageResultBlacklistedServer, targetUser));
                return;
            }

            if (isWorldBlacklisted.get()) {
                ModuleUtils.sendMessage(sender, getWithOther(sender, messageResultBlacklistedWorld, targetUser));
                return;
            }
        }

        if (home == null) {
            ModuleUtils.sendMessage(sender, getWithOther(player, messageResultHomeNotExists
                    .replace("%this_input%", homeName), targetUser)
            );
            return;
        }

        if (! (targetUser instanceof StreamlinePlayer)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
            return;
        }

        StreamlinePlayer targetPlayer = (StreamlinePlayer) sender;

        home.teleport(targetPlayer, home.getServer());

        if (targetUser.equals(sender)) {
            ModuleUtils.sendMessage(sender, getWithOther(player, messageResult
                    .replace("%this_input%", homeName), targetUser)
            );
        } else {
            ModuleUtils.sendMessage(sender, getWithOther(player, messageResultOther
                    .replace("%this_input%", homeName)
                    .replace("%this_other%", targetUser.getName()), targetUser)
            );
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser streamlineUser, String[] strings) {
        if (strings.length == 2) {
            if (ModuleUtils.hasPermission(streamlineUser, permissionOther)) {
                return ModuleUtils.getOnlinePlayerNames();
            }
        }

        return new ConcurrentSkipListSet<>();
    }
}
