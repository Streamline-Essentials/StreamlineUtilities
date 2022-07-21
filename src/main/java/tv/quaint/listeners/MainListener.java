package tv.quaint.listeners;

import net.streamline.api.events.StreamlineEvent;
import net.streamline.api.events.StreamlineEventBus;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.base.events.StreamlineChatEvent;
import tv.quaint.StreamlineUtilities;

public class MainListener extends StreamlineEventBus.StreamlineObserver {
    @Override
    protected void update(StreamlineEvent<?> streamlineEvent) {
        if (streamlineEvent instanceof StreamlineChatEvent chatEvent) {
            if (StreamlineUtilities.getConfigs().chatModify()) chatEvent.setMessage(ModuleUtils.getRATAPI().parseAllPlaceholders(chatEvent.getSender(), chatEvent.getMessage()));
        }
    }
}
