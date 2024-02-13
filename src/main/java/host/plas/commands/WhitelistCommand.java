package host.plas.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.GivenConfigs;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.configs.given.whitelist.WhitelistEntry;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.MessageUtils;
import net.streamline.api.utils.UserUtils;
import host.plas.StreamlineUtilities;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WhitelistCommand extends ModuleCommand {
    @Getter
    private final String messageResultAll;
    @Getter
    private final String messageResultAdd;
    @Getter
    private final String messageResultRemove;

    public WhitelistCommand() {
        super(StreamlineUtilities.getInstance(),
                "proxywhitelist",
                "streamline.command.whitelist.default",
                "pwhite", "pwhitelist", "pwl"
        );

        messageResultAll = getCommandResource().getOrSetDefault("messages.result.all", "&cWhitelist Mode &eis now %utils_whitelist_mode% &b(&ewas %this_previous%&b)&8.");
        messageResultAdd = getCommandResource().getOrSetDefault("messages.result.add",
                "&eAdded &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &eto &cWhitelist Mode &ewhitelist&e.");
        messageResultRemove = getCommandResource().getOrSetDefault("messages.result.remove",
                "&eRemoved &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &efrom &cWhitelist Mode &ewhitelist&e.");
    }

    @Override
    public void run(StreamlineUser streamlineUser, String[] strings) {
        if (strings.length < 1) {
            strings = new String[] { String.valueOf(! StreamlineUtilities.getMaintenanceConfig().isModeEnabled()) };
        }

        if (strings.length == 1) {
            if (strings[0].equals("add") || strings[0].equals("remove")) {
                ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                return;
            }
            try {
                String previous = ModuleUtils.replacePlaceholders("%utils_whitelist_mode%");
                boolean bool = Boolean.parseBoolean(strings[0]);
                GivenConfigs.getWhitelistConfig().setEnabled(bool);
                ModuleUtils.sendMessage(streamlineUser, getWithOther(streamlineUser, getMessageResultAll(), streamlineUser)
                        .replace("%this_previous%", previous)
                );

                if (bool) {
                    ModuleUtils.getLoadedUsers().forEach((s, player) -> {
                        if (GivenConfigs.getWhitelistConfig().getEntry(player.getUuid()) != null) return;
                        ModuleUtils.kick(player, ModuleUtils.replaceAllPlayerBungee(streamlineUser, "%utils_whitelist_message%"));
                    });
                }
                return;
            } catch (Exception e) {
                ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TYPE_DEFAULT.get());
                return;
            }
        }

        switch (strings[0]) {
            case "add":
                ConcurrentSkipListSet<String> names = Arrays.stream(MessageUtils.argsMinus(strings, 0)).collect(Collectors.toCollection(ConcurrentSkipListSet::new));

                AtomicInteger atomicInteger = new AtomicInteger(0);
                names.forEach(s -> {
                    StreamlinePlayer player = UserUtils.getOrGetPlayerByName(s);
                    if (player == null) {
                        ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                        return;
                    }

                    GivenConfigs.getWhitelistConfig().addEntry(new WhitelistEntry(player.getUuid(), new Date(), streamlineUser.getUuid()));
                    atomicInteger.getAndAdd(1);

                    ModuleUtils.sendMessage(streamlineUser,
                            getWithOther(streamlineUser, getMessageResultAdd(), player)
                                    .replace("%this_index%", String.valueOf(atomicInteger.get())
                                    )
                    );
                });

                StreamlineUtilities.getInstance().logInfo("Added " + atomicInteger.get() + " users to the &cMaintenance Mode &awhitelist&f!");
                break;
            case "remove":
                ConcurrentSkipListSet<String> namesRemove = Arrays.stream(MessageUtils.argsMinus(strings, 0)).collect(Collectors.toCollection(ConcurrentSkipListSet::new));

                AtomicInteger atomicIntegerRemove = new AtomicInteger(0);
                namesRemove.forEach(s -> {
                    StreamlinePlayer player = UserUtils.getOrGetPlayerByName(s);
                    if (player == null) {
                        ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                        return;
                    }

                    GivenConfigs.getWhitelistConfig().getEntry(player.getUuid()).remove();

                    if (GivenConfigs.getWhitelistConfig().isEnabled()) {
                        if (player.updateOnline()) ModuleUtils.kick(player, "%utils_whitelist_message%");
                    }

                    atomicIntegerRemove.getAndAdd(1);

                    ModuleUtils.sendMessage(streamlineUser,
                            getWithOther(streamlineUser, getMessageResultRemove(), player)
                                    .replace("%this_index%", String.valueOf(atomicIntegerRemove.get())
                                    )
                    );
                });

                StreamlineUtilities.getInstance().logInfo("Removed " + atomicIntegerRemove.get() + " users from the &cMaintenance Mode &awhitelist&f!");
                break;
            default:
                ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                return;
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length == 1) return new ConcurrentSkipListSet<>(List.of("true", "false", "add", "remove"));
        if (strings.length >= 2) {
            if (strings[0].equals("add") || strings[0].equals("remove")) return ModuleUtils.getOnlinePlayerNames();
        }

        return new ConcurrentSkipListSet<>();
    }
}
