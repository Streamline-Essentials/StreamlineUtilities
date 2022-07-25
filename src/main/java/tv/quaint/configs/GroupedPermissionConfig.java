package tv.quaint.configs;

import lombok.Getter;
import net.streamline.api.configs.ModularizedConfig;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.placeholder.CustomPlaceholder;
import tv.quaint.StreamlineUtilities;
import tv.quaint.configs.obj.PermissionGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class GroupedPermissionConfig extends ModularizedConfig {

    public GroupedPermissionConfig() {
        super(StreamlineUtilities.getInstance(), "grouped-permissions.yml", true);
    }

    public TreeMap<String, PermissionGroup> getPermissionGroups() {
        TreeMap<String, PermissionGroup> r = new TreeMap<>();
        for (String key : resource.singleLayerKeySet()) {
            try {
                String name = resource.getString(key + ".name");
                String permission = resource.getString(key + ".permission");
                r.put(key, new PermissionGroup(key, name, permission));
            } catch (Exception e) {
                StreamlineUtilities.getInstance().logWarning("Could not load placeholder value for '" + key + "' due to: " + e.getMessage());
            }
        }
        return r;
    }
}
