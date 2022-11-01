package tv.quaint.configs;

import lombok.Getter;
import net.streamline.api.configs.ModularizedConfig;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.placeholder.CustomPlaceholder;
import tv.quaint.StreamlineUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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
}
