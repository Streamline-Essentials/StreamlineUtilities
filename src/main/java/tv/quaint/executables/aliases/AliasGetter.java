package tv.quaint.executables.aliases;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class AliasGetter {
    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private Callable<List<String>> getter;

    public AliasGetter(String identifier, Callable<List<String>> getter) {
        this.identifier = identifier;
        this.getter = getter;
    }

    public List<String> get() {
        try {
            return getGetter().call();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
