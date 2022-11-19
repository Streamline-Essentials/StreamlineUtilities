package tv.quaint;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.SimpleModule;
import net.streamline.api.modules.dependencies.Dependency;
import net.streamline.api.placeholder.RATExpansion;
import org.pf4j.PluginWrapper;
import tv.quaint.commands.*;
import tv.quaint.configs.*;
import tv.quaint.executables.ExecutableHandler;
import tv.quaint.executables.aliases.AliasGetter;
import tv.quaint.listeners.MainListener;
import tv.quaint.ratapi.UtilitiesExpansion;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class StreamlineUtilities extends SimpleModule {
    @Getter
    static StreamlineUtilities instance;
    @Getter
    static File executablesFolder;
    @Getter
    static File functionsFolder;
    @Getter
    static File scriptsFolder;
    @Getter
    static File aliasesFolder;
    @Getter
    static CustomPlaceholdersConfig customPlaceholdersConfig;
    @Getter
    static ServerAliasesConfig serverAliasesConfig;
    @Getter
    static GroupedPermissionConfig groupedPermissionConfig;
    @Getter
    static MaintenanceConfig maintenanceConfig;
    @Getter
    static Configs configs;
    @Getter
    static Messages messages;
    @Getter
    static MainListener mainListener;

    @Getter
    static UtilitiesExpansion utilitiesExpansion;

    @Getter
    static ConcurrentSkipListSet<AliasGetter> getters;

    public StreamlineUtilities(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void registerCommands() {
        setCommands(List.of(
                new FunctionCommand(),
                new BroadcastCommand(),
                new TextCommand(),
                new TitleCommand(),
                new OnlineCommand(),
                new TeleportCommand(),
                new MaintenanceCommand(),
                new WhitelistCommand(),
                new NickCommand(),
                new SudoCommand(),
                new SudoOpCommand()
        ));

//        if (getConfigs().isNicknamesEnabled()) getCommands().add(new NickCommand());
    }

    @Override
    public void onLoad() {
        instance = this;

        getters = new ConcurrentSkipListSet<>(List.of(new AliasGetter("servers", ModuleUtils::getServerNames),
                new AliasGetter("online_names", () -> {
                    ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();

                    ModuleUtils.getLoadedUsersSet().forEach(a -> {
//            if (a.online && ! (a instanceof SavableConsole))
                        if (a.isOnline()) r.add(a.getName());
                    });

                    return r;
                }),
                new AliasGetter("online_uuids", () -> {
                    ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();

                    ModuleUtils.getLoadedUsersSet().forEach(a -> {
//            if (a.online && ! (a instanceof SavableConsole))
                        if (a.isOnline()) r.add(a.getUuid());
                    });

                    return r;
                }),
                new AliasGetter("loaded_names", () -> {
                    ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();

                    ModuleUtils.getLoadedUsersSet().forEach(a -> {
                        r.add(a.getName());
                    });

                    return r;
                }),
                new AliasGetter("loaded_uuids", () -> {
                    ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();

                    ModuleUtils.getLoadedUsersSet().forEach(a -> {
                        r.add(a.getUuid());
                    });

                    return r;
                })));

        getGetters().forEach(a -> {
            ExecutableHandler.unloadGetter(a);
            ExecutableHandler.loadGetter(a);
        });
        utilitiesExpansion = new UtilitiesExpansion();
    }

    @Override
    public void onEnable() {
        executablesFolder = new File(getDataFolder(), "executables" + File.separator);
        executablesFolder.mkdirs();
        functionsFolder = new File(getExecutablesFolder(), "functions" + File.separator);
        if (functionsFolder.mkdirs()) {
            String dstring = "default-function.sf";
            File file = new File(functionsFolder, dstring);
            try (InputStream in = getResourceAsStream(dstring)) {
                assert in != null;
                Files.copy(in, file.toPath());
                logInfo("Set up default file: " + dstring);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        scriptsFolder = new File(getExecutablesFolder(), "scripts" + File.separator);
        scriptsFolder.mkdirs();
        aliasesFolder = new File(getExecutablesFolder(), "aliases" + File.separator);
        if (aliasesFolder.mkdirs()) {
            String hubstring = "hub-alias.yml";
            File hfile = new File(aliasesFolder, hubstring);
            try (InputStream in = getResourceAsStream(hubstring)) {
                assert in != null;
                Files.copy(in, hfile.toPath());
                logInfo("Set up default file: " + hubstring);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String lobbystring = "lobby-alias.yml";
            File lfile = new File(aliasesFolder, lobbystring);
            try (InputStream in = getResourceAsStream(lobbystring)) {
                assert in != null;
                Files.copy(in, lfile.toPath());
                logInfo("Set up default file: " + lobbystring);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ExecutableHandler.loadFunctions(functionsFolder);
        ExecutableHandler.enableAllFunctions();
        ExecutableHandler.loadAllAliases(aliasesFolder);

        customPlaceholdersConfig = new CustomPlaceholdersConfig();
        serverAliasesConfig = new ServerAliasesConfig();
        groupedPermissionConfig = new GroupedPermissionConfig();
        maintenanceConfig = new MaintenanceConfig();

        configs = new Configs();
        messages = new Messages();

        mainListener = new MainListener();
        ModuleUtils.listen(mainListener, this);

        RATExpansion expansion = ModuleUtils.getRATAPI().getExpansionByIdentifier("utils");
        while (expansion != null) {
            ModuleUtils.getRATAPI().unregisterExpansion(expansion);
            expansion = ModuleUtils.getRATAPI().getExpansionByIdentifier("utils");
        }
        getUtilitiesExpansion().register();
    }

    @Override
    public void onDisable() {
        ExecutableHandler.unloadAllAliases();
        ExecutableHandler.disableAllFunctions();
        ExecutableHandler.unloadAllFunctions();

        getUtilitiesExpansion().unregister();
    }

    @Override
    public String getIdentifier() {
        return identifier();
    }
}
