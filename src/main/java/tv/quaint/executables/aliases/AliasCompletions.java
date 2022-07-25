package tv.quaint.executables.aliases;

import de.leonhard.storage.sections.FlatFileSection;
import tv.quaint.StreamlineUtilities;
import tv.quaint.executables.ExecutableHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class AliasCompletions {
    public static TreeMap<Integer, List<String>> getCompletions(StreamAlias alias) {
        FlatFileSection section = alias.getCommandResource().resource.getSection("basic.completion");

        TreeMap<Integer, List<String>> r = new TreeMap<>();

        StreamlineUtilities.getGetters().forEach(a -> {
            ExecutableHandler.unloadGetter(a);
            ExecutableHandler.loadGetter(a);
        });

        for (String key : section.singleLayerKeySet()) {
            int i = 0;
            try {
                i = Integer.parseInt(key);
            } catch (Exception e) {
                StreamlineUtilities.getInstance().logWarning("StreamAlias '" + alias.getBase() + "' has a mis-configured completion argument! Argument '" + key + "' was found to not be an integer! It must be an integer!");
            }

            List<String> completion = section.getOrSetDefault(key, new ArrayList<>());
            for (String string : new ArrayList<>(completion)) {
                if (string.startsWith("@")) {
                    String identifier = string.substring("@".length());
                    if (! ExecutableHandler.isGetterLoaded(identifier)) continue;

                    completion.addAll(ExecutableHandler.getGetterAndGet(identifier));
                    completion.remove(string);
                }
            }

            r.put(i, completion);
        }

        return r;
    }
}
