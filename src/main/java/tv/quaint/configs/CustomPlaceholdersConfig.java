package tv.quaint.configs;

import lombok.Getter;
import net.streamline.api.configs.ModularizedConfig;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.placeholder.CustomPlaceholder;
import tv.quaint.StreamlineUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class CustomPlaceholdersConfig extends ModularizedConfig {
    @Getter
    private TreeMap<String, String> loadedPlaceholders = new TreeMap<>();

    public CustomPlaceholdersConfig() {
        super(StreamlineUtilities.getInstance(), "custom-placeholders.yml", true);
        reloadResource(true);
    }

    @Override
    public void reloadResource(boolean force) {
        getAsObjects().forEach(a -> ModuleUtils.getRATAPI().unregisterCustomPlaceholder(a));
        super.reloadResource(force);
        this.loadedPlaceholders = getCustomPlaceholders();
        getAsObjects().forEach(a -> ModuleUtils.getRATAPI().registerCustomPlaceholder(a));
    }

    private TreeMap<String, String> getCustomPlaceholders() {
        TreeMap<String, String> r = new TreeMap<>();
        for (String key : resource.singleLayerKeySet()) {
            try {
                r.put(key, resource.getString(key));
            } catch (Exception e) {
                StreamlineUtilities.getInstance().logWarning("Could not load placeholder value for '" + key + "' due to: " + e.getMessage());
            }
        }
        return r;
    }

    public List<CustomPlaceholder> getAsObjects() {
        List<CustomPlaceholder> r = new ArrayList<>();

        if (getLoadedPlaceholders() == null) return r;

        for (String key : getLoadedPlaceholders().keySet()) {
            r.add(new CustomPlaceholder(key, getLoadedPlaceholders().get(key)));
        }

        return r;
    }
}
