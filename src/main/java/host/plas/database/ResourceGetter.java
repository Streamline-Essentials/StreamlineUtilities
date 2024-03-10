package host.plas.database;

import java.util.function.Function;

public interface ResourceGetter<T> extends Function<String, T> {
}
