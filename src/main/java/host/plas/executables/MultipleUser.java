package host.plas.executables;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.data.console.StreamSender;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class MultipleUser {
    @Getter @Setter
    private ConcurrentSkipListSet<StreamSender> users;

    public MultipleUser() {
        this.users = new ConcurrentSkipListSet<>();
    }

    public MultipleUser(StreamSender... users) {
        this.users = new ConcurrentSkipListSet<>(List.of(users));
    }

    public MultipleUser(ConcurrentSkipListSet<StreamSender> users) {
        this.users = users;
    }

    public void add(String uuid) {
        Optional<StreamSender> user = UserUtils.getOrGetSender(uuid);
        user.ifPresent(this::add);
    }

    public void add(StreamSender user) {
        users.add(user);
    }

    public void remove(String uuid) {
        new ArrayList<>(users).forEach(a -> {
            if (a.getUuid().equals(uuid)) remove(a);
        });
    }

    public void remove(StreamSender user) {
        users.remove(user);
    }
}
