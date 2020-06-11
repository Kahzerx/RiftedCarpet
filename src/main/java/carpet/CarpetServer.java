package carpet;

import carpet.settings.SettingsManager;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class CarpetServer {
    public static SettingsManager settingsManager;
    public static final List<CarpetExtension> extensions = new ArrayList<>();
    public static MinecraftServer minecraft_server;

    public static void onGameStarted(){
        settingsManager = new SettingsManager(CarpetSettings.carpetVersion, "carpet", "TISCarpet");
        settingsManager.parseSettingsClass(CarpetSettings.class);
        extensions.forEach(CarpetExtension::onGameStarted);
    }

    public static void onServerLoaded(MinecraftServer server){
        System.out.println("o/");
        CarpetServer.minecraft_server = server;
        settingsManager.attachServer(server);
        extensions.forEach(e -> {
            SettingsManager sm = e.customSettingsManager();
            if (sm != null) sm.attachServer(server);
            e.onServerClosed(server);
        });
    }
}
