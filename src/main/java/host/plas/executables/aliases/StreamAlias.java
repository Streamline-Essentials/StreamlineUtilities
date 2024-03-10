package host.plas.executables.aliases;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.command.ModuleCommand;
import host.plas.StreamlineUtilities;
import net.streamline.api.data.console.StreamSender;

import java.io.File;
import java.util.concurrent.ConcurrentSkipListSet;

@Setter
@Getter
public class StreamAlias extends ModuleCommand {
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
    public void run(StreamSender StreamSender, String[] strings) {
        execution.execute(StreamSender);
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamSender StreamSender, String[] strings) {
        if (AliasCompletions.getCompletions(this).containsKey(strings.length)) {
            return AliasCompletions.getCompletions(this).get(strings.length);
        }

        return new ConcurrentSkipListSet<>();
    }
}
