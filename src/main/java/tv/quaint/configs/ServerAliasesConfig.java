package tv.quaint.configs;

import lombok.Getter;
import net.streamline.api.configs.ModularizedConfig;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.StreamlineUtilities;
import tv.quaint.configs.obj.ServerAlias;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class ServerAliasesConfig extends ModularizedConfig {
    @Getter
    private ConcurrentSkipListMap<String, ServerAlias> loadedServerAliases = new ConcurrentSkipListMap<>();

    public ServerAliasesConfig() {
        super(StreamlineUtilities.getInstance(), "server-aliases.yml", false);
        reloadResource(true);
    }

    @Override
    public void init() {
        loadedServerAliases = new ConcurrentSkipListMap<>();
    }

    @Override
    public void reloadResource(boolean force) {
        this.loadedServerAliases = new ConcurrentSkipListMap<>();
        super.reloadResource(force);
        this.loadedServerAliases = getServerAliases();
    }

    private ConcurrentSkipListMap<String, ServerAlias> getServerAliases() {
        ModuleUtils.getServerNames().forEach(s -> {
            if (getResource().contains(s)) return;
            getResource().set(s, new ArrayList<>());
        });

        ConcurrentSkipListMap<String, ServerAlias> r = new ConcurrentSkipListMap<>();
        for (String key : getResource().singleLayerKeySet()) {
            if (! ModuleUtils.getServerNames().contains(key)) {
                StreamlineUtilities.getInstance().logWarning("Supposed server '" + key + "' in your 'server-aliases.yml' is not actually a server... Skipping...");
                continue;
            }

            try {
                getResource().singleLayerKeySet(key).forEach(s -> {
                    ServerAlias serverAlias = ServerAlias.buildFrom(s, getResource().getSection(key));
                    r.put(serverAlias.getActualServer(), serverAlias);
                });
            } catch (Exception e) {
                StreamlineUtilities.getInstance().logWarning("Could not load server alias value for '" + key + "' due to: " + e.getMessage());
            }
        }
        return r;
    }

    public ServerAlias getServerAlias(String aliasOrServer) {
        AtomicReference<ServerAlias> alias = new AtomicReference<>(null);
        StreamlineUtilities.getServerAliasesConfig().getLoadedServerAliases().forEach((strng, serverAlias) -> {
            if (serverAlias.hasAliasOrActualName(aliasOrServer)) alias.set(serverAlias);
        });
        return alias.get();
    }

    public boolean hasServerAlias(String aliasOrServer) {
        return getServerAlias(aliasOrServer) != null;
    }

    public String getActualName(String aliasOrServer) {
        if (! hasServerAlias(aliasOrServer)) return null;
        return getServerAlias(aliasOrServer).getActualServer();
    }

    public ConcurrentSkipListSet<String> getPossibleNames() {
        ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();
        getLoadedServerAliases().forEach((s, serverAlias) -> {
            r.add(serverAlias.getActualServer());
            r.addAll(serverAlias.getAliases());
        });
        return r;
    }
}
