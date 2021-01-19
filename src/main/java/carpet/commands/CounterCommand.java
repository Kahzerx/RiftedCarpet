package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.text.ITextComponent;

import static net.minecraft.command.Commands.literal;

public class CounterCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher){
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = literal("counter").
                executes((context) -> listAllCounters(context.getSource(), false)).
                requires((player) -> CarpetSettings.hopperCounters);

        literalArgumentBuilder.
                then((literal("reset").
                        executes((context) -> resetCounter(context.getSource(), null))));
        for (EnumDyeColor enumDyeColor : EnumDyeColor.values()){
            String color = enumDyeColor.toString();
            literalArgumentBuilder.
                    then((literal(color).
                            executes((context) -> displayCounter(context.getSource(), color, false))));
            literalArgumentBuilder.
                    then(literal(color).
                            then(literal("reset").
                                    executes((context) -> resetCounter(context.getSource(), color))));
            literalArgumentBuilder.
                    then(literal(color).
                            then(literal("realtime").
                                    executes((context) -> displayCounter(context.getSource(), color, true))));
        }
        dispatcher.register(literalArgumentBuilder);
    }

    private static int listAllCounters(CommandSource source, boolean realtime)
    {
        for (ITextComponent message: HopperCounter.formatAll(source.getServer(), realtime))
        {
            source.sendFeedback(message, false);
        }
        return 1;
    }

    private static int resetCounter(CommandSource source, String color)
    {
        if (color == null)
        {
            HopperCounter.resetAll(source.getServer());
            Messenger.m(source, "w Restarted all counters");
        }
        else
        {
            HopperCounter counter = HopperCounter.getCounter(color);
            if (counter == null) throw new CommandException(Messenger.s("Unknown wool color"));
            counter.reset(source.getServer());
            Messenger.m(source, "w Restarted "+color+" counter");
        }
        return 1;
    }

    private static int displayCounter(CommandSource source, String color, boolean realtime)
    {
        HopperCounter counter = HopperCounter.getCounter(color);
        if (counter == null) throw new CommandException(Messenger.s("Unknown wool color: "+color));

        for (ITextComponent message: counter.format(source.getServer(), realtime, false))
        {
            source.sendFeedback(message, false);
        }
        return 1;
    }
}
