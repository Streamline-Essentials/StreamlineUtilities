package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineUtilities;
import tv.quaint.essentials.configured.ConfiguredBlacklist;
import tv.quaint.essentials.configured.ConfiguredPermissionsList;
import tv.quaint.storage.StorageUtils;
import tv.quaint.storage.resources.databases.configurations.DatabaseConfig;

public class Configs extends ModularizedConfig {
    public Configs() {
        super(StreamlineUtilities.getInstance(), "config.yml", true);
    }

    @Override
    public void init() {
        chatModifyEnabled();
        chatModifyPermission();
        isNicknamesEnabled();

        getTPATimeout();
        getTPADelayTicks();
        getTPABlacklist();

        lastServerEnabled();
        lastServerPermissionRequired();
        lastServerPermissionValue();
        lastServerDefaultServer();

        homesEnabled();
        homesDelayTicks();
        getHomesPermissions();
        getHomesBlacklist();
    }

    public boolean chatModifyEnabled() {
        reloadResource();

        return getResource().getOrSetDefault("chat.modify.modify", true);
    }

    public String chatModifyPermission() {
        reloadResource();

        return getResource().getOrSetDefault("chat.modify.permission", "streamline.utils.chat.modify");
    }

    public boolean isNicknamesEnabled() {
        reloadResource();

        return getResource().getOrSetDefault("nicknames.enabled", true);
    }

    public String getNicknamesFormat() {
        reloadResource();

        return getResource().getOrSetDefault("nicknames.format", "%streamline_user_prefix%%this_input%%streamline_user_suffix%");
    }

    public long getTPATimeout() {
        reloadResource();

        return getResource().getOrSetDefault("tpa.timeout", 600L);
    }

    public long getTPADelayTicks() {
        reloadResource();

        return getResource().getOrSetDefault("tpa.delay-ticks", 20L);
    }

    public ConfiguredBlacklist getTPABlacklist() {
        reloadResource();

        return new ConfiguredBlacklist(getResource().getSection("tpa.blacklist"));
    }

    public boolean lastServerEnabled() {
        reloadResource();

        return getResource().getOrSetDefault("last-server.enabled", false);
    }

    public boolean lastServerPermissionRequired() {
        reloadResource();

        return getResource().getOrSetDefault("last-server.permission.required", true);
    }

    public String lastServerPermissionValue() {
        reloadResource();

        return getResource().getOrSetDefault("last-server.permission.value", "streamline.utils.last-server");
    }

    public String lastServerDefaultServer() {
        reloadResource();

        return getResource().getOrSetDefault("last-server.default-server", "hub");
    }

    public boolean homesEnabled() {
        reloadResource();

        return getResource().getOrSetDefault("homes.enabled", true);
    }

    public int homesDelayTicks() {
        reloadResource();

        return getResource().getOrSetDefault("homes.delay-ticks", 20);
    }

    public ConfiguredPermissionsList getHomesPermissions() {
        reloadResource();

        return new ConfiguredPermissionsList(getResource().getSection("homes.permissions"));
    }

    public ConfiguredBlacklist getHomesBlacklist() {
        reloadResource();

        return new ConfiguredBlacklist(getResource().getSection("homes.blacklist"));
    }

    public StorageUtils.SupportedStorageType getUserStorageType() {
        reloadResource();

        return StorageUtils.SupportedStorageType.valueOf(getResource().getOrSetDefault("saving.use", "YAML"));
    }

    public String getUserStorageURI() {
        reloadResource();

        return getResource().getOrSetDefault("saving.databases.uri", "jdbc:mysql://localhost:3306/utilities_users?useSSL=false");
    }

    public String getUserStoragePrefix() {
        reloadResource();

        return getResource().getOrSetDefault("saving.databases.prefix", "utilities_");
    }

    public DatabaseConfig getUserStorageDatabaseConfig() {
        StorageUtils.SupportedDatabaseType type;
        try {
            type = StorageUtils.SupportedDatabaseType.valueOf(getUserStorageType().name());
        } catch (IllegalArgumentException e) {
            return null;
        }

        return new DatabaseConfig(type, getUserStorageURI(), getUserStoragePrefix());
    }
}
