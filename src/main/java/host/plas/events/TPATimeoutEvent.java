package host.plas.events;

import lombok.Getter;
import net.streamline.api.events.modules.ModuleEvent;
import host.plas.StreamlineUtilities;
import host.plas.essentials.TPARequest;

public class TPATimeoutEvent extends ModuleEvent {
    @Getter
    final TPARequest request;

    public TPATimeoutEvent(TPARequest tpaRequest) {
        super(StreamlineUtilities.getInstance());
        this.request = tpaRequest;
    }
}
