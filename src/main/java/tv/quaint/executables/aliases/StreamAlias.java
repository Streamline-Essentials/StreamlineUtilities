package tv.quaint.executables.aliases;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.savables.users.SavableUser;
import tv.quaint.StreamlineUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    public void run(SavableUser savableUser, String[] strings) {
        execution.execute(savableUser);
    }

    @Override
    public List<String> doTabComplete(SavableUser savableUser, String[] strings) {
        if (AliasCompletions.getCompletions(this).containsKey(strings.length)) {
            return AliasCompletions.getCompletions(this).get(strings.length);
        }

        return new ArrayList<>();
    }
}
