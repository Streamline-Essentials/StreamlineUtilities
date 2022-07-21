package tv.quaint.executables.functions;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.StreamlineUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FunctionHandler {
    @Getter @Setter
    private static TreeMap<String, StreamlineFunction> loadedFunctions = new TreeMap<>();

    public static void loadFunctions(File folder) {
        if (! folder.isDirectory()) return;

        File[] files = folder.listFiles();
        if (files == null) return;
        List<StreamlineFunction> toLoad = new ArrayList<>();
        for (File file : files) {
            toLoad.add(new StreamlineFunction(file.getParentFile(), file.getName()));
        }
        loadThese(toLoad);
    }

    public static void loadFunction(StreamlineFunction function) {
        getLoadedFunctions().put(function.getIdentifier(), function);
        StreamlineUtilities.getInstance().logInfo("Loaded function with identifier of '" + function.getIdentifier() + "'!");
    }

    public static void unloadFunction(StreamlineFunction function) {
        getLoadedFunctions().remove(function.getIdentifier());
        StreamlineUtilities.getInstance().logInfo("Unloaded function with identifier of '" + function.getIdentifier() + "'!");
    }

    public static void loadThese(Collection<StreamlineFunction> functions) {
        AtomicInteger count = new AtomicInteger();
        functions.forEach((streamlineFunction) -> {
            if (streamlineFunction.load()) count.getAndIncrement();
        });

        StreamlineUtilities.getInstance().logInfo("Loaded " + count + " functions!");
    }

    public static void unloadAll() {
        unloadThese(getLoadedFunctions().values());
    }

    public static void unloadThese(Collection<StreamlineFunction> functions) {
        AtomicInteger count = new AtomicInteger();
        functions.forEach((streamlineFunction) -> {
            if (streamlineFunction.unload()) count.getAndIncrement();
        });

        StreamlineUtilities.getInstance().logInfo("Unloaded " + count + " functions!");
    }

    public static boolean isLoaded(StreamlineFunction function) {
        return getLoadedFunctions().containsValue(function);
    }

    public static TreeMap<String, StreamlineFunction> getEnabledFunctions() {
        TreeMap<String, StreamlineFunction> r = new TreeMap<>();

        for (StreamlineFunction function : getLoadedFunctions().values()) {
            if (function.isEnabled()) r.put(function.getIdentifier(), function);
        }

        return r;
    }

    public static void enableAll() {
        enableThese(getLoadedFunctions().values());
    }

    public static void enableThese(Collection<StreamlineFunction> functions) {
        AtomicInteger count = new AtomicInteger();
        functions.forEach((streamlineFunction) -> {
            if (streamlineFunction.enable()) count.getAndIncrement();
        });

        StreamlineUtilities.getInstance().logInfo("Enabled " + count + " functions!");
    }

    public static void disableAll() {
        disableThese(getLoadedFunctions().values());
    }

    public static void disableThese(Collection<StreamlineFunction> functions) {
        AtomicInteger count = new AtomicInteger();
        functions.forEach((streamlineFunction) -> {
            if (streamlineFunction.disable()) count.getAndIncrement();
        });

        StreamlineUtilities.getInstance().logInfo("Disabled " + count + " functions!");
    }

    public static StreamlineFunction getFunction(String identifier) {
        return getLoadedFunctions().get(identifier);
    }

    public static StreamlineFunction getEnabledFunction(String identifier) {
        return getEnabledFunctions().get(identifier);
    }

    public static List<String> getFunctionIdentifiers() {
        List<String> r = new ArrayList<>();

        getLoadedFunctions().forEach((s, function) -> r.add(s));

        return r;
    }

    public static List<String> getEnabledFunctionIdentifiers() {
        List<String> r = new ArrayList<>();

        getEnabledFunctions().forEach((s, function) -> r.add(s));

        return r;
    }
}
