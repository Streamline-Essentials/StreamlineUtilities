package tv.quaint.configs;

import lombok.Getter;
import net.streamline.api.configs.ModularizedConfig;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.placeholder.CustomPlaceholder;
import net.streamline.api.utils.MessageUtils;
import tv.quaint.StreamlineUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ServerAliasesConfig extends ModularizedConfig {
    @Getter
    private ConcurrentSkipListMap<String, String> loadedServerAliases = new ConcurrentSkipListMap<>();

    public ServerAliasesConfig() {
        super(StreamlineUtilities.getInstance(), "server-aliases.yml", false);
        reloadResource(true);
    }

    @Override
    public void init() {

    }

    @Override
    public void reloadResource(boolean force) {
        getAsObjects().forEach(a -> ModuleUtils.getRATAPI().unregisterCustomPlaceholder(a));
        super.reloadResource(force);
        this.loadedServerAliases = getCustomPlaceholders();
        getAsObjects().forEach(a -> ModuleUtils.getRATAPI().registerCustomPlaceholder(a));
    }

    private ConcurrentSkipListMap<String, String> getCustomPlaceholders() {
        ModuleUtils.getServerNames().forEach(s -> {
            if (getResource().contains(s)) return;
            getResource().set(s, new ArrayList<>());
        });

        ConcurrentSkipListMap<String, String> r = new ConcurrentSkipListMap<>();
        for (String key : getResource().singleLayerKeySet()) {
            if (! ModuleUtils.getServerNames().contains(key)) {
                StreamlineUtilities.getInstance().logWarning("Supposed server '" + key + "' in your 'server-aliases.yml' is not actually a server... Skipping...");
                continue;
            }

            try {
                getResource().singleLayerKeySet(key).forEach(s -> {
                    r.put(s, key);
                });
            } catch (Exception e) {
                StreamlineUtilities.getInstance().logWarning("Could not load placeholder value for '" + key + "' due to: " + e.getMessage());
            }
        }
        return r;
    }

    public List<CustomPlaceholder> getAsObjects() {
        List<CustomPlaceholder> r = new ArrayList<>();

        if (getLoadedServerAliases() == null) return r;

        for (String key : getLoadedServerAliases().keySet()) {
            r.add(new CustomPlaceholder(key, getLoadedServerAliases().get(key)));
        }

        return r;
    }
}
