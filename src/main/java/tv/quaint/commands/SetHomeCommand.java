package tv.quaint.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineLocation;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.UserUtils;
import tv.quaint.StreamlineUtilities;
import tv.quaint.essentials.EssentialsManager;
import tv.quaint.essentials.configured.ConfiguredBlacklist;
import tv.quaint.essentials.configured.ConfiguredPermissionsList;
import tv.quaint.essentials.users.StreamlineHome;
import tv.quaint.essentials.users.UtilitiesUser;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class SetHomeCommand extends ModuleCommand {
    @Getter
    private final String messageResultSet;
    @Getter
    private final String messageResultReplaced;

    @Getter
    private final String messageResultSetOther;
    @Getter
    private final String messageResultReplacedOther;

    @Getter
    private final String messageResultTooManyHomes;
    @Getter
    private final String messageResultBlacklistedWorld;
    @Getter
    private final String messageResultBlacklistedServer;

    @Getter
    private final String permissionSetHomeOther;

    public SetHomeCommand() {
        super(StreamlineUtilities.getInstance(),
                "streamline-sethome",
                "streamline.command.sethome.default",
                "ssethome", "stsh"
        );

        messageResultSet = getCommandResource().getOrSetDefault("messages.result.set", "&eSet home &7'&c%this_input%&7'&8!");
        messageResultReplaced = getCommandResource().getOrSetDefault("messages.result.replaced", "&eReplaced home &7'&c%this_input%&7'&8!");

        messageResultSetOther = getCommandResource().getOrSetDefault("messages.result.set-other", "&eSet home &7'&c%this_input%&7' &efor &7'&c%this_other%&7'&8!");
        messageResultReplacedOther = getCommandResource().getOrSetDefault("messages.result.replaced-other", "&eReplaced home &7'&c%this_input%&7' &efor &7'&c%this_other%&7'&8!");

        messageResultTooManyHomes = getCommandResource().getOrSetDefault("messages.result.too-many-homes", "&cYou have too many homes, so you cannot set another one&8!");
        messageResultBlacklistedWorld = getCommandResource().getOrSetDefault("messages.result.blocked.world", "&cYou cannot set a home in this world&8!");
        messageResultBlacklistedServer = getCommandResource().getOrSetDefault("messages.result.blocked.server", "&cYou cannot set a home on this server&8!");

        permissionSetHomeOther = getCommandResource().getOrSetDefault("permissions.sethome.other", "streamline.command.sethome.other");
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
            if (ModuleUtils.hasPermission(sender, permissionSetHomeOther)) {
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

        ConfiguredPermissionsList configuredPermissionsList = StreamlineUtilities.getConfigs().getHomesPermissions();

        if (configuredPermissionsList != null) {
            if (! configuredPermissionsList.hasPermission(targetUser, target.getHomesCount() + 1)) {
                ModuleUtils.sendMessage(sender, getWithOther(sender, messageResultTooManyHomes, targetUser));
                return;
            }
        }

        StreamlinePlayer player = (StreamlinePlayer) sender;
        StreamlineLocation location = player.getLocation();

        ConfiguredBlacklist configuredBlacklist = StreamlineUtilities.getConfigs().getHomesBlacklist();

        if (configuredBlacklist != null) {
            AtomicBoolean isServerBlacklisted = new AtomicBoolean(false);
            configuredBlacklist.getServers().forEach(server -> {
                if (configuredBlacklist.isAsWhitelist()) {
                    if (! server.equals(player.getLatestServer())) {
                        isServerBlacklisted.set(true);
                    }
                } else {
                    if (server.equals(player.getLatestServer())) {
                        isServerBlacklisted.set(true);
                    }
                }
            });
            AtomicBoolean isWorldBlacklisted = new AtomicBoolean(false);
            configuredBlacklist.getWorlds().forEach(world -> {
                if (configuredBlacklist.isAsWhitelist()) {
                    if (! world.equals(location.getWorld())) {
                        isWorldBlacklisted.set(true);
                    }
                } else {
                    if (world.equals(location.getWorld())) {
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

        StreamlineHome streamlineHome = new StreamlineHome(homeName, player.getLatestServer(), location.getWorld(),
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        StreamlineHome home = target.getHome(homeName);

        if (home != null) {
            target.removeHome(homeName);
            target.addHome(streamlineHome);

            if (! target.getUuid().equals(player.getUuid())) {
                ModuleUtils.sendMessage(sender, getWithOther(player, messageResultReplacedOther
                        .replace("%this_input%", homeName), targetUser)
                );
            } else {
                ModuleUtils.sendMessage(sender, getWithOther(player, messageResultReplaced
                        .replace("%this_input%", homeName), targetUser)
                );
            }
        } else {
            target.addHome(streamlineHome);

            if (! target.getUuid().equals(player.getUuid())) {
                ModuleUtils.sendMessage(sender, getWithOther(player, messageResultSetOther
                        .replace("%this_input%", homeName), targetUser)
                );
            } else {
                ModuleUtils.sendMessage(sender, getWithOther(player, messageResultSet
                        .replace("%this_input%", homeName), targetUser)
                );
            }
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length == 2) {
            if (ModuleUtils.hasPermission(StreamlineUser, permissionSetHomeOther)) {
                return ModuleUtils.getOnlinePlayerNames();
            }
        }

        return new ConcurrentSkipListSet<>();
    }
}
