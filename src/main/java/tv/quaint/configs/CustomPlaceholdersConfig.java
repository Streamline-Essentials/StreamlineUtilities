package tv.quaint.configs;

import lombok.Getter;
import net.streamline.api.configs.ModularizedConfig;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.placeholders.RATRegistry;
import net.streamline.api.placeholders.replaceables.GenericReplaceable;
import tv.quaint.StreamlineUtilities;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class CustomPlaceholdersConfig extends ModularizedConfig {
    @Getter
    private ConcurrentSkipListMap<String, String> loadedPlaceholders = new ConcurrentSkipListMap<>();

    public CustomPlaceholdersConfig() {
        super(StreamlineUtilities.getInstance(), "custom-placeholders.yml", true);
        reloadResource(true);
    }

    @Override
    public void init() {
        loadedPlaceholders = new ConcurrentSkipListMap<>();
    }

    @Override
    public void reloadResource(boolean force) {
        getAsObjects().forEach(RATRegistry::unregister);
        super.reloadResource(force);
        this.loadedPlaceholders = getCustomPlaceholders();
        getAsObjects().forEach(RATRegistry::register);
    }

    private ConcurrentSkipListMap<String, String> getCustomPlaceholders() {
        ConcurrentSkipListMap<String, String> r = new ConcurrentSkipListMap<>();
        for (String key : getResource().singleLayerKeySet()) {
            try {
                r.put(key, getResource().getString(key));
            } catch (Exception e) {
                StreamlineUtilities.getInstance().logWarning("Could not load placeholder value for '" + key + "' due to: " + e.getMessage());
            }
        }
        return r;
    }

    public ConcurrentSkipListSet<GenericReplaceable> getAsObjects() {
        ConcurrentSkipListSet<GenericReplaceable> r = new ConcurrentSkipListSet<>();

        if (getLoadedPlaceholders() == null) return r;

        for (String key : getLoadedPlaceholders().keySet()) {
            r.add(new GenericReplaceable(key, (s) -> getLoadedPlaceholders().get(key)));
        }

        return r;
    }
}
