package carpet;

import carpet.commands.CameraModeCommand;
import carpet.commands.CounterCommand;
import carpet.settings.SettingsManager;
import carpet.utils.MobAI;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class CarpetServer {
    public static SettingsManager settingsManager;
    public static final List<CarpetExtension> extensions = new ArrayList<>();
    public static MinecraftServer minecraft_server;
    private static CommandDispatcher<CommandSource> currentCommandDispatcher;

    public static void onGameStarted(){
        settingsManager = new SettingsManager(CarpetSettings.carpetVersion, "carpet", "TISCarpet");
        settingsManager.parseSettingsClass(CarpetSettings.class);
        extensions.forEach(CarpetExtension::onGameStarted);
    }

    public static void onServerLoaded(MinecraftServer server){
        CarpetServer.minecraft_server = server;
        settingsManager.attachServer(server);
        extensions.forEach(e -> {
            SettingsManager sm = e.customSettingsManager();
            if (sm != null) sm.attachServer(server);
            e.onServerClosed(server);
        });
        MobAI.resetTrackers();
    }

    public static void tick(MinecraftServer server){
        //in case something happens
        CarpetSettings.impendingFillSkipUpdates = false;
        CarpetSettings.currentTelepotingEntityBox = null;
        CarpetSettings.fixedPosition = null;

        extensions.forEach(e -> e.onTick(server));
    }

    public static void registerCarpetCommands(CommandDispatcher<CommandSource> dispatcher){
        CameraModeCommand.register(dispatcher);
        CounterCommand.register(dispatcher);

        extensions.forEach(e -> e.registerCommands(dispatcher));
        currentCommandDispatcher = dispatcher;
    }
}
