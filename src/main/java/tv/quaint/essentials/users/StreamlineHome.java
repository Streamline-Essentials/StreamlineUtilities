package tv.quaint.essentials.users;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.savables.users.StreamlineLocation;
import org.jetbrains.annotations.NotNull;

public class StreamlineHome extends StreamlineLocation implements Comparable<StreamlineHome> {
    @Getter
    final String name;
    @Getter @Setter
    String server;

    public StreamlineHome(String name, String server, String world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
        this.name = name;
        this.server = server;
    }

    public StreamlineHome(String string) {
        super(cutToLocation(string));
        String noLocation = cutWithoutLocation(string);
        if (noLocation.startsWith(",")) {
            noLocation = noLocation.substring(1);
        }
        this.name = noLocation.substring(0, noLocation.indexOf(","));
        this.server = noLocation.substring(noLocation.indexOf(",") + 1);
    }

    public static String cutToLocation(String string) {
        string = string.substring(string.lastIndexOf(",")); // Cut to last comma
        string = string.substring(0, string.lastIndexOf(",")); // Do again to cut to second last comma
        return string;
    }

    public static String cutWithoutLocation(String string) {
        String justLocation = cutToLocation(string);
        return string.replace(justLocation, "");
    }

    @Override
    public String toString() {
        return super.toString() + "," + this.name + "," + this.server;
    }

    @Override
    public int compareTo(@NotNull StreamlineHome o) {
        return this.toString().compareTo(o.toString());
    }
}
