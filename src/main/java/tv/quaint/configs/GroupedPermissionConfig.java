package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineUtilities;
import tv.quaint.configs.obj.PermissionGroup;

import java.util.concurrent.ConcurrentSkipListMap;

public class GroupedPermissionConfig extends ModularizedConfig {

    public GroupedPermissionConfig() {
        super(StreamlineUtilities.getInstance(), "grouped-permissions.yml", true);
    }

    @Override
    public void init() {

    }

    public ConcurrentSkipListMap<String, PermissionGroup> getPermissionGroups() {
        ConcurrentSkipListMap<String, PermissionGroup> r = new ConcurrentSkipListMap<>();
        for (String key : getResource().singleLayerKeySet()) {
            try {
                String name = getResource().getString(key + ".name");
                String permission = getResource().getString(key + ".permission");
                r.put(key, new PermissionGroup(key, name, permission));
            } catch (Exception e) {
                StreamlineUtilities.getInstance().logWarning("Could not load placeholder value for '" + key + "' due to: " + e.getMessage());
            }
        }
        return r;
    }
}
