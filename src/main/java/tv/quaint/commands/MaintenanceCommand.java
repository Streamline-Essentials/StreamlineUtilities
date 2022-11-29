package tv.quaint.commands;

import com.earth2me.essentials.User;
import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.MessageUtils;
import net.streamline.api.utils.UserUtils;
import tv.quaint.StreamlineUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class MaintenanceCommand extends ModuleCommand {
    @Getter
    private final String messageResultAll;
    @Getter
    private final String messageResultAdd;
    @Getter
    private final String messageResultRemove;

    public MaintenanceCommand() {
        super(StreamlineUtilities.getInstance(),
                "proxymaintenance",
                "streamline.command.maintenance.default",
                "pmaint", "pmaintenance", "pmnt"
        );

        messageResultAll = getCommandResource().getOrSetDefault("messages.result.all", "&cMaintenance Mode &eis now %utils_maintenance_mode% &b(&ewas %this_previous%&b)&8.");
        messageResultAdd = getCommandResource().getOrSetDefault("messages.result.add",
                "&eAdded &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &eto &cMaintenance Mode &ewhitelist&e.");
        messageResultRemove = getCommandResource().getOrSetDefault("messages.result.remove",
                "&eRemoved &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &efrom &cMaintenance Mode &ewhitelist&e.");
    }

    @Override
    public void run(StreamlineUser streamlineUser, String[] strings) {
        if (strings.length < 1) {
            strings = new String[] { String.valueOf(! StreamlineUtilities.getMaintenanceConfig().isModeEnabled()) };
        }
//        if (strings.length > 1) {
//            ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
//            return;
//        }

        if (strings.length == 1) {
            if (strings[0].equals("add") || strings[0].equals("remove")) {
                ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                return;
            }
            try {
                String previous = ModuleUtils.replacePlaceholders("%utils_maintenance_mode%");
                boolean bool = Boolean.parseBoolean(strings[0]);
                StreamlineUtilities.getMaintenanceConfig().setModeEnabled(bool);
                ModuleUtils.sendMessage(streamlineUser, getWithOther(streamlineUser, getMessageResultAll(), streamlineUser)
                        .replace("%this_previous%", previous)
                );

                if (bool) {
                    ModuleUtils.getLoadedUsers().forEach((s, player) -> {
                        if (StreamlineUtilities.getMaintenanceConfig().containsAllowed(player.getUuid())) return;
                        ModuleUtils.kick(player, ModuleUtils.replaceAllPlayerBungee(streamlineUser, StreamlineUtilities.getMaintenanceConfig().getModeKickMessage()));
                    });
                }
                return;
            } catch (Exception e) {
                ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TYPE_DEFAULT.get());
                return;
            }
        }

        switch (strings[0]) {
            case "add" -> {
                ConcurrentSkipListSet<String> names = new ConcurrentSkipListSet<>(Arrays.stream(MessageUtils.argsMinus(strings, 0)).toList());

                AtomicInteger atomicInteger = new AtomicInteger(0);
                names.forEach(s -> {
                    StreamlinePlayer player = UserUtils.getOrGetPlayerByName(s);
                    if (player == null) {
                        ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                        return;
                    }

                    StreamlineUtilities.getMaintenanceConfig().addAllowed(player.getUuid());
                    atomicInteger.getAndAdd(1);

                    ModuleUtils.sendMessage(streamlineUser,
                            getWithOther(streamlineUser, getMessageResultAdd(), player)
                                    .replace("%this_index%", String.valueOf(atomicInteger.get())
                                    )
                    );
                });

                StreamlineUtilities.getInstance().logInfo("Added " + atomicInteger.get() + " users to the &cMaintenance Mode &awhitelist&f!");
            }
            case "remove" -> {
                ConcurrentSkipListSet<String> names = new ConcurrentSkipListSet<>(Arrays.stream(MessageUtils.argsMinus(strings, 0)).toList());

                AtomicInteger atomicInteger = new AtomicInteger(0);
                names.forEach(s -> {
                    StreamlinePlayer player = UserUtils.getOrGetPlayerByName(s);
                    if (player == null) {
                        ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PLAYER_OTHER.get());
                        return;
                    }

                    StreamlineUtilities.getMaintenanceConfig().removeAllowed(player.getUuid());

                    if (StreamlineUtilities.getMaintenanceConfig().isModeEnabled()) {
                        if (player.updateOnline()) ModuleUtils.kick(player, "%utils_maintenance_message%");
                    }

                    atomicInteger.getAndAdd(1);

                    ModuleUtils.sendMessage(streamlineUser,
                            getWithOther(streamlineUser, getMessageResultRemove(), player)
                                    .replace("%this_index%", String.valueOf(atomicInteger.get())
                                    )
                    );
                });

                StreamlineUtilities.getInstance().logInfo("Removed " + atomicInteger.get() + " users from the &cMaintenance Mode &awhitelist&f!");
            }
            default -> {
                ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                return;
            }
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
