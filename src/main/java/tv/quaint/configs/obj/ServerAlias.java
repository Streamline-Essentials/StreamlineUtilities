package tv.quaint.configs.obj;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import tv.quaint.thebase.lib.leonhard.storage.sections.FlatFileSection;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class ServerAlias implements Comparable<ServerAlias> {
    @Getter @Setter
    String actualServer;
    @Getter @Setter
    ConcurrentSkipListSet<String> aliases;

    public ServerAlias(String actualServer, ConcurrentSkipListSet<String> aliases) {
        this.actualServer = actualServer;
        this.aliases = aliases;
    }

    public boolean hasAlias(String alias) {
        return aliases.contains(alias);
    }

    public boolean isActualServer(String server) {
        return actualServer.equalsIgnoreCase(server);
    }

    public boolean hasAliasOrActualName(String server) {
        return hasAlias(server) || isActualServer(server);
    }

    public void addAlias(String alias) {
        aliases.add(alias);
    }

    public void removeAlias(String alias) {
        aliases.remove(alias);
    }

    @Override
    public int compareTo(@NotNull ServerAlias o) {
        return CharSequence.compare(actualServer, o.actualServer);
    }

    public static ServerAlias buildFrom(String key, FlatFileSection section) {
        ConcurrentSkipListSet<String> aliases = new ConcurrentSkipListSet<>(section.getStringList(key));
        return new ServerAlias(key, aliases);
    }

    public static ServerAlias buildFrom(String name, List<String> aliases) {
        return new ServerAlias(name, new ConcurrentSkipListSet<>(aliases));
    }
}
