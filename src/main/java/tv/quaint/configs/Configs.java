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

    public boolean chatModifyEnabled() {
        reloadResource();

        return resource.getBoolean("chat.modify.modify");
    }

    public String chatModifyPermission() {
        reloadResource();

        return resource.getString("chat.modify.permission");
    }
}
