package carpet;

import carpet.commands.*;
import carpet.helpers.TickSpeed;
import carpet.logging.LoggerRegistry;
import carpet.network.ServerNetworkHandler;
import carpet.script.CarpetScriptServer;
import carpet.settings.SettingsManager;
import carpet.utils.HUDController;
import carpet.utils.MobAI;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class CarpetServer {
    public static SettingsManager settingsManager;
    public static final List<CarpetExtension> extensions = new ArrayList<>();
    public static MinecraftServer minecraft_server;
    private static CommandDispatcher<CommandSource> currentCommandDispatcher;
    public static CarpetScriptServer scriptServer;

    // Separate from onServerLoaded, because a server can be loaded multiple times in singleplayer
    public static void manageExtension(CarpetExtension extension) {
        extensions.add(extension);
        // for extensions that come late to the party, after server is created / loaded
        // we will handle them now.
        // that would handle all extensions, even these that add themselves really late to the party
        if (currentCommandDispatcher != null)
        {
            extension.registerCommands(currentCommandDispatcher);
        }
    }

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
            e.onServerLoaded(server);
        });
        scriptServer = new CarpetScriptServer();
        MobAI.resetTrackers();
        LoggerRegistry.initLoggers();
    }

    public static void tick(MinecraftServer server){
        TickSpeed.tick(server);
        HUDController.update_hud(server);
        scriptServer.events.tick();
        //in case something happens
        CarpetSettings.impendingFillSkipUpdates = false;
        CarpetSettings.currentTelepotingEntityBox = null;
        CarpetSettings.fixedPosition = null;

        extensions.forEach(e -> e.onTick(server));
    }

    public static void registerCarpetCommands(CommandDispatcher<CommandSource> dispatcher){
        CameraModeCommand.register(dispatcher);
        CounterCommand.register(dispatcher);
        SpawnCommand.register(dispatcher);
        LogCommand.register(dispatcher);
        DrawCommand.register(dispatcher);
        DistanceCommand.register(dispatcher);
        PlayerCommand.register(dispatcher);
        ScriptCommand.register(dispatcher);
        InfoCommand.register(dispatcher);
        PerimeterInfoCommand.register(dispatcher);
        TickCommand.register(dispatcher);

        extensions.forEach(e -> e.registerCommands(dispatcher));
        currentCommandDispatcher = dispatcher;
    }

    public static void registerExtensionLoggers() {
        extensions.forEach(CarpetExtension::registerLoggers);
    }

    public static void onPlayerLoggedIn(EntityPlayerMP player) {
        ServerNetworkHandler.onPlayerJoin(player);
        LoggerRegistry.playerConnected(player);
        extensions.forEach(e -> e.onPlayerLoggedIn(player));
    }

    public static void onPlayerLoggedOut(EntityPlayerMP player){
        ServerNetworkHandler.onPlayerLoggedOut(player);
        LoggerRegistry.playerDisconnected(player);
        extensions.forEach(e -> e.onPlayerLoggedOut(player));
    }

    public static void onServerClosed(MinecraftServer server){
        ServerNetworkHandler.close();
        currentCommandDispatcher = null;
        LoggerRegistry.stopLoggers();
        extensions.forEach(e -> e.onServerClosed(server));
        minecraft_server = null;
        disconnect();
    }

    public static void disconnect(){
        TickSpeed.reset();
        settingsManager.detachServer();
    }

}
