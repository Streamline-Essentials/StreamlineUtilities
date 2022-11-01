package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineUtilities;

public class Messages extends ModularizedConfig {
    public Messages() {
        super(StreamlineUtilities.getInstance(), "messages.yml", true);
    }

    @Override
    public void init() {
        errorsFunctionsNotEnabled();
        errorsFunctionsNotLoaded();
    }

    public String errorsFunctionsNotLoaded() {
        reloadResource();

        return getResource().getOrSetDefault("errors.functions.not.loaded", "&cThat function is not loaded!");
    }

    public String errorsFunctionsNotEnabled() {
        reloadResource();

        return getResource().getOrSetDefault("errors.functions.not.enabled", "&cThat function is not enabled!");
    }
}
