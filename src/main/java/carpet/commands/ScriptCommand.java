package carpet.commands;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.script.*;
import carpet.script.exception.CarpetExpressionException;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.BlockPredicateArgument;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.ISuggestionProvider.suggest;

public class ScriptCommand
{

    private static CompletableFuture<Suggestions> suggestCode(
            CommandContext<CommandSource> context,
            SuggestionsBuilder suggestionsBuilder
    )
    {
        ScriptHost currentHost = getHost(context);
        String previous = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        int strlen = previous.length();
        StringBuilder lastToken = new StringBuilder();
        for (int idx = strlen-1; idx >=0; idx--)
        {
            char ch = previous.charAt(idx);
            if (Character.isLetterOrDigit(ch) || ch == '_') lastToken.append(ch); else break;
        }
        if (lastToken.length() == 0)
            return suggestionsBuilder.buildFuture();
        String prefix = lastToken.reverse().toString();
        String previousString =  previous.substring(0,previous.length()-prefix.length()) ;
        ExpressionInspector.suggestFunctions(currentHost, previousString, prefix).forEach(text -> suggestionsBuilder.suggest(previousString+text));
        return suggestionsBuilder.buildFuture();
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralArgumentBuilder<CommandSource> b = literal("globals").
                executes(ScriptCommand::listGlobals);
        LiteralArgumentBuilder<CommandSource> o = literal("stop").
                executes( (cc) -> { CarpetServer.scriptServer.stopAll = true; return 1;});
        LiteralArgumentBuilder<CommandSource> u = literal("resume").
                executes( (cc) -> { CarpetServer.scriptServer.stopAll = false; return 1;});
        LiteralArgumentBuilder<CommandSource> l = literal("run").
                requires((player) -> player.hasPermissionLevel(2)).
                then(argument("expr", StringArgumentType.greedyString()).suggests((cc, bb) -> suggestCode(cc, bb)).
                        executes((cc) -> compute(
                                cc,
                                StringArgumentType.getString(cc, "expr"))));
        LiteralArgumentBuilder<CommandSource> s = literal("invoke").
                then(argument("call", StringArgumentType.word()).suggests( (cc, bb)->suggest(suggestFunctionCalls(cc),bb)).
                        executes( (cc) -> invoke(
                                cc,
                                StringArgumentType.getString(cc, "call"),
                                null,
                                null,
                                ""
                        )).
                        then(argument("arguments", StringArgumentType.greedyString()).
                                executes( (cc) -> invoke(
                                        cc,
                                        StringArgumentType.getString(cc, "call"),
                                        null,
                                        null,
                                        StringArgumentType.getString(cc, "arguments")
                                ))));
        LiteralArgumentBuilder<CommandSource> c = literal("invokepoint").
                then(argument("call", StringArgumentType.word()).suggests( (cc, bb)->suggest(suggestFunctionCalls(cc),bb)).
                        then(argument("origin", BlockPosArgument.blockPos()).
                                executes( (cc) -> invoke(
                                        cc,
                                        StringArgumentType.getString(cc, "call"),
                                        BlockPosArgument.getBlockPos(cc, "origin"),
                                        null,
                                        ""
                                )).
                                then(argument("arguments", StringArgumentType.greedyString()).
                                        executes( (cc) -> invoke(
                                                cc,
                                                StringArgumentType.getString(cc, "call"),
                                                BlockPosArgument.getBlockPos(cc, "origin"),
                                                null,
                                                StringArgumentType.getString(cc, "arguments")
                                        )))));
        LiteralArgumentBuilder<CommandSource> h = literal("invokearea").
                then(argument("call", StringArgumentType.word()).suggests( (cc, bb)->suggest(suggestFunctionCalls(cc),bb)).
                        then(argument("from", BlockPosArgument.blockPos()).
                                then(argument("to", BlockPosArgument.blockPos()).
                                        executes( (cc) -> invoke(
                                                cc,
                                                StringArgumentType.getString(cc, "call"),
                                                BlockPosArgument.getBlockPos(cc, "from"),
                                                BlockPosArgument.getBlockPos(cc, "to"),
                                                ""
                                        )).
                                        then(argument("arguments", StringArgumentType.greedyString()).
                                                executes( (cc) -> invoke(
                                                        cc,
                                                        StringArgumentType.getString(cc, "call"),
                                                        BlockPosArgument.getBlockPos(cc, "from"),
                                                        BlockPosArgument.getBlockPos(cc, "to"),
                                                        StringArgumentType.getString(cc, "arguments")
                                                ))))));
        LiteralArgumentBuilder<CommandSource> i = literal("scan").requires((player) -> player.hasPermissionLevel(2)).
                then(argument("origin", BlockPosArgument.blockPos()).
                        then(argument("from", BlockPosArgument.blockPos()).
                                then(argument("to", BlockPosArgument.blockPos()).
                                        then(argument("expr", StringArgumentType.greedyString()).
                                                suggests((cc, bb) -> suggestCode(cc, bb)).
                                                executes( (cc) -> scriptScan(
                                                        cc,
                                                        BlockPosArgument.getBlockPos(cc, "origin"),
                                                        BlockPosArgument.getBlockPos(cc, "from"),
                                                        BlockPosArgument.getBlockPos(cc, "to"),
                                                        StringArgumentType.getString(cc, "expr")
                                                ))))));
        LiteralArgumentBuilder<CommandSource> e = literal("fill").requires((player) -> player.hasPermissionLevel(2)).
                then(argument("origin", BlockPosArgument.blockPos()).
                        then(argument("from", BlockPosArgument.blockPos()).
                                then(argument("to", BlockPosArgument.blockPos()).
                                        then(argument("expr", StringArgumentType.string()).
                                                then(argument("block", BlockStateArgument.blockState()).
                                                        executes((cc) -> scriptFill(
                                                                cc,
                                                                BlockPosArgument.getBlockPos(cc, "origin"),
                                                                BlockPosArgument.getBlockPos(cc, "from"),
                                                                BlockPosArgument.getBlockPos(cc, "to"),
                                                                StringArgumentType.getString(cc, "expr"),
                                                                BlockStateArgument.getBlockState(cc, "block"),
                                                                null, "solid"
                                                        )).
                                                        then(literal("replace").
                                                                then(argument("filter", BlockPredicateArgument.blockPredicate())
                                                                        .executes((cc) -> scriptFill(
                                                                                cc,
                                                                                BlockPosArgument.getBlockPos(cc, "origin"),
                                                                                BlockPosArgument.getBlockPos(cc, "from"),
                                                                                BlockPosArgument.getBlockPos(cc, "to"),
                                                                                StringArgumentType.getString(cc, "expr"),
                                                                                BlockStateArgument.getBlockState(cc, "block"),
                                                                                BlockPredicateArgument.getBlockPredicate(cc, "filter"),
                                                                                "solid"
                                                                        )))))))));
        LiteralArgumentBuilder<CommandSource> t = literal("outline").requires((player) -> player.hasPermissionLevel(2)).
                then(argument("origin", BlockPosArgument.blockPos()).
                        then(argument("from", BlockPosArgument.blockPos()).
                                then(argument("to", BlockPosArgument.blockPos()).
                                        then(argument("expr", StringArgumentType.string()).
                                                then(argument("block", BlockStateArgument.blockState()).
                                                        executes((cc) -> scriptFill(
                                                                cc,
                                                                BlockPosArgument.getBlockPos(cc, "origin"),
                                                                BlockPosArgument.getBlockPos(cc, "from"),
                                                                BlockPosArgument.getBlockPos(cc, "to"),
                                                                StringArgumentType.getString(cc, "expr"),
                                                                BlockStateArgument.getBlockState(cc, "block"),
                                                                null, "outline"
                                                        )).
                                                        then(literal("replace").
                                                                then(argument("filter", BlockPredicateArgument.blockPredicate())
                                                                        .executes((cc) -> scriptFill(
                                                                                cc,
                                                                                BlockPosArgument.getBlockPos(cc, "origin"),
                                                                                BlockPosArgument.getBlockPos(cc, "from"),
                                                                                BlockPosArgument.getBlockPos(cc, "to"),
                                                                                StringArgumentType.getString(cc, "expr"),
                                                                                BlockStateArgument.getBlockState(cc, "block"),
                                                                                BlockPredicateArgument.getBlockPredicate(cc, "filter"),
                                                                                "outline"
                                                                        )))))))));
        LiteralArgumentBuilder<CommandSource> a = literal("load").requires( (player) -> player.hasPermissionLevel(2) ).
                then(argument("package", StringArgumentType.word()).
                        suggests( (cc, bb) -> suggest(CarpetServer.scriptServer.listAvailableModules(),bb)).
                        executes((cc) ->
                        {
                            boolean success = CarpetServer.scriptServer.addScriptHost(cc.getSource(), StringArgumentType.getString(cc, "package"));
                            return success?1:0;
                        })
                );
        LiteralArgumentBuilder<CommandSource> f = literal("unload").requires( (player) -> player.hasPermissionLevel(2) ).
                then(argument("package", StringArgumentType.word()).
                        suggests( (cc, bb) -> suggest(CarpetServer.scriptServer.modules.keySet(),bb)).
                        executes((cc) ->
                        {
                            boolean success =CarpetServer.scriptServer.removeScriptHost(cc.getSource(), StringArgumentType.getString(cc, "package"));
                            return success?1:0;
                        }));

        LiteralArgumentBuilder<CommandSource> q = literal("event").requires( (player) -> player.hasPermissionLevel(2) ).
                executes( (cc) -> listEvents(cc.getSource())).
                then(literal("add_to").
                        then(argument("event", StringArgumentType.word()).
                                suggests( (cc, bb) -> suggest(CarpetServer.scriptServer.events.eventHandlers.keySet() ,bb)).
                                then(argument("call", StringArgumentType.word()).
                                        suggests( (cc, bb) -> suggest(suggestFunctionCalls(cc), bb)).
                                        executes( (cc) -> CarpetServer.scriptServer.events.addEvent(
                                                StringArgumentType.getString(cc, "event"),
                                                null,
                                                StringArgumentType.getString(cc, "call")
                                        )?1:0)).
                                then(literal("from").
                                        then(argument("package", StringArgumentType.word()).
                                                suggests( (cc, bb) -> suggest(CarpetServer.scriptServer.modules.keySet(), bb)).
                                                then(argument("call", StringArgumentType.word()).
                                                        suggests( (cc, bb) -> suggest(suggestFunctionCalls(cc), bb)).
                                                        executes( (cc) -> CarpetServer.scriptServer.events.addEvent(
                                                                StringArgumentType.getString(cc, "event"),
                                                                StringArgumentType.getString(cc, "package"),
                                                                StringArgumentType.getString(cc, "call")
                                                        )?1:0)))))).
                then(literal("remove_from").
                        then(argument("event", StringArgumentType.word()).
                                suggests( (cc, bb) -> suggest(CarpetServer.scriptServer.events.eventHandlers.keySet() ,bb)).
                                then(argument("call", StringArgumentType.greedyString()).
                                        suggests( (cc, bb) -> suggest(CarpetServer.scriptServer.events.eventHandlers.get(StringArgumentType.getString(cc, "event")).callList.stream().map(CarpetEventServer.Callback::toString), bb)).
                                        executes( (cc) -> CarpetServer.scriptServer.events.removeEvent(
                                                StringArgumentType.getString(cc, "event"),
                                                StringArgumentType.getString(cc, "call")
                                        )?1:0))));


        dispatcher.register(literal("script").
                requires((player) -> CarpetSettings.commandScript).
                then(b).then(u).then(o).then(l).then(s).then(c).then(h).then(i).then(e).then(t).then(a).then(f).then(q));
        dispatcher.register(literal("script").
                requires((player) -> CarpetSettings.commandScript).
                then(literal("in").
                        then(argument("package", StringArgumentType.word()).
                                suggests( (cc, bb) -> suggest(CarpetServer.scriptServer.modules.keySet(), bb)).
                                then(b).then(u).then(o).then(l).then(s).then(c).then(h).then(i).then(e).then(t))));
    }
    private static ScriptHost getHost(CommandContext<CommandSource> context)
    {
        try
        {
            String name = StringArgumentType.getString(context, "package").toLowerCase(Locale.ROOT);
            return CarpetServer.scriptServer.modules.getOrDefault(name, CarpetServer.scriptServer.globalHost);

        }
        catch (IllegalArgumentException ignored)
        {
            return CarpetServer.scriptServer.globalHost;
        }
    }
    private static Collection<String> suggestFunctionCalls(CommandContext<CommandSource> c)
    {
        CommandSource s = c.getSource();
        ScriptHost host = getHost(c);
        return host.getPublicFunctions();
    }
    private static int listEvents(CommandSource source)
    {
        Messenger.m(source, "w Lists ALL event handlers:");
        for (String event: CarpetServer.scriptServer.events.eventHandlers.keySet())
        {
            boolean shownEvent = false;
            for (CarpetEventServer.Callback c: CarpetServer.scriptServer.events.eventHandlers.get(event).callList)
            {
                if (!shownEvent)
                {
                    Messenger.m(source, "w Handlers for "+event+": ");
                    shownEvent = true;
                }
                Messenger.m(source, "w  - "+c.udf+(c.host==null?"":" (from "+c.host+")"));
            }
        }
        return 1;
    }
    private static int listGlobals(CommandContext<CommandSource> context)
    {
        ScriptHost host = getHost(context);
        CommandSource source = context.getSource();

        Messenger.m(source, "lb Stored functions"+((host == CarpetServer.scriptServer.globalHost)?":":" in "+host.getName()+":"));
        for (String fname : host.getAvailableFunctions())
        {
            Expression expr = host.getExpressionForFunction(fname);
            Tokenizer.Token tok = host.getTokenForFunction(fname);
            List<String> snippet = ExpressionInspector.Expression_getExpressionSnippet(tok, expr);
            Messenger.m(source, "wb "+fname,"t  defined at: line "+(tok.lineno+1)+" pos "+(tok.linepos+1));
            for (String snippetLine: snippet)
            {
                Messenger.m(source, "w "+snippetLine);
            }
            Messenger.m(source, "gi ----------------");
        }
        Messenger.m(source, "w  ");
        Messenger.m(source, "lb Global variables"+((host == CarpetServer.scriptServer.globalHost)?":":" in "+host.getName()+":"));

        for (String vname : host.globalVariables.keySet())
        {
            if (!vname.startsWith("global")) continue;
            Messenger.m(source, "wb "+vname+": ", "w "+ host.globalVariables.get(vname).evalValue(null).getPrettyString());
        }
        return 1;
    }

    public static void handleCall(CommandSource source, Supplier<String> call)
    {
        try
        {
            CarpetServer.scriptServer.setChatErrorSnooper(source);
            long start = System.nanoTime();
            String result = call.get();
            long time = ((System.nanoTime()-start)/1000);
            String metric = "\u00B5s";
            if (time > 5000)
            {
                time /= 1000;
                metric = "ms";
            }
            if (time > 10000)
            {
                time /= 1000;
                metric = "s";
            }
            Messenger.m(source, "wi  = ", "wb "+result, "gi  ("+time+metric+")");
        }
        catch (CarpetExpressionException e)
        {
            Messenger.m(source, "r Exception white evaluating expression at "+new BlockPos(source.getPos())+": "+e.getMessage());
        }
        CarpetServer.scriptServer.resetErrorSnooper();
    }

    private static int invoke(CommandContext<CommandSource> context, String call, BlockPos pos1, BlockPos pos2,  String args)
    {
        CommandSource source = context.getSource();
        ScriptHost host = getHost(context);
        if (call.startsWith("__"))
        {
            Messenger.m(source, "r Hidden functions are only callable in scripts");
            return 0;
        }
        List<Integer> positions = new ArrayList<>();
        if (pos1 != null)
        {
            positions.add(pos1.getX());
            positions.add(pos1.getY());
            positions.add(pos1.getZ());
        }
        if (pos2 != null)
        {
            positions.add(pos2.getX());
            positions.add(pos2.getY());
            positions.add(pos2.getZ());
        }
        //if (!(args.trim().isEmpty()))
        //    arguments.addAll(Arrays.asList(args.trim().split("\\s+")));
        handleCall(source, () ->  host.call(source, call, positions, args));
        return 1;
    }


    private static int compute(CommandContext<CommandSource> context, String expr)
    {
        CommandSource source = context.getSource();
        ScriptHost host = getHost(context);
        handleCall(source, () -> {
            CarpetExpression ex = new CarpetExpression(expr, source, new BlockPos(0, 0, 0));
            return ex.scriptRunCommand(host, new BlockPos(source.getPos()));
        });
        return 1;
    }

    private static int scriptScan(CommandContext<CommandSource> context, BlockPos origin, BlockPos a, BlockPos b, String expr)
    {
        CommandSource source = context.getSource();
        ScriptHost host = getHost(context);
        MutableBoundingBox area = new MutableBoundingBox(a, b);
        CarpetExpression cexpr = new CarpetExpression(expr, source, origin);
        if (area.getXSize() * area.getYSize() * area.getZSize() > CarpetSettings.fillLimit)
        {
            Messenger.m(source, "r too many blocks to evaluate: " + area.getXSize() * area.getYSize() * area.getZSize());
            return 1;
        }
        int successCount = 0;
        try
        {
            for (int x = area.minX; x <= area.maxX; x++)
            {
                for (int y = area.minY; y <= area.maxY; y++)
                {
                    for (int z = area.minZ; z <= area.maxZ; z++)
                    {
                        try
                        {
                            if (cexpr.fillAndScanCommand(host, x, y, z)) successCount++;
                        }
                        catch (ArithmeticException ignored)
                        {
                        }
                    }
                }
            }
        }
        catch (CarpetExpressionException exc)
        {
            Messenger.m(source, "r Error while processing command: "+exc);
            return 0;
        }
        Messenger.m(source, "w Expression successful in " + successCount + " out of " + area.getXSize() * area.getYSize() * area.getZSize() + " blocks");
        return successCount;

    }


    private static int scriptFill(CommandContext<CommandSource> context, BlockPos origin, BlockPos a, BlockPos b, String expr,
                                  BlockStateInput block, Predicate<BlockWorldState> replacement, String mode)
    {
        CommandSource source = context.getSource();
        ScriptHost host = getHost(context);
        MutableBoundingBox area = new MutableBoundingBox(a, b);
        CarpetExpression cexpr = new CarpetExpression(expr, source, origin);
        if (area.getXSize() * area.getYSize() * area.getZSize() > CarpetSettings.fillLimit)
        {
            Messenger.m(source, "r too many blocks to evaluate: "+ area.getXSize() * area.getYSize() * area.getZSize());
            return 1;
        }

        boolean[][][] volume = new boolean[area.getXSize()][area.getYSize()][area.getZSize()];

        BlockPos.MutableBlockPos mbpos = new BlockPos.MutableBlockPos(origin);
        WorldServer world = source.getWorld();


        for (int x = area.minX; x <= area.maxX; x++)
        {
            for (int y = area.minY; y <= area.maxY; y++)
            {
                for (int z = area.minZ; z <= area.maxZ; z++)
                {
                    try
                    {
                        if (cexpr.fillAndScanCommand(host, x, y, z))
                        {
                            volume[x-area.minX][y-area.minY][z-area.minZ]=true;
                        }
                    }
                    catch (CarpetExpressionException e)
                    {
                        Messenger.m(source, "r Exception while filling the area:\n","l "+e.getMessage());
                        return 0;
                    }
                    catch (ArithmeticException e)
                    {
                    }
                }
            }
        }
        final int maxx = area.getXSize()-1;
        final int maxy = area.getYSize()-1;
        final int maxz = area.getZSize()-1;
        if ("outline".equalsIgnoreCase(mode))
        {
            boolean[][][] newVolume = new boolean[area.getXSize()][area.getYSize()][area.getZSize()];
            for (int x = 0; x <= maxx; x++)
            {
                for (int y = 0; y <= maxy; y++)
                {
                    for (int z = 0; z <= maxz; z++)
                    {
                        if (volume[x][y][z])
                        {
                            if ( (  (x != 0    && !volume[x-1][y  ][z  ]) ||
                                    (x != maxx && !volume[x+1][y  ][z  ]) ||
                                    (y != 0    && !volume[x  ][y-1][z  ]) ||
                                    (y != maxy && !volume[x  ][y+1][z  ]) ||
                                    (z != 0    && !volume[x  ][y  ][z-1]) ||
                                    (z != maxz && !volume[x  ][y  ][z+1])
                            ))
                            {
                                newVolume[x][y][z] = true;
                            }
                        }
                    }
                }
            }
            volume = newVolume;
        }
        int affected = 0;
        for (int x = 0; x <= maxx; x++)
        {
            for (int y = 0; y <= maxy; y++)
            {
                for (int z = 0; z <= maxz; z++)
                {
                    if (volume[x][y][z])
                    {
                        mbpos.setPos(x+area.minX, y+area.minY, z+area.minZ);
                        if (replacement == null || replacement.test(
                                new BlockWorldState( world, mbpos, true)))
                        {
                            TileEntity tileentity = world.getTileEntity(mbpos);
                            if (tileentity instanceof IInventory)
                            {
                                ((IInventory)tileentity).clear();
                            }
                            if (block.place(
                                    world,
                                    mbpos,
                                    2 | (CarpetSettings.fillUpdates ?0:1024)
                            ))
                            {
                                ++affected;
                            }
                        }
                    }
                }
            }
        }
        if (CarpetSettings.fillUpdates && block != null)
        {
            for (int x = 0; x <= maxx; x++)
            {
                for (int y = 0; y <= maxy; y++)
                {
                    for (int z = 0; z <= maxz; z++)
                    {
                        if (volume[x][y][z])
                        {
                            mbpos.setPos(x+area.minX, y+area.minY, z+area.minZ);
                            Block blokc = world.getBlockState(mbpos).getBlock();
                            world.notifyNeighbors(mbpos, blokc);
                        }
                    }
                }
            }
        }
        Messenger.m(source, "gi Affected "+affected+" blocks in "+area.getXSize() * area.getYSize() * area.getZSize()+" block volume");
        return 1;
    }
}

