package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineUtilities;

public class Messages extends ModularizedConfig {
    public Messages() {
        super(StreamlineUtilities.getInstance(), "messages.yml", true);
    }

    public String errorsFunctionsNotLoaded() {
        reloadResource();

        return resource.getString("errors.functions.not.loaded");
    }

    public String errorsFunctionsNotEnabled() {
        reloadResource();

        return resource.getString("errors.functions.not.enabled");
    }
}
