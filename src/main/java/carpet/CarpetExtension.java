package carpet;

import carpet.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public interface CarpetExtension {
    /**
     * When game started before world is loaded
     */
    default void onGameStarted() {}

    /**
     * Runs once per loaded world once the World files / gamerules etc are fully loaded
     * Can be loaded multiple times in SinglePlayer
     */
    default void onServerLoaded(MinecraftServer server) {}

    /**
     * Runs once per game tick, as a first thing in the tick
     */
    default void onTick(MinecraftServer server) {}

    /**
     * Register your own commands right after vanilla commands are added
     * If that matters for you
     */
    default void registerCommands(CommandDispatcher<CommandSource> dispatcher) {}

    /**
     * Provide your own custom settings manager managed in the same way as base /carpet
     * command, but separated to its own command as defined in SettingsManager.
     */
    default SettingsManager customSettingsManager() {return null;}

    /**
     * todiddalidoo
     */
    default void onPlayerLoggedIn(EntityPlayerMP player) {}

    /**
     * todiddalidoo
     */
    default void onPlayerLoggedOut(EntityPlayerMP player) {}

    default void onServerClosed(MinecraftServer server) {}

    default String version() {return null;}

    default void registerLoggers() {}
}
