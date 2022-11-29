package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineUtilities;

public class Configs extends ModularizedConfig {
    public Configs() {
        super(StreamlineUtilities.getInstance(), "config.yml", true);
    }

    @Override
    public void init() {
        chatModifyEnabled();
        chatModifyPermission();
        isNicknamesEnabled();
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

    public long tpaTimeout() {
        reloadResource();

        return getResource().getOrSetDefault("tpa.timeout", 600L);
    }
}
