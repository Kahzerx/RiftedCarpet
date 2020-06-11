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
    }

    public static void tick(MinecraftServer server){

    }

    public static void onServerClosed(MinecraftServer server){

    }
}
