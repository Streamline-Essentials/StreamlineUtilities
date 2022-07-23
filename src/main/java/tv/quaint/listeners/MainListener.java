package tv.quaint.listeners;

import net.streamline.api.events.EventProcessor;
import net.streamline.api.events.StreamlineEvent;
import net.streamline.api.events.StreamlineListener;
import net.streamline.api.events.server.LoginEvent;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.base.events.StreamlineChatEvent;
import net.streamline.utils.MessagingUtils;
import tv.quaint.StreamlineUtilities;

public class MainListener implements StreamlineListener {
    @EventProcessor
    public void onChat(StreamlineChatEvent chatEvent) {
        if (StreamlineUtilities.getConfigs().chatModifyEnabled()) {
            if (! ModuleUtils.hasPermission(chatEvent.getSender(), StreamlineUtilities.getConfigs().chatModifyPermission())) return;

            String message = ModuleUtils.replaceAllPlayerBungee(chatEvent.getSender(), chatEvent.getMessage());
            chatEvent.setMessage(message);
        }
    }
}
