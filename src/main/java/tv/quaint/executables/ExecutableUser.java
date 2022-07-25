package tv.quaint.executables;

import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.OperatorUser;
import net.streamline.api.savables.users.SavableUser;

import java.util.concurrent.atomic.AtomicInteger;

public record ExecutableUser<T>(T user) {
    private static final int SUCCESS = 1;
    private static final int FAIL = -1;

    public int runCommand(String command) {
        if (user instanceof SavableUser savableUser) {
            return ModuleUtils.runAs(savableUser, command) ? SUCCESS : FAIL;
        }
        if (user instanceof OperatorUser operatorUser) {
            return ModuleUtils.runAs(operatorUser, command) ? SUCCESS : FAIL;
        }
        if (user instanceof MultipleUser multipleUser) {
            AtomicInteger c = new AtomicInteger();
            multipleUser.getUsers().forEach(a -> {
                if (ModuleUtils.runAs(a, command)) c.getAndIncrement();
            });
            return c.get();
        }
        return FAIL;
    }
}
