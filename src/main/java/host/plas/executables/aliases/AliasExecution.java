package host.plas.executables.aliases;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.data.console.StreamSender;
import net.streamline.api.modules.ModuleUtils;
import host.plas.StreamlineUtilities;
import host.plas.executables.ExecutableHandler;

public class AliasExecution {
    @Getter @Setter
    private Type type;
    @Getter @Setter
    private String execution;

    public AliasExecution(Type type, String execution) {
        this.type = type;
        this.execution = execution;
    }

    public enum Type {
        COMMAND,
        FUNCTION,
        SCRIPT,
        ;
    }

    public boolean execute(StreamSender sender) {
        switch (getType()) {
            case COMMAND:
                ModuleUtils.runAs(sender, execution);
                break;
            case SCRIPT:
                return false;
            case FUNCTION:
                if (! ExecutableHandler.isFunctionLoadedByName(execution)) {
                    ModuleUtils.sendMessage(sender, StreamlineUtilities.getMessages().errorsFunctionsNotLoaded());
                    return false;
                }
                if (! ExecutableHandler.isFunctionEnabledByName(execution)) {
                    ModuleUtils.sendMessage(sender, StreamlineUtilities.getMessages().errorsFunctionsNotEnabled());
                    return false;
                }
                ExecutableHandler.getFunction(execution).runAs(sender);
                break;
        }
        return true;
    }
}
