package host.plas.events;

import lombok.Getter;
import net.streamline.api.events.modules.ModuleEvent;
import host.plas.StreamlineUtilities;
import host.plas.essentials.TPARequest;

@Getter
public class TPATimeoutEvent extends ModuleEvent {
    final TPARequest request;

    public TPATimeoutEvent(TPARequest tpaRequest) {
        super(StreamlineUtilities.getInstance());
        this.request = tpaRequest;
    }
}
