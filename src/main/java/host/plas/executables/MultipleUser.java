package host.plas.executables;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class MultipleUser {
    @Getter @Setter
    private ConcurrentSkipListSet<StreamlineUser> users;

    public MultipleUser() {
        this.users = new ConcurrentSkipListSet<>();
    }

    public MultipleUser(StreamlineUser... users) {
        this.users = new ConcurrentSkipListSet<>(List.of(users));
    }

    public MultipleUser(ConcurrentSkipListSet<StreamlineUser> users) {
        this.users = users;
    }

    public void add(String uuid) {
        add(ModuleUtils.getOrGetUser(uuid));
    }

    public void add(StreamlineUser user) {
        users.add(user);
    }

    public void remove(String uuid) {
        new ArrayList<>(users).forEach(a -> {
            if (a.getUuid().equals(uuid)) remove(a);
        });
    }

    public void remove(StreamlineUser user) {
        users.remove(user);
    }
}
