package tv.quaint.configs;

import de.leonhard.storage.Json;
import net.streamline.api.configs.FlatFileResource;
import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

public class MaintenanceConfig extends ModularizedConfig {

    public MaintenanceConfig() {
        super(StreamlineUtilities.getInstance(), "maintenance-config.yml", false);

        init();
    }

    public void init() {
        isModeEnabled();
        getModeKickMessage();
        isModeKickOnline();
        getAllowedUUIDs();
    }

    public boolean isModeEnabled() {
        reloadResource();

        return resource.getOrSetDefault("mode.enabled", false);
    }

    public void setModeEnabled(boolean bool) {
        resource.set("mode.enabled", bool);
    }

    public String getModeKickMessage() {
        reloadResource();

        return resource.getOrSetDefault("mode.kick.message", "&cThis server is currently being maintenanced&8!%newline%&cPlease feel free to come back in a little bit&8!%newline%%newline%&aJoin our &9&lDiscord &afor updates&8: {{discord_link}}");
    }

    public boolean isModeKickOnline() {
        reloadResource();

        return resource.getOrSetDefault("mode.kick.online", true);
    }

    public ConcurrentSkipListSet<String> getAllowedUUIDs() {
        reloadResource();

        return new ConcurrentSkipListSet<>(resource.getOrSetDefault("allowed-to-join.uuids", new ArrayList<>()));
    }

    public boolean containsAllowed(String uuid) {
        return getAllowedUUIDs().contains(uuid);
    }

    public void addAllowed(String uuid) {
        if (containsAllowed(uuid)) return;

        ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>(getAllowedUUIDs());
        r.add(uuid);

        resource.set("allowed-to-join.uuids", r.stream().toList());
    }

    public void removeAllowed(String uuid) {
        if (! containsAllowed(uuid)) return;

        ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>(getAllowedUUIDs());
        r.remove(uuid);

        resource.set("allowed-to-join.uuids", r.stream().toList());
    }
}
