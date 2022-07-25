package tv.quaint.executables;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.SavableUser;

import java.util.ArrayList;
import java.util.List;

public class MultipleUser {
    @Getter @Setter
    private List<SavableUser> users;

    public MultipleUser() {
        this.users = new ArrayList<>();
    }

    public MultipleUser(SavableUser... users) {
        this.users = List.of(users);
    }

    public MultipleUser(List<SavableUser> users) {
        this.users = users;
    }

    public void add(String uuid) {
        add(ModuleUtils.getOrGetUser(uuid));
    }

    public void add(SavableUser user) {
        users.add(user);
    }

    public void remove(String uuid) {
        new ArrayList<>(users).forEach(a -> {
            if (a.uuid.equals(uuid)) remove(a);
        });
    }

    public void remove(SavableUser user) {
        users.remove(user);
    }
}
