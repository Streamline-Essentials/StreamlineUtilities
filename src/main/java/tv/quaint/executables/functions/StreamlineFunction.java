package tv.quaint.executables.functions;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.OperatorUser;
import net.streamline.api.savables.users.SavableUser;
import net.streamline.utils.MatcherUtils;
import net.streamline.utils.UUIDUtils;
import net.streamline.utils.objects.SingleSet;
import org.jetbrains.annotations.NotNull;
import tv.quaint.StreamlineUtilities;
import tv.quaint.executables.ExecutableUser;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StreamlineFunction extends File {
    @Getter @Setter
    private boolean enabled;
    @Getter
    private final boolean valid;
    @Getter
    private final String identifier;

    public StreamlineFunction(File parent, @NotNull String child) {
        super(parent, child);
        this.valid = child.endsWith(".sf");
        this.identifier = this.valid ? child.substring(0, child.lastIndexOf(".")) : child;
    }

    public int run() {
        return runAs(ModuleUtils.getConsole());
    }

    public int runAs(SavableUser user) {
        AtomicInteger count = new AtomicInteger();
        TreeMap<Integer, SingleSet<ExecutableUser<?>, String>> map = getCommandsWithAs(user);
        for (int i : map.keySet()) {
            SingleSet<ExecutableUser<?>, String> set = map.get(i);
            if (set == null) continue;
            if (set.key.runCommand(set.value)) count.getAndIncrement();
        }

        return count.get();
    }

    public TreeMap<Integer, String> lines() {
        try {
            Scanner reader = new Scanner(this);

            TreeMap<Integer, String> lines = new TreeMap<>();
            while (reader.hasNext()) {
                String s = reader.nextLine();
                lines.put(lines.size() + 1, s);
            }
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
            return new TreeMap<>();
        }
    }

    public TreeMap<Integer, String> uncommentedLines() {
        TreeMap<Integer, String> r = new TreeMap<>();

        lines().forEach((integer, s) -> {
            String toAdd = s;
            while (toAdd.startsWith(" ")) {
                toAdd = toAdd.substring(1);
            }
            if (toAdd.startsWith("#")) return;
            r.put(integer, s);
        });

        return r;
    }

    public TreeMap<Integer, SingleSet<ExecutableUser<?>, String>> getCommandsWithAs(SavableUser as) {
        TreeMap<Integer, SingleSet<ExecutableUser<?>, String>> r = new TreeMap<>();

        uncommentedLines().forEach((integer, s) -> {
            if (s.startsWith("@o")) {
                r.put(integer, new SingleSet<>(new ExecutableUser<>(new OperatorUser(as)), s.split(" ", 2)[1]));
            } else if (s.startsWith("@c")) {
                r.put(integer, new SingleSet<>(new ExecutableUser<>(ModuleUtils.getConsole()), s.split(" ", 2)[1]));
            } else if (s.startsWith("@n:")) {
                List<String[]> groups = MatcherUtils.getGroups(MatcherUtils.matcherBuilder("[\\\"](.*?)[\\\"]", s), 1);
                if (groups.size() <= 0) return;
                r.put(integer, new SingleSet<>(new ExecutableUser<>(ModuleUtils.getOrGetUser(UUIDUtils.swapToUUID(groups.get(0)[0]))), s.split(" ", 2)[1]));
            } else if (s.startsWith("@u:")) {
                List<String[]> groups = MatcherUtils.getGroups(MatcherUtils.matcherBuilder("[\\\"](.*?)[\\\"]", s), 1);
                if (groups.size() <= 0) return;
                r.put(integer, new SingleSet<>(new ExecutableUser<>(ModuleUtils.getOrGetUser(UUIDUtils.swapToUUID(groups.get(0)[0]))), s.split(" ", 2)[1]));
            } else {
                r.put(integer, new SingleSet<>(new ExecutableUser<>(as), s));
            }
        });

        return r;
    }

    public boolean isLoaded() {
        return FunctionHandler.isLoaded(this);
    }

    public boolean load() {
        if (! isValid()) return false;
        if (isLoaded()) return false;
        FunctionHandler.loadFunction(this);
        return true;
    }

    public boolean unload() {
        if (! isLoaded()) return false;
        FunctionHandler.unloadFunction(this);
        return true;
    }

    public boolean enable() {
        if (! isValid()) {
            StreamlineUtilities.getInstance().logWarning("Could not enable function with identifier of '" + getIdentifier() + "' as it was invalid!");
            return false;
        }
        if (isEnabled()) return false;
        setEnabled(true);
        StreamlineUtilities.getInstance().logInfo("Enabled function with identifier of '" + getIdentifier() + "'!");
        return true;
    }

    public boolean disable() {
        if (! isEnabled()) return false;
        setEnabled(false);
        StreamlineUtilities.getInstance().logInfo("Enabled function with identifier of '" + getIdentifier() + "'!");
        return true;
    }
}
