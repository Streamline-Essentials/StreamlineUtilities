package host.plas.events;

import net.streamline.api.savables.events.UserNameUpdateEvent;
import net.streamline.api.savables.users.StreamlineUser;

public class NicknameUpdateEvent extends UserNameUpdateEvent {
    public NicknameUpdateEvent(StreamlineUser user, String changeTo, String changeFrom) {
        super(user, changeTo, changeFrom);
    }
}
