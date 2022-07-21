package tv.quaint.executables;

import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.OperatorUser;
import net.streamline.api.savables.users.SavableUser;

public record ExecutableUser<T>(T user) {
    public boolean runCommand(String command) {
        if (user instanceof SavableUser savableUser) {
            return ModuleUtils.runAs(savableUser, command);
        }
        if (user instanceof OperatorUser operatorUser) {
            return ModuleUtils.runAs(operatorUser, command);
        }
        return false;
    }
}
