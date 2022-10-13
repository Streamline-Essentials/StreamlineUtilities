package tv.quaint.ratapi;

import net.streamline.api.configs.given.GivenConfigs;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.placeholder.RATExpansion;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineUtilities;
import tv.quaint.configs.obj.PermissionGroup;

public class UtilitiesExpansion extends RATExpansion {
    public UtilitiesExpansion() {
        super("utils", "Quaint", "1.0");
    }

    @Override
    public String onLogic(String s) {
        if (s.startsWith("group_")) {
            String identifier = s.substring("group_".length());
            if (identifier.contains("_")) identifier = identifier.substring(0, identifier.indexOf("_"));
            PermissionGroup group = StreamlineUtilities.getGroupedPermissionConfig().getPermissionGroups().get(identifier);
            if (group == null) return null;
            if (s.equals("group_" + identifier + "_name")) {
                return group.name();
            }
            if (s.equals("group_" + identifier + "_permission")) {
                return group.permission();
            }
        }
        if (s.equals("maintenance_mode")) return (StreamlineUtilities.getMaintenanceConfig().isModeEnabled()
                ? MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() : MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get());
        if (s.equals("maintenance_message")) return StreamlineUtilities.getMaintenanceConfig().getModeKickMessage();
        if (s.equals("whitelist_mode")) return (GivenConfigs.getWhitelistConfig().isEnabled()
                ? MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() : MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get());
        if (s.equals("whitelist_message")) return MainMessagesHandler.MESSAGES.INVALID.WHITELIST_NOT.get();
        return null;
    }

    @Override
    public String onRequest(StreamlineUser StreamlineUser, String s) {
        return null;
    }
}
