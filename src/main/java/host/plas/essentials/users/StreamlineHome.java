package host.plas.essentials.users;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.data.players.StreamPlayer;
import net.streamline.api.data.players.location.PlayerLocation;
import net.streamline.api.data.players.location.PlayerRotation;
import net.streamline.api.data.players.location.PlayerWorld;
import net.streamline.api.data.players.location.WorldPosition;
import net.streamline.api.data.server.StreamServer;
import net.streamline.api.modules.ModuleUtils;

@Getter @Setter
public class StreamlineHome extends PlayerLocation {
    private String name;
    private StreamServer server;

    public StreamlineHome(String name, String server, String world, double x, double y, double z, float yaw, float pitch) {
        super(null, new PlayerWorld(world), new WorldPosition(x, y, z), new PlayerRotation(yaw, pitch));
        this.name = name;
        this.server = new StreamServer(server);
    }

    public void teleport(StreamPlayer player) {
        ModuleUtils.teleport(player, this);
    }
}
