package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.TickSpeed;
import carpet.settings.SettingsManager;
import carpet.utils.CarpetProfiler;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.ISuggestionProvider.suggest;

public class TickCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher){
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = literal("tick").
                requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandTick)).
                then(literal("rate").
                        executes((c) -> queryTps(c.getSource())).
                        then(argument("rate", FloatArgumentType.floatArg(0.1F, 500.0F)).
                                suggests((c, b) -> suggest(new String[]{"20"}, b)).
                                executes((c) -> setTps(c.getSource(), FloatArgumentType.getFloat(c, "rate"))))).
                then(literal("warp").
                        executes((c) -> setWarp(c.getSource(), 0, null)).
                        then(argument("ticks", IntegerArgumentType.integer(0,4000000)).
                                suggests((c, b) -> suggest(new String[]{"3600","72000"}, b)).
                                executes((c) -> setWarp(c.getSource(), IntegerArgumentType.getInteger(c, "ticks"), null)).
                                then(argument("tail command", StringArgumentType.greedyString()).
                                        executes((c) -> setWarp(c.getSource(), IntegerArgumentType.getInteger(c, "ticks"), StringArgumentType.getString(c, "tail command")))))).
                then(literal("freeze").
                        executes((c) -> toggleFreeze(c.getSource(), false)).
                        then(literal("deep").
                                executes((c) -> toggleFreeze(c.getSource(), true)))).
                then(literal("step").
                        executes((c) -> step(1)).
                        then(argument("ticks", IntegerArgumentType.integer(1,72000)).
                                suggests((c, b) -> suggest(new String[]{"20"}, b)).
                                executes((c) -> step(IntegerArgumentType.getInteger(c, "ticks"))))).
                then(literal("superHot").
                        executes( (c)-> toggleSuperHot(c.getSource()))).
                then(literal("health").
                        executes( (c) -> healthReport(c.getSource(), 100)).
                        then(argument("ticks", integer(20,24000)).
                                executes( (c) -> healthReport(c.getSource(), getInteger(c, "ticks"))))).
                then(literal("entities").
                        executes((c) -> healthEntities(c.getSource(), 100)).
                        then(argument("ticks", integer(20,24000)).
                                executes((c) -> healthEntities(c.getSource(), getInteger(c, "ticks")))));
        dispatcher.register(literalArgumentBuilder);
    }

    private static int queryTps(CommandSource source)
    {
        Messenger.m(source, "w Current tps is: ",String.format("wb %.1f", TickSpeed.tickrate));
        return (int)TickSpeed.tickrate;
    }

    private static int setTps(CommandSource source, float tps)
    {
        TickSpeed.tickrate(tps);
        queryTps(source);
        return (int)tps;
    }

    private static int setWarp(CommandSource source, int advance, String tail_command)
    {
        EntityPlayerMP player = null;
        try
        {
            player = source.asPlayer();
        }
        catch (CommandSyntaxException ignored)
        {
        }
        ITextComponent message = TickSpeed.tickrate_advance(player, advance, tail_command, source);
        if (message != null)
        {
            source.sendFeedback(message, false);
        }
        return 1;
    }

    private static int toggleFreeze(CommandSource source, boolean isDeep)
    {
        TickSpeed.is_paused = !TickSpeed.is_paused;
        if (TickSpeed.is_paused)
        {
            TickSpeed.deepFreeze = isDeep;
            Messenger.m(source, "gi Game is "+(isDeep?"deeply ":"")+"frozen");

        }
        else
        {
            TickSpeed.deepFreeze = false;
            Messenger.m(source, "gi Game runs normally");
        }
        return 1;
    }

    private static int step(int advance)
    {
        TickSpeed.add_ticks_to_run_in_pause(advance);
        return 1;
    }

    private static int toggleSuperHot(CommandSource source)
    {
        TickSpeed.is_superHot = !TickSpeed.is_superHot;
        if (TickSpeed.is_superHot)
        {
            Messenger.m(source,"gi Superhot enabled");
        }
        else
        {
            Messenger.m(source, "gi Superhot disabled");
        }
        return 1;
    }

    public static int healthReport(CommandSource source, int ticks)
    {
        CarpetProfiler.prepare_tick_report(ticks);
        return 1;
    }

    public static int healthEntities(CommandSource source, int ticks)
    {
        CarpetProfiler.prepare_entity_report(ticks);
        return 1;
    }
}
