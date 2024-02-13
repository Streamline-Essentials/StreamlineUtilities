package host.plas.essentials.users;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.messages.builders.TeleportMessageBuilder;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.StreamlinePlayer;
import host.plas.StreamlineUtilities;
import host.plas.essentials.EssentialsManager;
import tv.quaint.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class UtilitiesUser extends SavableResource {
    @Getter @Setter
    private ConcurrentSkipListSet<StreamlineHome> homes = new ConcurrentSkipListSet<>();
    @Getter @Setter
    private String lastServer;

    public UtilitiesUser(String uuid) {
        super(uuid, EssentialsManager.newStorageResourceUsers(uuid, UtilitiesUser.class));
    }

    public List<String> getStringListFromResource(String key, List<String> def){
        String defString = StringUtils.listToString(def, "{!!}");
        String s = getStorageResource().getOrSetDefault(key, defString);
        return StringUtils.stringToList(s, "[{][!][!][}]");
    }

    @Override
    public void populateDefaults() {
        List<String> homeStrings = getStringListFromResource("homes", new ArrayList<>());
        homes = new ConcurrentSkipListSet<>();
        homeStrings.forEach((homeString) -> {
            homes.add(new StreamlineHome(homeString));
        });
        lastServer = getOrSetDefault("lastServer", "null");
    }

    @Override
    public void loadValues() {
        List<String> homeStrings = getStringListFromResource("homes", new ArrayList<>());
        homes = new ConcurrentSkipListSet<>();
        homeStrings.forEach((homeString) -> {
            homes.add(new StreamlineHome(homeString));
        });
        lastServer = getOrSetDefault("lastServer", "null");
    }

    @Override
    public void saveAll() {
        List<String> homeStrings = new ArrayList<>();
        homes.forEach((home) -> {
            homeStrings.add(home.toString());
        });
        getStorageResource().write("homes", StringUtils.listToString(homeStrings, "{!!}"));
        getStorageResource().write("lastServer", lastServer);
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

        StreamlinePlayer player = ModuleUtils.getOrGetPlayer(getUuid());
        if (player == null) return;

        ModuleUtils.connect(player, home.getServer());

        ProxiedMessage message = TeleportMessageBuilder.build(player, home, player);
        new EssentialsManager.TeleportRunner(StreamlineUtilities.getConfigs().homesDelayTicks(), message);
    }

    public void goToLastServer() {
        StreamlinePlayer player = ModuleUtils.getOrGetPlayer(getUuid());
        if (player == null) return;

        String lastServer = getLastServer();
        if (Objects.equals(lastServer, "null"))
            lastServer = StreamlineUtilities.getConfigs().lastServerDefaultServer();

        ModuleUtils.connect(player, lastServer);
    }

    public int getHomesCount() {
        return homes.size();
    }
}
