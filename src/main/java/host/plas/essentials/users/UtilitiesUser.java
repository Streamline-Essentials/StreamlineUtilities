package host.plas.essentials.users;

import host.plas.StreamlineUtilities;
import host.plas.essentials.EssentialsManager;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.data.IUuidable;
import net.streamline.api.data.players.StreamPlayer;
import net.streamline.api.messages.builders.TeleportMessageBuilder;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.thebase.lib.re2j.Matcher;
import tv.quaint.utils.MatcherUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

@Setter
@Getter
public class UtilitiesUser implements IUuidable {
    private String uuid;

    private ConcurrentSkipListSet<StreamlineHome> homes;
    private String lastServer;

    public UtilitiesUser(String uuid) {
        this.uuid = uuid;

        homes = new ConcurrentSkipListSet<>();
        lastServer = "";
    }

    public String computableHomes() {
        StringBuilder builder = new StringBuilder();

        homes.forEach((home) -> {
            builder.append("!!!")
                    .append(home.getServer()).append("::")
                    .append(home.getName()).append("::")
                    .append(home.getWorld()).append("::")
                    .append(home.getX()).append("::")
                    .append(home.getY()).append("::")
                    .append(home.getZ()).append("::")
                    .append(home.getYaw()).append("::")
                    .append(home.getPitch())
                    .append(";;;");
        });

        return builder.toString();
    }

    public static ConcurrentSkipListSet<StreamlineHome> computableHomes(String homes) {
        ConcurrentSkipListSet<StreamlineHome> r = new ConcurrentSkipListSet<>();

        Matcher matcher = MatcherUtils.matcherBuilder("!!!([^:]+)::([^:]+)::([^:]+)::([^:]+)::([^:]+)::([^:]+)::([^:]+)::([^:]+);;;", homes);
        List<String[]> groups = MatcherUtils.getGroups(matcher, 8);
        groups.forEach((group) -> {
            try {
                r.add(new StreamlineHome(
                        group[0], group[1], group[2],
                        Double.parseDouble(group[3]),
                        Double.parseDouble(group[4]),
                        Double.parseDouble(group[5]),
                        Float.parseFloat(group[6]),
                        Float.parseFloat(group[7])));
            } catch (Exception e) {
                // ignore
            }
        });

        return r;
    }

    public void save() {
        StreamlineUtilities.getKeeper().save(this);
    }

    public void addHome(StreamlineHome home) {
        homes.add(home);
    }

    public void removeHome(StreamlineHome home) {
        homes.remove(home);
    }

    public void removeHome(String name) {
        homes.forEach((home) -> {
            if (home.getName().equals(name)) {
                homes.remove(home);
            }
        });
    }

    public StreamlineHome getHome(String name) {
        AtomicReference<StreamlineHome> atomicReference = new AtomicReference<>();

        homes.forEach((home) -> {
            if (home.getName().equals(name)) {
                atomicReference.set(home);
            }
        });

        return atomicReference.get();
    }

    public ConcurrentSkipListSet<StreamlineHome> getHomesOnServer(String server) {
        ConcurrentSkipListSet<StreamlineHome> homesOnServer = new ConcurrentSkipListSet<>();

        homes.forEach((home) -> {
            if (home.getServer().equals(server)) {
                homesOnServer.add(home);
            }
        });

        return homesOnServer;
    }

    public void teleportTo(String homeName) {
        StreamlineHome home = getHome(homeName);
        if (home == null) return;

        StreamPlayer player = ModuleUtils.getOrGetPlayer(getUuid()).orElse(null);
        if (player == null) return;

        ModuleUtils.connect(player, home.getServer().getIdentifier());

        ProxiedMessage message = TeleportMessageBuilder.build(player, home, player);
        new EssentialsManager.TeleportRunner(StreamlineUtilities.getConfigs().homesDelayTicks(), message);
    }

    public void goToLastServer() {
        StreamPlayer player = ModuleUtils.getOrGetPlayer(getUuid()).orElse(null);
        if (player == null) return;

        String lastServer = getLastServer();
        if (Objects.equals(lastServer, "null"))
            lastServer = StreamlineUtilities.getConfigs().lastServerDefaultServer();

        ModuleUtils.connect(player, lastServer);
    }

    public int getHomesCount() {
        return homes.size();
    }

    public void register() {
        EssentialsManager.registerUser(this);
    }

    public void unregister() {
        EssentialsManager.unregisterUser(this);
    }
}
