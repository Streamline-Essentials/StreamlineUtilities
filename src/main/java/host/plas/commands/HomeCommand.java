package host.plas.commands;

import host.plas.database.MyLoader;
import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.data.players.StreamPlayer;
import net.streamline.api.data.console.StreamSender;
import net.streamline.api.utils.UserUtils;
import host.plas.StreamlineUtilities;
import host.plas.essentials.EssentialsManager;
import host.plas.essentials.configured.ConfiguredBlacklist;
import host.plas.essentials.users.StreamlineHome;
import host.plas.essentials.users.UtilitiesUser;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class HomeCommand extends ModuleCommand {
    private final String messageResult;

    private final String messageResultOther;

    private final String messageResultHomeNotExists;
    private final String messageResultBlacklistedServer;
    private final String messageResultBlacklistedWorld;

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
    public void run(StreamSender sender, String[] strings) {
        String homeName = "home";
        StreamSender targetUser = null;
        UtilitiesUser target = null;

        if (strings.length <= 1) {
            target = MyLoader.getInstance().getOrCreate(sender.getUuid());
            targetUser = sender;
        }

        if (strings.length == 1) {
            homeName = strings[0];
        }

        if (strings.length == 2) {
            if (ModuleUtils.hasPermission(sender, permissionOther)) {
                targetUser = UserUtils.getOrCreatePlayerByName(strings[0]).orElse(null);
                if (targetUser == null) {
                    ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                target = MyLoader.getInstance().getOrCreate(targetUser.getUuid());
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

        if (! (sender instanceof StreamPlayer)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
            return;
        }

        StreamPlayer player = (StreamPlayer) sender;

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

        if (! (targetUser instanceof StreamPlayer)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
            return;
        }

        StreamPlayer targetPlayer = (StreamPlayer) sender;

        home.teleport(targetPlayer);

        if (targetUser.equals(sender)) {
            ModuleUtils.sendMessage(sender, getWithOther(player, messageResult
                    .replace("%this_input%", homeName), targetUser)
            );
        } else {
            ModuleUtils.sendMessage(sender, getWithOther(player, messageResultOther
                    .replace("%this_input%", homeName)
                    .replace("%this_other%", targetUser.getCurrentName()), targetUser)
            );
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamSender StreamSender, String[] strings) {
        if (strings.length == 2) {
            if (ModuleUtils.hasPermission(StreamSender, permissionOther)) {
                return ModuleUtils.getOnlinePlayerNames();
            }
        }

        return new ConcurrentSkipListSet<>();
    }
}
