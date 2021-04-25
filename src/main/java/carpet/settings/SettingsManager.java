package carpet.settings;

import carpet.CarpetSettings;
import carpet.network.ServerNetworkHandler;
import carpet.utils.Messenger;
import carpet.utils.Translations;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static carpet.utils.Translations.tr;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

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
        ServerNetworkHandler.updateRuleWithConnectedClients(rule);
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

    public void detachServer() {
        for (ParsedRule<?> rule : rules.values()) rule.resetToDefault(null);
        server = null;
    }

    public void attachServer(MinecraftServer server){
        this.server = server;
        loadConfigurationFromConf();
        registerCommand(server.getCommandManager().getDispatcher());
        notifyPlayersCommandsChanged();
    }

    private void loadConfigurationFromConf(){
        for (ParsedRule<?> rule : rules.values()) rule.resetToDefault(server.getCommandSource());
        Pair<Map<String, String>,Boolean> conf = readSettingsFromConf();
        locked = false;
        if (conf.getRight()){
            CarpetSettings.LOG.info("[CM]: "+fancyName+" features are locked by the administrator");
            disableBooleanCommands();
        }
        for (String key: conf.getLeft().keySet()){
            try{
                if (rules.get(key).set(server.getCommandSource(), conf.getLeft().get(key)) != null) CarpetSettings.LOG.info("[CM]: loaded setting " + key + " as " + conf.getLeft().get(key) + " from " + identifier + ".conf");
            }
            catch (Exception exc){
                CarpetSettings.LOG.error("[CM Error]: Failed to load setting: "+key+", "+exc);
            }
        }
        locked = conf.getRight();
    }

    private Pair<Map<String, String>,Boolean> readSettingsFromConf(){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(getFile()));
            String line = "";
            boolean confLocked = false;
            Map<String,String> result = new HashMap<String, String>();
            while ((line = reader.readLine()) != null){
                line = line.replaceAll("[\\r\\n]", "");
                if ("locked".equalsIgnoreCase(line)) confLocked = true;
                String[] fields = line.split("\\s+",2);
                if (fields.length > 1){
                    if (!rules.containsKey(fields[0])){
                        CarpetSettings.LOG.error("[CM]: "+fancyName+" Setting " + fields[0] + " is not a valid - ignoring...");
                        continue;
                    }
                    ParsedRule<?> rule = rules.get(fields[0]);
                    if (!(rule.options.contains(fields[1])) && rule.isStrict){
                        CarpetSettings.LOG.error("[CM]: The value of " + fields[1] + " for " + fields[0] + "("+fancyName+") is not valid - ignoring...");
                        continue;
                    }
                    result.put(fields[0],fields[1]);
                }
            }
            reader.close();
            return Pair.of(result, confLocked);
        }
        catch(FileNotFoundException e){
            return Pair.of(new HashMap<>(), false);
        }
        catch (IOException e){
            e.printStackTrace();
            return Pair.of(new HashMap<>(), false);
        }
    }

    private File getFile()
    {
        return server.getActiveAnvilConverter().getFile(server.getFolderName(), identifier+".conf");
    }

    public void disableBooleanCommands(){
        for (ParsedRule<?> rule : rules.values()){
            if (!rule.categories.contains(RuleCategory.COMMAND))
                continue;
            if (rule.type == boolean.class) ((ParsedRule<Boolean>) rule).set(server.getCommandSource(), false, "false");
            if (rule.type == String.class && rule.options.contains("false"))((ParsedRule<String>) rule).set(server.getCommandSource(), "false", "false");
        }
    }

    public void notifyPlayersCommandsChanged(){
        if (server.getPlayerList() == null) return;
        for (EntityPlayerMP entityPlayerMP : server.getPlayerList().getPlayers()){
            server.getCommandManager().send(entityPlayerMP);
        }
    }

    public void registerCommand(CommandDispatcher<CommandSource> dispatcher){
        if (dispatcher.getRoot().getChildren().stream().anyMatch(node -> node.getName().equalsIgnoreCase(identifier))){
            CarpetSettings.LOG.error("Failed to add settings command for " + identifier + ". It is masking previous command.");
            return;
        }
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = literal(identifier).requires((player) -> player.hasPermissionLevel(2) && !locked);

        literalArgumentBuilder.executes((context) -> listAllSettings(context.getSource())).
                then(literal("list").
                        executes((c) -> listSettings(c.getSource(), String.format(tr("ui.all_%(mod)s_settings","All %s Settings"), fancyName), getRules())).
                        then(literal("defaults").
                                executes((c) -> listSettings(c.getSource(), String.format(tr("ui.current_%(mod)s_startup_settings_from_%(conf)s","Current %s Startup Settings from %s"), fancyName, (identifier+".conf")), findStartupOverrides()))).
                        then(argument("tag", StringArgumentType.word()).
                                suggests((c, b)-> ISuggestionProvider.suggest(getCategories(), b)).
                                executes( (c) -> listSettings(c.getSource(),String.format(tr("ui.%(mod)s_settings_matching_'%(query)s'","%s Settings matching \"%s\""), fancyName, tr("category." + StringArgumentType.getString(c, "tag"),StringArgumentType.getString(c, "tag"))),getRulesMatching(StringArgumentType.getString(c, "tag")))))).
                then(literal("removeDefault").
                        requires(s -> !locked).
                        then(argument("rule", StringArgumentType.word()).
                                suggests((c, b)-> ISuggestionProvider.suggest(getRules().stream().map(r -> r.name), b)).
                                executes((c) -> removeDefault(c.getSource(), contextRule(c))))).
                then(literal("setDefault").
                        requires(s -> !locked).
                        then(argument("rule", StringArgumentType.word()).
                                suggests((c,b) -> ISuggestionProvider.suggest(getRules().stream().map(r -> r.name), b)).
                                then(argument("value", StringArgumentType.greedyString()).
                                        suggests((c, b) -> ISuggestionProvider.suggest(contextRule(c).options, b)).
                                        executes((c) -> setDefault(c.getSource(), contextRule(c), StringArgumentType.getString(c, "value")))))).
                then(argument("rule", StringArgumentType.word()).
                        suggests((c, b) -> ISuggestionProvider.suggest(getRules().stream().map(r -> r.name), b)).
                        requires(s -> !locked).
                        executes((c) -> displayRuleMenu(c.getSource(), contextRule(c))).
                        then(argument("value", StringArgumentType.greedyString()).
                                suggests((c, b) -> ISuggestionProvider.suggest(contextRule(c).options, b)).
                                executes((c) -> setRule(c.getSource(), contextRule(c), StringArgumentType.getString(c, "value")))));

        dispatcher.register(literalArgumentBuilder);
    }

    private int listAllSettings(CommandSource source){
        listSettings(source, String.format(tr("ui.current_%(mod)s_settings","Current %s Settings"), fancyName), getNonDefault());
        if (version != null) Messenger.m(source, "g "+fancyName+" "+ tr("ui.version",  "version") + ": "+ version);
        try{
            EntityPlayer player = source.asPlayer();
            List<Object> tags = new ArrayList<>();
            tags.add("w " + tr("ui.browse_categories", "Browse Categories")  + ":\n");
            for (String t : getCategories()){
                String catKey = "category." + t;
                String translated = tr(catKey, t);
                String translatedPlus = Translations.hasTranslation(catKey) ? String.format("%s (%s)",tr(catKey, t), t) : t;
                tags.add("c [" + translated +"]");
                tags.add("^g " + String.format(tr("ui.list_all_%(cat)s_settings","list all %s settings"), translatedPlus));
                tags.add("!/"+identifier+" list " + t);
                tags.add("w  ");
            }
            tags.remove(tags.size() - 1);
            Messenger.m(player, tags.toArray(new Object[0]));
        }
        catch (CommandSyntaxException ignored){
        }
        return 1;
    }

    private int listSettings(CommandSource source, String title, Collection<ParsedRule<?>> settings_list){
        try{
            EntityPlayer player = source.asPlayer();
            Messenger.m(player, String.format("wb %s:", title));
            settings_list.forEach(e -> Messenger.m(player, displayInteractiveSetting(e)));
        }
        catch (CommandSyntaxException e){
            Messenger.m(source, "w s:"+title);
            settings_list.forEach(r -> Messenger.m(source, "w  - "+ r.toString()));
        }
        return 1;
    }

    private ITextComponent displayInteractiveSetting(ParsedRule<?> rule){
        String displayName = rule.translatedName();
        List<Object> args = new ArrayList<>();
        args.add("w - "+ displayName +" ");
        args.add("!/"+identifier+" "+rule.name);
        args.add("^y "+rule.translatedDescription());
        for (String option: rule.options){
            args.add(makeSetRuleButton(rule, option, true));
            args.add("w  ");
        }
        args.remove(args.size()-1);
        return Messenger.c(args.toArray(new Object[0]));
    }

    private ITextComponent makeSetRuleButton(ParsedRule<?> rule, String option, boolean brackets){
        String style = rule.isDefault()?"g":(option.equalsIgnoreCase(rule.defaultAsString)?"e":"y");
        if (option.equalsIgnoreCase(rule.getAsString())){
            style = style + "u";
            if (option.equalsIgnoreCase(rule.defaultAsString)) style = style + "b";
        }
        String baseText = style + (brackets ? " [" : " ") + option + (brackets ? "]" : "");
        if (locked) return Messenger.c(baseText, "^g "+fancyName+" " + tr("ui.settings_are_locked","settings are locked"));
        if (option.equalsIgnoreCase(rule.getAsString())) return Messenger.c(baseText);
        return Messenger.c(baseText, "^g "+ tr("ui.switch_to","Switch to") +" " + option+(option.equalsIgnoreCase(rule.defaultAsString)?" (default)":""), "?/"+identifier+" " + rule.name + " " + option);
    }

    public Collection<ParsedRule<?>> getNonDefault()
    {
        return rules.values().stream().filter(r -> !r.isDefault()).sorted().collect(Collectors.toList());
    }

    public Iterable<String> getCategories()
    {
        Set<String> categories = new HashSet<>();
        getRules().stream().map(r -> r.categories).forEach(categories::addAll);
        return categories;
    }

    public Collection<ParsedRule<?>> getRules()
    {
        return rules.values().stream().sorted().collect(Collectors.toList());
    }

    public Collection<ParsedRule<?>> findStartupOverrides(){
        Set<String> defaults = readSettingsFromConf().getLeft().keySet();
        return rules.values().stream().filter(r -> defaults.contains(r.name)).sorted().collect(Collectors.toList());
    }

    private Collection<ParsedRule<?>> getRulesMatching(String search) {
        String lcSearch = search.toLowerCase(Locale.ROOT);
        return rules.values().stream().filter(rule -> {
            if (rule.name.toLowerCase(Locale.ROOT).contains(lcSearch)) return true; // substring match, case insensitive
            for (String c : rule.categories) if (c.equals(search)) return true; // category exactly, case sensitive
            return Sets.newHashSet(rule.description.toLowerCase(Locale.ROOT).split("\\W+")).contains(lcSearch); // contains full term in description, but case insensitive
        }).sorted().collect(ImmutableList.toImmutableList());
    }

    private int removeDefault(CommandSource source, ParsedRule<?> rule)
    {
        if (locked) return 0;
        if (!rules.containsKey(rule.name)) return 0;
        Pair<Map<String, String>,Boolean> conf = readSettingsFromConf();
        conf.getLeft().remove(rule.name);
        writeSettingsToConf(conf.getLeft());
        rules.get(rule.name).resetToDefault(source);
        return 1;
    }

    private void writeSettingsToConf(Map<String, String> values)
    {
        if (locked) return;
        try{
            FileWriter fw  = new FileWriter(getFile());
            for (String key: values.keySet())
            {
                fw.write(key+" "+values.get(key)+"\n");
            }
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
            CarpetSettings.LOG.error("[CM]: failed write "+identifier+".conf config file");
        }
        ///todo is it really needed? resendCommandTree();
    }

    private ParsedRule<?> contextRule(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        String ruleName = StringArgumentType.getString(ctx, "rule");
        ParsedRule<?> rule = getRule(ruleName);
        if (rule == null) throw new SimpleCommandExceptionType(Messenger.c("rb "+ tr("ui.unknown_rule","Unknown rule")+": "+ruleName)).create();
        return rule;
    }

    public ParsedRule<?> getRule(String name){
        return rules.get(name);
    }

    private int setDefault(CommandSource source, ParsedRule<?> rule, String stringValue){
        if (locked) return 0;
        if (!rules.containsKey(rule.name)) return 0;
        Pair<Map<String, String>,Boolean> conf = readSettingsFromConf();
        conf.getLeft().put(rule.name, stringValue);
        writeSettingsToConf(conf.getLeft()); // this may feels weird, but if conf
        // is locked, it will never reach this point.
        rule.set(source,stringValue);
        Messenger.m(source ,"gi "+String.format(tr("ui.rule_%(rule)s_will_now_default_to_%(value)s","rule %s will now default to %s"), rule.translatedName(), stringValue));
        return 1;
    }

    private int displayRuleMenu(CommandSource source, ParsedRule<?> rule)
    {
        EntityPlayer player;
        String displayName = rule.translatedName();
        try
        {
            player = source.asPlayer();
        }
        catch (CommandSyntaxException e)
        {
            Messenger.m(source, "w "+ displayName +" "+ tr( "ui.is_set_to","is set to")+": ","wb "+rule.getAsString());
            return 1;
        }

        Messenger.m(player, "");
        Messenger.m(player, "wb "+ displayName ,"!/"+identifier+" "+rule.name,"^g refresh");
        Messenger.m(player, "w "+ rule.translatedDescription());

        rule.translatedExtras().forEach(s -> Messenger.m(player, "g  "+s));

        List<ITextComponent> tags = new ArrayList<>();
        tags.add(Messenger.c("w "+ tr("ui.tags", "Tags")+": "));
        for (String t: rule.categories)
        {
            String translated = tr("category." + t, t);
            tags.add(Messenger.c("c ["+ translated +"]", "^g "+ String.format(tr("list_all_%s_settings","list all %s settings"), translated),"!/"+identifier+" list "+t));
            tags.add(Messenger.c("w , "));
        }
        tags.remove(tags.size()-1);
        Messenger.m(player, tags.toArray(new Object[0]));

        Messenger.m(player, "w "+ tr("ui.current_value", "Current value")+": ",String.format("%s %s (%s value)",rule.getBoolValue()?"lb":"nb", rule.getAsString(),rule.isDefault()?"default":"modified"));
        List<ITextComponent> options = new ArrayList<>();
        options.add(Messenger.c("w Options: ", "y [ "));
        for (String o: rule.options)
        {
            options.add(makeSetRuleButton(rule, o, false));
            options.add(Messenger.c("w  "));
        }
        options.remove(options.size()-1);
        options.add(Messenger.c("y  ]"));
        Messenger.m(player, options.toArray(new Object[0]));

        return 1;
    }

    private int setRule(CommandSource source, ParsedRule<?> rule, String newValue){
        if (rule.set(source, newValue) != null)
            Messenger.m(source, "w "+rule.toString()+", ", "c ["+ tr("ui.change_permanently","change permanently")+"?]",
                    "^w "+String.format(tr("ui.click_to_keep_the_settings_in_%(conf)s_to_save_across_restarts","Click to keep the settings in %s to save across restarts"), identifier+".conf"),
                    "?/"+identifier+" setDefault "+rule.name+" "+rule.getAsString());
        return 1;
    }

    public static boolean canUseCommand(CommandSource source, String commandLevel) {
        switch (commandLevel)
        {
            case "true": return true;
            case "false": return false;
            case "ops": return source.hasPermissionLevel(2); // typical for other cheaty commands
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
                return source.hasPermissionLevel(Integer.parseInt(commandLevel));
        }
        return false;
    }

    public void inspectClientsideCommand(CommandSource source, String string)
    {
        if (string.startsWith("/"+identifier+" "))
        {
            String[] res = string.split("\\s+", 3);
            if (res.length == 3)
            {
                String setting = res[1];
                String strOption = res[2];
                if (rules.containsKey(setting) && rules.get(setting).isClient)
                {
                    rules.get(setting).set(source, strOption);
                }
            }
        }
    }
}