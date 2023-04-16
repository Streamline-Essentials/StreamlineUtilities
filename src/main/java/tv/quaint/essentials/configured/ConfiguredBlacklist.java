package tv.quaint.essentials.configured;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.thebase.lib.leonhard.storage.sections.FlatFileSection;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

public class ConfiguredBlacklist {
    @Getter @Setter
    private boolean asWhitelist;
    @Getter @Setter
    private ConcurrentSkipListSet<String> servers;
    @Getter @Setter
    private ConcurrentSkipListSet<String> worlds;

    public ConfiguredBlacklist(boolean asWhitelist, ConcurrentSkipListSet<String> servers, ConcurrentSkipListSet<String> worlds) {
        this.asWhitelist = asWhitelist;
        this.servers = servers;
        this.worlds = worlds;
    }

    public ConfiguredBlacklist(boolean asWhitelist) {
        this(asWhitelist, new ConcurrentSkipListSet<>(), new ConcurrentSkipListSet<>());
    }

    public ConfiguredBlacklist() {
        this(false);
    }

    public ConfiguredBlacklist(FlatFileSection section) {
        this(section.getBoolean("as-whitelist"),
                new ConcurrentSkipListSet<>(section.getStringList("servers")),
                new ConcurrentSkipListSet<>(section.getStringList("worlds")));
    }

    public void save(FlatFileSection section) {
        section.set("as-whitelist", isAsWhitelist());
        section.set("servers", new ArrayList<>(getServers()));
        section.set("worlds", new ArrayList<>(getWorlds()));
    }
}
