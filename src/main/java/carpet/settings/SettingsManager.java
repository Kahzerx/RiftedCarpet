package carpet.settings;

import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.TriConsumer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsManager
{
    private Map<String, ParsedRule<?>> rules = new HashMap<>();
    public boolean locked;
    private final String version;
    private final String identifier;
    private final String fancyName;
    private MinecraftServer server;
    private List<TriConsumer<CommandSource, ParsedRule<?>, String>> observers = new ArrayList<>();

    public SettingsManager(String version, String identifier)
    {
        this.version = version;
        this.identifier = identifier;
        this.fancyName = identifier;
    }

    public SettingsManager(String version, String identifier, String fancyName)
    {
        this.version = version;
        this.identifier = identifier;
        this.fancyName = fancyName;
    }

    void notifyRuleChanged(CommandSource source, ParsedRule<?> rule, String userTypedValue)
    {
        observers.forEach(observer -> observer.accept(source, rule, userTypedValue));
    }

    public void parseSettingsClass(Class settingsClass)
    {
        for (Field f : settingsClass.getDeclaredFields())
        {
            Rule rule = f.getAnnotation(Rule.class);
            if (rule == null) continue;
            ParsedRule parsed = new ParsedRule(f, rule);
            rules.put(parsed.name, parsed);
        }
    }

}