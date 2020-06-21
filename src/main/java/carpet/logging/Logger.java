package carpet.logging;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Logger {

    private Map<String, String> subscribedOnlinePlayers;
    private Map<String,String> subscribedOfflinePlayers;
    private String default_option;

    private String[] options;
    private String logName;
    private Field acceleratorField;

    static Logger stardardLogger(String logName, String def, String [] options)
    {
        try
        {
            return new Logger(LoggerRegistry.class.getField("__"+logName), logName, def, options);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException("Failed to create logger "+logName);
        }
    }

    public Logger(Field acceleratorField, String logName, String def, String [] options) {
        subscribedOnlinePlayers = new HashMap<>();
        subscribedOfflinePlayers = new HashMap<>();
        this.acceleratorField = acceleratorField;
        this.logName = logName;
        this.default_option = def;
        this.options = options;
        if (acceleratorField == null)
            CarpetSettings.LOG.error("[CM] Logger "+getLogName()+" is missing a specified accelerator");
    }

    public boolean hasOnlineSubscribers() {
        return subscribedOnlinePlayers.size() > 0;
    }

    public String [] getOptions() {
        if (options == null) {
            return new String[0];
        }
        return options;
    }

    public String getLogName()
    {
        return logName;
    }

    public Field getField() {
        return acceleratorField;
    }

    public void removePlayer(String playerName) {
        subscribedOnlinePlayers.remove(playerName);
        subscribedOfflinePlayers.remove(playerName);
        LoggerRegistry.setAccess(this);
    }

    public String getDefault() {
        return default_option;
    }

    public void addPlayer(String playerName, String option) {
        if (playerFromName(playerName) != null) {
            subscribedOnlinePlayers.put(playerName, option);
        }
        else {
            subscribedOfflinePlayers.put(playerName, option);
        }
        LoggerRegistry.setAccess(this);
    }

    protected EntityPlayer playerFromName(String name) {
        return CarpetServer.minecraft_server.getPlayerList().getPlayerByUsername(name);
    }

    public void serverStopped() {
        subscribedOnlinePlayers.clear();
        subscribedOfflinePlayers.clear();
    }

    public void log(Supplier<ITextComponent[]> messagePromise) {
        ITextComponent [] cannedMessages = null;
        for (Map.Entry<String,String> en : subscribedOnlinePlayers.entrySet()) {
            EntityPlayer player = playerFromName(en.getKey());
            if (player != null) {
                if (cannedMessages == null) cannedMessages = messagePromise.get();
                sendPlayerMessage(player, cannedMessages);
            }
        }
    }

    public void sendPlayerMessage(EntityPlayer player, ITextComponent ... messages) {
        Arrays.stream(messages).forEach(player::sendMessage);
    }

    @FunctionalInterface
    public interface lMessageIgnorePlayer { ITextComponent [] get(String playerOption);}
    public void log(lMessageIgnorePlayer messagePromise)
    {
        Map<String, ITextComponent[]> cannedMessages = new HashMap<>();
        for (Map.Entry<String,String> en : subscribedOnlinePlayers.entrySet())
        {
            EntityPlayer player = playerFromName(en.getKey());
            if (player != null)
            {
                String option = en.getValue();
                if (!cannedMessages.containsKey(option))
                {
                    cannedMessages.put(option,messagePromise.get(option));
                }
                ITextComponent [] messages = cannedMessages.get(option);
                if (messages != null)
                    sendPlayerMessage(player, messages);
            }
        }
    }

    @FunctionalInterface
    public interface lMessage { ITextComponent [] get(String playerOption, EntityPlayer player);}
    public void log(lMessage messagePromise)
    {
        for (Map.Entry<String,String> en : subscribedOnlinePlayers.entrySet())
        {
            EntityPlayer player = playerFromName(en.getKey());
            if (player != null)
            {
                ITextComponent [] messages = messagePromise.get(en.getValue(),player);
                if (messages != null)
                    sendPlayerMessage(player, messages);
            }
        }
    }
}
