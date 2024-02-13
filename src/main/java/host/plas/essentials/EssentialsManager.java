package host.plas.essentials;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.SLAPI;
import net.streamline.api.configs.given.GivenConfigs;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.scheduler.ModuleDelayedRunnable;
import host.plas.StreamlineUtilities;
import host.plas.essentials.users.UtilitiesUser;
import tv.quaint.storage.StorageUtils;
import tv.quaint.storage.resources.StorageResource;
import tv.quaint.storage.resources.cache.CachedResource;
import tv.quaint.storage.resources.cache.CachedResourceUtils;
import tv.quaint.storage.resources.databases.configurations.DatabaseConfig;
import tv.quaint.storage.resources.flat.FlatFileResource;
import net.streamline.thebase.lib.leonhard.storage.Config;
import net.streamline.thebase.lib.leonhard.storage.Json;
import net.streamline.thebase.lib.leonhard.storage.Toml;
import net.streamline.thebase.lib.mongodb.MongoClient;

import java.io.File;
import java.sql.Connection;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class EssentialsManager {
    public static class TeleportRunner extends ModuleDelayedRunnable {
        @Getter
        @Setter
        private ProxiedMessage message;

        public TeleportRunner(long ticksDelayed, ProxiedMessage message) {
            super(StreamlineUtilities.getInstance(), ticksDelayed);
            this.message = message;
        }

        public TeleportRunner(ProxiedMessage message) {
            this(StreamlineUtilities.getConfigs().getTPADelayTicks(), message);
        }

        @Override
        public void runDelayed() {
            this.message.send();
        }
    }

    static ConcurrentSkipListSet<TPARequest> pendingTPARequests = new ConcurrentSkipListSet<>();

    public static void addTPARequest(TPARequest request) {
        pendingTPARequests.add(request);
    }

    public static void removeTPARequest(TPARequest request) {
        pendingTPARequests.remove(request);
    }

    public static TPARequest getTPARequest(String senderUuid, String receiverUuid, TPARequest.TransportType transportType) {
        AtomicReference<TPARequest> atomicReference = new AtomicReference<>();
        pendingTPARequests.forEach((request) -> {
            if (request.getSender().getUuid().equals(senderUuid) && request.getReceiver().getUuid().equals(receiverUuid) && request.getTransportType().equals(transportType)) {
                atomicReference.set(request);
            }
        });
        return atomicReference.get();
    }

    public static void requestTPA(StreamlinePlayer sender, StreamlinePlayer receiver) {
        TPARequest request = new TPARequest(sender, receiver, TPARequest.TransportType.SENDER_TO_RECEIVER);
        addTPARequest(request);
    }

    public static void requestTPAHere(StreamlinePlayer sender, StreamlinePlayer receiver) {
        TPARequest request = new TPARequest(sender, receiver, TPARequest.TransportType.RECEIVER_TO_SENDER);
        addTPARequest(request);
    }

    public static void acceptTPA(StreamlinePlayer senderPlayer, StreamlinePlayer otherPlayer, TPARequest.TransportType transportType) {
        TPARequest request = getTPARequest(senderPlayer.getUuid(), otherPlayer.getUuid(), transportType);
        if (request != null) {
            request.perform();
        }
    }

    public static void denyTPA(StreamlinePlayer senderPlayer, StreamlinePlayer otherPlayer, TPARequest.TransportType transportType) {
        TPARequest request = getTPARequest(senderPlayer.getUuid(), otherPlayer.getUuid(), transportType);
        if (request != null) {
            request.deny();
        }
    }

    public static ConcurrentSkipListSet<TPARequest> getPendingTPARequests(StreamlinePlayer asPlayerSentTo, TPARequest.TransportType transportType) {
        ConcurrentSkipListSet<TPARequest> requests = new ConcurrentSkipListSet<>();
        pendingTPARequests.forEach((request) -> {
            if (request.getReceiver().getUuid().equals(asPlayerSentTo.getUuid()) && request.getTransportType() == transportType) {
                requests.add(request);
            }
        });
        return requests;
    }

    public static TPARequest getLatestPendingTPARequest(StreamlinePlayer asPlayerSentTo, TPARequest.TransportType transportType) {
        ConcurrentSkipListSet<TPARequest> requests = getPendingTPARequests(asPlayerSentTo, transportType);
        if (requests.size() > 0) {
            return requests.last();
        }
        return null;
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<UtilitiesUser> loadedUsers = new ConcurrentSkipListSet<>();

    public static UtilitiesUser getOrGetUser(StreamlineUser streamlineUser) {
        return getOrGetUser(streamlineUser.getUuid());
    }

    private static UtilitiesUser getUser(String uuid) {
        AtomicReference<UtilitiesUser> atomicReference = new AtomicReference<>();
        loadedUsers.forEach((user) -> {
            if (user.getUuid().equals(uuid)) {
                atomicReference.set(user);
            }
        });
        return atomicReference.get();
    }

    public static void addUser(UtilitiesUser user) {
        loadedUsers.add(user);
        syncUser(user);
    }

    public static void removeUser(UtilitiesUser user) {
        loadedUsers.remove(user);
    }

    public static void saveAllUsers() {
        loadedUsers.forEach(UtilitiesUser::saveAll);
    }

    public static UtilitiesUser getOrGetUser(String uuid) {
        UtilitiesUser user = getUser(uuid);
        if (user != null) return user;

        user = new UtilitiesUser(uuid);
        addUser(user);
        getUserFromDatabase(user);
        return user;
    }

    public static void getUserFromDatabase(UtilitiesUser user) {
        StorageUtils.SupportedStorageType type = StreamlineUtilities.getConfigs().getUserStorageType();
        if (type == StorageUtils.SupportedStorageType.YAML || type == StorageUtils.SupportedStorageType.JSON || type == StorageUtils.SupportedStorageType.TOML) return;

        CachedResource<?> cachedResource = (CachedResource<?>) user.getStorageResource();
        String tableName = SLAPI.getMainDatabase().getConfig().getTablePrefix() + "utilities_users";

        try {
            boolean changed = false;
            switch (GivenConfigs.getMainConfig().savingUseType()) {
                case MONGO:
                case SQLITE:
                case MYSQL:
                    if (! SLAPI.getMainDatabase().exists(tableName)) {
                        return;
                    }
                    CachedResourceUtils.updateCache(tableName, cachedResource.getDiscriminatorKey(), cachedResource.getDiscriminatorAsString(), cachedResource, SLAPI.getMainDatabase());
                    changed = true;
                    break;
            }
            if (changed) user.loadValues();
        } catch (Exception e) {
            syncUser(user);
        }
    }

    public static void syncUser(UtilitiesUser user) {
        switch (StreamlineUtilities.getConfigs().getUserStorageType()) {
            case MONGO:
            case SQLITE:
            case MYSQL:
                CachedResource<?> cachedResource = (CachedResource<?>) user.getStorageResource();
                String tableName = SLAPI.getMainDatabase().getConfig().getTablePrefix() + "utilities_users";
                CachedResourceUtils.pushToDatabase(tableName, cachedResource, SLAPI.getMainDatabase());
                break;
        }
    }

    public static boolean userExists(String uuid) {
        StorageUtils.SupportedStorageType type = StreamlineUtilities.getConfigs().getUserStorageType();
        DatabaseConfig config = GivenConfigs.getMainConfig().getConfiguredDatabase();
        File userFolder = SLAPI.getUserFolder();
        switch (type) {
            case YAML:
                File[] files = userFolder.listFiles();
                if (files == null) return false;

                for (File file : files) {
                    if (file.getName().equals(uuid + ".yml")) return true;
                }
                return false;
            case JSON:
                File[] files2 = userFolder.listFiles();
                if (files2 == null) return false;

                for (File file : files2) {
                    if (file.getName().equals(uuid + ".json")) return true;
                }
                return false;
            case TOML:
                File[] files3 = userFolder.listFiles();
                if (files3 == null) return false;

                for (File file : files3) {
                    if (file.getName().equals(uuid + ".toml")) return true;
                }
                return false;
            case MONGO:
            case MYSQL:
            case SQLITE:
                return SLAPI.getMainDatabase().exists(SLAPI.getMainDatabase().getConfig().getTablePrefix() + "utilities_users", "uuid", uuid);
            default:
                return false;
        }
    }

    public static StorageResource<?> newStorageResourceUsers(String uuid, Class<? extends SavableResource> clazz) {
        switch (StreamlineUtilities.getConfigs().getUserStorageType()) {
            case YAML:
                return new FlatFileResource<>(Config.class, uuid + ".yml", StreamlineUtilities.getUsersFolder(), false);
            case JSON:
                return new FlatFileResource<>(Json.class, uuid + ".json", StreamlineUtilities.getUsersFolder(), false);
            case TOML:
                return new FlatFileResource<>(Toml.class, uuid + ".toml", StreamlineUtilities.getUsersFolder(), false);
            case MONGO:
                return new CachedResource<>(MongoClient.class, "uuid", uuid);
            case MYSQL:
            case SQLITE:
                return new CachedResource<>(Connection.class, "uuid", uuid);
        }

        return null;
    }
}
