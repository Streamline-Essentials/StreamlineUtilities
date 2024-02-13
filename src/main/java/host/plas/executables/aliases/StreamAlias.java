package host.plas.executables.aliases;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.savables.users.StreamlineUser;
import host.plas.StreamlineUtilities;

import java.io.File;
import java.util.concurrent.ConcurrentSkipListSet;

public class StreamAlias extends ModuleCommand {
    @Getter @Setter
    private AliasExecution execution;


    public StreamAlias(String base, File folder) {
        super(StreamlineUtilities.getInstance(),
                base,
                "streamline.utils.alias.<change-this>",
                folder,
                "change-this-too");
        AliasExecution.Type aeType = AliasExecution.Type.valueOf(getCommandResource().getOrSetDefault("execution.type", AliasExecution.Type.FUNCTION.toString()));
        String aeExecutes = getCommandResource().getOrSetDefault("execution.executes", "change-this");
        this.execution = new AliasExecution(aeType, aeExecutes);
    }

    @Override
    public void run(StreamlineUser StreamlineUser, String[] strings) {
        execution.execute(StreamlineUser);
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        if (AliasCompletions.getCompletions(this).containsKey(strings.length)) {
            return AliasCompletions.getCompletions(this).get(strings.length);
        }

        return new ConcurrentSkipListSet<>();
    }
}
