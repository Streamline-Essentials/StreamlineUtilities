package tv.quaint;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.SimpleModule;
import net.streamline.api.modules.dependencies.Dependency;
import net.streamline.api.placeholder.RATExpansion;
import tv.quaint.commands.*;
import tv.quaint.configs.Configs;
import tv.quaint.configs.CustomPlaceholdersConfig;
import tv.quaint.configs.GroupedPermissionConfig;
import tv.quaint.configs.Messages;
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
    static GroupedPermissionConfig groupedPermissionConfig;
    @Getter
    static Configs configs;
    @Getter
    static Messages messages;
    @Getter
    static MainListener mainListener;

    @Getter
    static UtilitiesExpansion utilitiesExpansion;

    @Getter
    static List<AliasGetter> getters;

    @Override
    public String identifier() {
        return "streamline-utils";
    }

    @Override
    public List<String> authors() {
        return List.of(
                "Quaint"
        );
    }

    @Override
    public List<Dependency> dependencies() {
        return Collections.emptyList();
    }

    @Override
    public void registerCommands() {
        setCommands(List.of(
                new FunctionCommand(),
                new BroadcastCommand(),
                new TextCommand(),
                new TitleCommand(),
                new OnlineCommand()
        ));
    }

    @Override
    public void onLoad() {
        instance = this;

        getters = List.of(new AliasGetter("servers", ModuleUtils::getServerNames),
                new AliasGetter("online_names", () -> {
                    List<String> r = new ArrayList<>();

                    ModuleUtils.getLoadedUsers().forEach(a -> {
//            if (a.online && ! (a instanceof SavableConsole))
                        if (a.online) r.add(a.getName());
                    });

                    return r;
                }),
                new AliasGetter("online_uuids", () -> {
                    List<String> r = new ArrayList<>();

                    ModuleUtils.getLoadedUsers().forEach(a -> {
//            if (a.online && ! (a instanceof SavableConsole))
                        if (a.online) r.add(a.uuid);
                    });

                    return r;
                }),
                new AliasGetter("loaded_names", () -> {
                    List<String> r = new ArrayList<>();

                    ModuleUtils.getLoadedUsers().forEach(a -> {
                        r.add(a.getName());
                    });

                    return r;
                }),
                new AliasGetter("loaded_uuids", () -> {
                    List<String> r = new ArrayList<>();

                    ModuleUtils.getLoadedUsers().forEach(a -> {
                        r.add(a.uuid);
                    });

                    return r;
                }));

        getGetters().forEach(a -> {
            ExecutableHandler.unloadGetter(a);
            ExecutableHandler.loadGetter(a);
        });
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
        groupedPermissionConfig = new GroupedPermissionConfig();

        configs = new Configs();
        messages = new Messages();

        mainListener = new MainListener();
        ModuleUtils.listen(mainListener, this);

        RATExpansion expansion = ModuleUtils.getRATAPI().getExpansionByIdentifier("utils");
        while (expansion != null) {
            ModuleUtils.getRATAPI().unregisterExpansion(expansion);
            expansion = ModuleUtils.getRATAPI().getExpansionByIdentifier("utils");
        }
        utilitiesExpansion = new UtilitiesExpansion();
    }

    @Override
    public void onDisable() {
        ExecutableHandler.unloadAllAliases();
        ExecutableHandler.disableAllFunctions();
        ExecutableHandler.unloadAllFunctions();

//        getUtilitiesExpansion().disable();
        ModuleUtils.getRATAPI().unregisterExpansion(getUtilitiesExpansion());
    }
}
