package tv.quaint.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineUtilities;
import tv.quaint.configs.obj.PermissionGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentSkipListSet;

public class OnlineCommand extends ModuleCommand {
    @Getter
    private final String messageResultGlobalBase;
    @Getter
    private final String messageResultGlobalModified;
    @Getter
    private final String messageResultExactBase;
    @Getter
    private final String messageResultExactModified;
    @Getter
    private final String messageResultPermissionedBase;
    @Getter
    private final String messageResultPermissionedModified;
    @Getter
    private final String messageErrorInvalidServer;
    @Getter
    private final String messageErrorInvalidGroup;
    @Getter
    private final String permissionServers;
    @Getter
    private final String permissionGroups;

    public OnlineCommand() {
        super(StreamlineUtilities.getInstance(),
                "online",
                "streamline.command.online.default",
                "on"
        );

        messageResultGlobalBase = getCommandResource().getOrSetDefault("messages.result.global.base",
                "&eOnline&8: &r%this_online_global%");
        messageResultGlobalModified = getCommandResource().getOrSetDefault("messages.result.global.player.modified",
                "%streamline_parse_%this_username%:::*/*streamline_user_formatted*/* &7(&c*/*streamline_user_server*/*&7)%");

        messageResultExactBase = getCommandResource().getOrSetDefault("messages.result.exact.base",
                "&eOnline on &7'&c%this_identifier%&7'&8: &r%this_online_exact%");
        messageResultExactModified = getCommandResource().getOrSetDefault("messages.result.exact.player.modified",
                "%streamline_parse_%this_username%:::*/*streamline_user_formatted*/*%");

        messageResultPermissionedBase = getCommandResource().getOrSetDefault("messages.result.grouped.base",
                "&eOnline players for group &7'%utils_group_%this_identifier%_name%&7'&8: &r%this_online_permission%");
        messageResultPermissionedModified = getCommandResource().getOrSetDefault("messages.result.grouped.player.modified",
                "%streamline_parse_%this_username%:::*/*streamline_user_formatted*/* &7(&c*/*streamline_user_server*/*&7)%");

        messageErrorInvalidServer = getCommandResource().getOrSetDefault("messages.error.not-found.server",
                "&cThe server '%this_identifier%' does not exist!");
        messageErrorInvalidGroup = getCommandResource().getOrSetDefault("messages.error.not-found.group",
                "&cThe group '%this_identifier%' does not exist!");

        permissionServers = getCommandResource().getOrSetDefault("permissions.servers.base",
                "streamline.command.online.server.");
        permissionGroups = getCommandResource().getOrSetDefault("permissions.groups.base",
                "streamline.command.online.group.");
    }

    @Override
    public void run(StreamlineUser StreamlineUser, String[] strings) {
        if (strings[0].equals("")) {
            ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser,
                    messageResultGlobalBase
                            .replace("%this_online_global%", getOnlineGlobal())
                            .replace("%this_online_exact%", getOnlineExact(StreamlineUser.getLatestServer()))
                            .replace("%this_online_permission%", getOnlineByPermission(StreamlineUser.getLatestServer()))
                    ,
                    StreamlineUser
            ));
            return;
        }
        if (strings.length > 2) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
            return;
        }
        if (strings.length <= 1) {
            ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }

        String option = strings[0];
        switch (option.toLowerCase(Locale.ROOT)) {
            case "server" -> {
                String serverName = strings[1];
                if (! ModuleUtils.equalsAnyServer(serverName)) {
                    ModuleUtils.sendMessage(StreamlineUser, messageErrorInvalidServer.replace("%this_identifier%", serverName));
                    return;
                }

                ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser,
                        messageResultExactBase
                                .replace("%this_identifier%", serverName)
                                .replace("%this_online_global%", getOnlineGlobal())
                                .replace("%this_online_exact%", getOnlineExact(serverName))
                                .replace("%this_online_permission%", getOnlineByPermission(serverName))
                        ,
                        StreamlineUser
                ));
            }
            case "group" -> {
                String group = strings[1];
                if (! StreamlineUtilities.getGroupedPermissionConfig().getPermissionGroups().containsKey(group)) {
                    ModuleUtils.sendMessage(StreamlineUser, messageErrorInvalidGroup.replace("%this_identifier%", group));
                    return;
                }

                ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser,
                        messageResultPermissionedBase
                                .replace("%this_identifier%", group)
                                .replace("%this_online_global%", getOnlineGlobal())
                                .replace("%this_online_exact%", getOnlineExact(group))
                                .replace("%this_online_permission%", getOnlineByPermission(group))
                        ,
                        StreamlineUser
                ));
            }
            default -> {

            }
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length <= 1) return new ConcurrentSkipListSet<>(List.of("server", "group"));
        if (strings.length > 2) return new ConcurrentSkipListSet<>();
        if (strings[0].equals("server")) {
            ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();
            ModuleUtils.getServerNames().forEach(a -> {
                if (ModuleUtils.hasPermission(StreamlineUser, permissionServers + a)) r.add(a);
            });
            return r;
        }
        if (strings[0].equals("group")) {
            ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();
            StreamlineUtilities.getGroupedPermissionConfig().getPermissionGroups().keySet().stream().toList().forEach(a -> {
                if (ModuleUtils.hasPermission(StreamlineUser, permissionServers + a)) r.add(a);
            });
            return r;
        }
        return new ConcurrentSkipListSet<>();
    }

    public String getOnlineGlobal() {
        List<String> modified = new ArrayList<>();
        ModuleUtils.getOnlinePlayerNames().forEach(a -> {
            modified.add(messageResultGlobalModified.replace("%this_username%", a));
        });

        return ModuleUtils.getListAsFormattedString(modified);
    }

    public String getOnlineExact(String serverName) {
        List<String> modified = new ArrayList<>();
        ModuleUtils.getUsersOn(serverName).forEach(a -> {
            modified.add(messageResultExactModified.replace("%this_username%", a.getName()));
        });

        return ModuleUtils.getListAsFormattedString(modified);
    }

    public String getOnlineByPermission(String identifier) {
        PermissionGroup group = StreamlineUtilities.getGroupedPermissionConfig().getPermissionGroups().get(identifier);
        if (group == null) return "";

        List<String> modified = new ArrayList<>();
        ModuleUtils.getLoadedUsersSet().forEach(a -> {
            if (! a.isOnline()) return;
            if (! ModuleUtils.hasPermission(a, group.permission())) return;
            modified.add(messageResultPermissionedModified.replace("%this_username%", a.getName()));
        });

        return ModuleUtils.getListAsFormattedString(modified);
    }
}
