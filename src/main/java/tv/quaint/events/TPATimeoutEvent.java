package tv.quaint.events;

import lombok.Getter;
import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.StreamlineUtilities;
import tv.quaint.essentials.TPARequest;

public class TPATimeoutEvent extends ModuleEvent {
    @Getter
    final TPARequest request;

    public TPATimeoutEvent(TPARequest tpaRequest) {
        super(StreamlineUtilities.getInstance());
        this.request = tpaRequest;
    }
}
