package tv.quaint.executables.aliases;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;

public class AliasGetter implements Comparable<AliasGetter> {
    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private Callable<ConcurrentSkipListSet<String>> getter;

    public AliasGetter(String identifier, Callable<ConcurrentSkipListSet<String>> getter) {
        this.identifier = identifier;
        this.getter = getter;
    }

    public ConcurrentSkipListSet<String> get() {
        try {
            return getGetter().call();
        } catch (Exception e) {
            e.printStackTrace();
            return new ConcurrentSkipListSet<>();
        }
    }

    @Override
    public int compareTo(@NotNull AliasGetter o) {
        return CharSequence.compare(getIdentifier(), o.getIdentifier());
    }
}
