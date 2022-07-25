package tv.quaint.ratapi;

import net.streamline.api.placeholder.RATExpansion;
import net.streamline.api.savables.users.SavableUser;
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
        return null;
    }

    @Override
    public String onRequest(SavableUser savableUser, String s) {
        return null;
    }
}
