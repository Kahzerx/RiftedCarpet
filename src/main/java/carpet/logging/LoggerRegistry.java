package carpet.logging;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import net.minecraft.item.EnumDyeColor;

import java.lang.reflect.Field;
import java.util.*;

public class LoggerRegistry {
    private static Map<String, Logger> loggerRegistry = new HashMap<>();
    private static Map<String, Map<String, String>> playerSubscriptions = new HashMap<>();
    public static boolean __tnt;
    public static boolean __projectiles;
    public static boolean __fallingBlocks;
    public static boolean __kills;
    public static boolean __tps;
    public static boolean __counter;
    public static boolean __mobcaps;
    public static boolean __damage;
    public static boolean __packets;
    public static boolean __weather;
    public static boolean __pathfinding;
    public static boolean __explosions;

    public static void initLoggers()
    {
        stopLoggers();
        registerLoggers();
        CarpetServer.registerExtensionLoggers();
    }

    public static void registerLoggers() {
        registerLogger("tnt", Logger.stardardLogger( "tnt", "brief", new String[]{"brief", "full"}));
        //registerLogger("projectiles", Logger.stardardLogger("projectiles", "brief",  new String[]{"brief", "full"}));
        //registerLogger("fallingBlocks",Logger.stardardLogger("fallingBlocks", "brief", new String[]{"brief", "full"}));
        //registerLogger("kills", new Logger("kills", null, null));
        //registerLogger("damage", new Logger("damage", "all", new String[]{"all","players","me"}));
        //registerLogger("weather", new Logger("weather", null, null));
        registerLogger( "pathfinding", Logger.stardardLogger("pathfinding", "20", new String[]{"2", "5", "10"}));

        registerLogger("tps", HUDLogger.stardardHUDLogger("tps", null, null));
        registerLogger("packets", HUDLogger.stardardHUDLogger("packets", null, null));
        registerLogger("counter",HUDLogger.stardardHUDLogger("counter","white", Arrays.stream(EnumDyeColor.values()).map(Object::toString).toArray(String[]::new)));
        registerLogger("mobcaps", HUDLogger.stardardHUDLogger("mobcaps", "dynamic",new String[]{"dynamic", "overworld", "nether","end"}));
        //registerLogger("explosions", HUDLogger.stardardLogger("explosions", "brief",new String[]{"brief", "full"}));

    }

    public static Map<String,String> getPlayerSubscriptions(String playerName){
        if (playerSubscriptions.containsKey(playerName)){
            return playerSubscriptions.get(playerName);
        }
        return null;
    }

    public static Set<String> getLoggerNames() { return loggerRegistry.keySet(); }

    public static Logger getLogger(String name) { return loggerRegistry.get(name); }

    public static void registerLogger(String name, Logger logger) {
        loggerRegistry.put(name, logger);
        setAccess(logger);
    }

    protected static void setAccess(Logger logger) {
        String name = logger.getLogName();
        boolean value = logger.hasOnlineSubscribers();
        try {
            Field f = logger.getField();
            f.setBoolean(null, value);
        }
        catch (IllegalAccessException e) {
            CarpetSettings.LOG.error("Cannot change logger quick access field");
        }
    }

    public static void unsubscribePlayer(String playerName, String logName) {
        if (playerSubscriptions.containsKey(playerName)) {
            Map<String,String> subscriptions = playerSubscriptions.get(playerName);
            subscriptions.remove(logName);
            loggerRegistry.get(logName).removePlayer(playerName);
            if (subscriptions.size() == 0) playerSubscriptions.remove(playerName);
        }
    }

    public static boolean togglePlayerSubscription(String playerName, String logName) {
        if (playerSubscriptions.containsKey(playerName) && playerSubscriptions.get(playerName).containsKey(logName)) {
            unsubscribePlayer(playerName, logName);
            return false;
        }
        else {
            subscribePlayer(playerName, logName, null);
            return true;
        }
    }

    public static void subscribePlayer(String playerName, String logName, String option) {
        if (!playerSubscriptions.containsKey(playerName)) playerSubscriptions.put(playerName, new HashMap<>());
        Logger log = loggerRegistry.get(logName);
        if (option == null) option = log.getDefault();
        playerSubscriptions.get(playerName).put(logName,option);
        log.addPlayer(playerName, option);
    }

    public static void stopLoggers() {
        for(Logger log: loggerRegistry.values() ) {
            log.serverStopped();
        }
        seenPlayers.clear();
        loggerRegistry.clear();
        playerSubscriptions.clear();
    }

    private static Set<String> seenPlayers = new HashSet<>();
}
