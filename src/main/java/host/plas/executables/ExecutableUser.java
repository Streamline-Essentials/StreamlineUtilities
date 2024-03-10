package host.plas.executables;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.data.console.StreamSender;
import net.streamline.api.modules.ModuleUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class ExecutableUser<T> {
    @Getter @Setter
    private T user;

    public ExecutableUser(T user) {
        this.user = user;
    }

    private static final int SUCCESS = 1;
    private static final int FAIL = -1;

    public int runCommand(String command) {
        if (user instanceof StreamSender) {
            StreamSender StreamSender = (StreamSender) user;
            return ModuleUtils.runAs(StreamSender, command) ? SUCCESS : FAIL;
        }
//        if (user instanceof OperatorUser) {
//            OperatorUser operatorUser = (OperatorUser) user;
//            return ModuleUtils.runAs(operatorUser, command) ? SUCCESS : FAIL;
//        }
        if (user instanceof MultipleUser) {
            MultipleUser multipleUser = (MultipleUser) user;
            AtomicInteger c = new AtomicInteger();
            multipleUser.getUsers().forEach(a -> {
                if (ModuleUtils.runAs(a, command)) c.getAndIncrement();
            });
            return c.get();
        }
        return FAIL;
    }
}
