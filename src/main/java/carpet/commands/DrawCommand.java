package carpet.commands;

import carpet.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.ISuggestionProvider.suggest;

public class DrawCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralArgumentBuilder<CommandSource> command = literal("draw").
                requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandDraw)).
                then(literal("sphere").
                        then(argument("center", BlockPosArgument.blockPos()).
                                then(argument("radius", IntegerArgumentType.integer(1)).
                                        then(drawShape(c -> drawSphere(c, false)))))).
                then(literal("ball").
                        then(argument("center", BlockPosArgument.blockPos()).
                                then(argument("radius", IntegerArgumentType.integer(1)).
                                        then(drawShape(c -> drawSphere(c, true)))))).
                then(literal("diamond").
                        then(argument("center", BlockPosArgument.blockPos()).
                                then(argument("radius", IntegerArgumentType.integer(1)).
                                        then(drawShape(c -> drawDiamond(c, true)))))).
                then(literal("pyramid").
                        then(argument("center", BlockPosArgument.blockPos()).
                                then(argument("radius", IntegerArgumentType.integer(1)).
                                        then(argument("height", IntegerArgumentType.integer(1)).
                                                then(argument("pointing", word()).suggests((c, b) -> suggest(new String[]{"up", "down"}, b)).
                                                        then(argument("orientation", word()).suggests((c, b) -> suggest(new String[]{"y", "x", "z"}, b)).
                                                                then(drawShape(c -> drawPyramid(c, "square", true))))))))).
                then(literal("cone").
                        then(argument("center", BlockPosArgument.blockPos()).
                                then(argument("radius", IntegerArgumentType.integer(1)).
                                        then(argument("height",IntegerArgumentType.integer(1)).
                                                then(argument("pointing",StringArgumentType.word()).suggests( (c, b) -> suggest(new String[]{"up","down"},b)).
                                                        then(argument("orientation",word()).suggests( (c, b) -> suggest(new String[]{"y","x","z"},b)).
                                                                then(drawShape(c -> drawPyramid(c, "circle", true))))))))).
                then(literal("cylinder").
                        then(argument("center", BlockPosArgument.blockPos()).
                                then(argument("radius", IntegerArgumentType.integer(1)).
                                        then(argument("height",IntegerArgumentType.integer(1)).
                                                then(argument("orientation",word()).suggests( (c, b) -> suggest(new String[]{"y","x","z"},b)).
                                                        then(drawShape(c -> drawPrism(c, "circle")))))))).
                then(literal("cuboid").
                        then(argument("center", BlockPosArgument.blockPos()).
                                then(argument("radius", IntegerArgumentType.integer(1)).
                                        then(argument("height",IntegerArgumentType.integer(1)).
                                                then(argument("orientation",word()).suggests( (c, b) -> suggest(new String[]{"y","x","z"},b)).
                                                        then(drawShape(c -> drawPrism(c, "square"))))))));

        dispatcher.register(command);
    }

    @FunctionalInterface
    private interface OptionalBlockSelector {
        Integer apply(final CommandContext<CommandSource> ctx) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private interface ArgumentExtractor<T> {
        T apply(final CommandContext<CommandSource> ctx, final String argName) throws CommandSyntaxException;
    }

    private static RequiredArgumentBuilder<CommandSource, BlockStateInput> drawShape(OptionalBlockSelector drawer){
        return argument("block", BlockStateArgument.blockState()).
                executes(drawer::apply).
                then(literal("replace").
                        then(argument("filter", BlockPredicateArgument.blockPredicate()).
                                executes(drawer::apply)));
    }

    private static <T> T getArg(CommandContext<CommandSource> ctx, ArgumentExtractor<T> extract, String hwat) throws CommandSyntaxException {
        return getArg(ctx, extract, hwat, false);
    }

    private static <T> T getArg(CommandContext<CommandSource> ctx, ArgumentExtractor<T> extract, String hwat, boolean optional) throws CommandSyntaxException {
        T arg = null;
        try {
            arg = extract.apply(ctx, hwat);
        }
        catch (IllegalArgumentException e) {
            if (optional) return null;
            Messenger.m(ctx.getSource(), "rb Missing "+hwat);
            throw new ErrorHandled();
        }
        return arg;
    }

    private static class ErrorHandled extends RuntimeException {}

    private static double lengthSq(double x, double y, double z)
    {
        return (x * x) + (y * y) + (z * z);
    }

    private static int setBlock(WorldServer world, BlockPos.MutableBlockPos mbpos, int x, int y, int z, BlockStateInput block, Predicate<BlockWorldState> replacement, List<BlockPos> list) {
        mbpos.setPos(x, y, z);
        int success=0;
        if (replacement == null || replacement.test(new BlockWorldState(world, mbpos, true))) {
            TileEntity tileentity = world.getTileEntity(mbpos);
            if (tileentity instanceof IInventory) {
                ((IInventory) tileentity).clear();
            }
            if (block.place(world, mbpos, 2)) {
                list.add(mbpos.toImmutable());
                ++success;
            }
        }

        return success;
    }

    private static int fillFlat(WorldServer world, BlockPos pos, int offset, double dr, boolean rectangle, String orientation, BlockStateInput block, Predicate<BlockWorldState> replacement, List<BlockPos> list, BlockPos.MutableBlockPos mbpos) {
        int successes=0;
        int r = MathHelper.floor(dr);
        double drsq = dr*dr;
        if (orientation.equalsIgnoreCase("x")) {
            for(int a=-r; a<=r; ++a) for(int b=-r; b<=r; ++b) if(rectangle || a*a + b*b <= drsq) {
                successes += setBlock(world, mbpos,pos.getX()+offset, pos.getY()+a, pos.getZ()+b, block, replacement, list);
            }
            return successes;
        }
        if (orientation.equalsIgnoreCase("y")) {
            for(int a=-r; a<=r; ++a) for(int b=-r; b<=r; ++b) if(rectangle || a*a + b*b <= drsq) {
                successes += setBlock(world, mbpos,pos.getX()+a, pos.getY()+offset, pos.getZ()+b, block, replacement, list);
            }
            return successes;
        }
        if (orientation.equalsIgnoreCase("z")) {
            for(int a=-r; a<=r; ++a) for(int b=-r; b<=r; ++b) if(rectangle || a*a + b*b <= drsq) {
                successes += setBlock(world, mbpos,pos.getX()+b, pos.getY()+a, pos.getZ()+offset, block, replacement, list);
            }
            return successes;
        }
        return 0;
    }

    private static int drawSphere(CommandContext<CommandSource> ctx, boolean solid) throws CommandSyntaxException {
        BlockPos pos;
        int radius;
        BlockStateInput block;
        Predicate<BlockWorldState> replacement;
        try {
            pos = getArg(ctx, BlockPosArgument::getBlockPos, "center");
            radius = getArg(ctx, IntegerArgumentType::getInteger, "radius");
            block = getArg(ctx, BlockStateArgument::getBlockState, "block");
            replacement = getArg(ctx, BlockPredicateArgument::getBlockPredicate, "filter", true);
        }
        catch (ErrorHandled ignored){
            return 0;
        }

        int affected = 0;
        WorldServer world = ctx.getSource().getWorld();

        double radiusX = radius+0.5;
        double radiusY = radius+0.5;
        double radiusZ = radius+0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        BlockPos.MutableBlockPos mbpos = new BlockPos.MutableBlockPos(pos);
        List<BlockPos> list = Lists.newArrayList();

        double nextXn = 0;

        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY: for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    if (!solid && lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1) {
                        continue;
                    }

                    CarpetSettings.impendingFillSkipUpdates = !CarpetSettings.fillUpdates;
                    for (int xmod = -1; xmod < 2; xmod += 2) {
                        for (int ymod = -1; ymod < 2; ymod += 2) {
                            for (int zmod = -1; zmod < 2; zmod += 2) {
                                affected+= setBlock(world, mbpos, pos.getX() + xmod * x, pos.getY() + ymod * y, pos.getZ() + zmod * z, block, replacement, list
                                );
                            }
                        }
                    }
                    CarpetSettings.impendingFillSkipUpdates = false;
                }
            }
        }
        if (CarpetSettings.fillUpdates) {
            list.forEach(blockpos1 -> world.notifyNeighbors(blockpos1, world.getBlockState(blockpos1).getBlock()));
        }
        Messenger.m(ctx.getSource(), "gi Filled " + affected + " blocks");
        return affected;
    }

    private static int drawDiamond(CommandContext<CommandSource> ctx, boolean solid) throws CommandSyntaxException {
        BlockPos pos;
        int radius;
        BlockStateInput block;
        Predicate<BlockWorldState> replacement;
        try {
            pos = getArg(ctx, BlockPosArgument::getBlockPos, "center");
            radius = getArg(ctx, IntegerArgumentType::getInteger, "radius");
            block = getArg(ctx, BlockStateArgument::getBlockState, "block");
            replacement = getArg(ctx, BlockPredicateArgument::getBlockPredicate, "filter", true);
        }
        catch (ErrorHandled ignored) { return 0; }

        CommandSource source = ctx.getSource();

        int affected=0;

        BlockPos.MutableBlockPos mbpos = new BlockPos.MutableBlockPos(pos);
        List<BlockPos> list = Lists.newArrayList();

        WorldServer world = source.getWorld();

        CarpetSettings.impendingFillSkipUpdates = !CarpetSettings.fillUpdates;

        for (int r = 0; r < radius; ++r) {
            int y=r-radius+1;
            for (int x = -r; x <= r; ++x) {
                int z=r-Math.abs(x);

                affected+= setBlock(world, mbpos, pos.getX()+x, pos.getY()-y, pos.getZ()+z, block, replacement, list);
                affected+= setBlock(world, mbpos, pos.getX()+x, pos.getY()-y, pos.getZ()-z, block, replacement, list);
                affected+= setBlock(world, mbpos, pos.getX()+x, pos.getY()+y, pos.getZ()+z, block, replacement, list);
                affected+= setBlock(world, mbpos, pos.getX()+x, pos.getY()+y, pos.getZ()-z, block, replacement, list);
            }
        }

        CarpetSettings.impendingFillSkipUpdates = false;

        if (CarpetSettings.fillUpdates) {
            list.forEach(p -> world.notifyNeighbors(p, world.getBlockState(p).getBlock()));
        }

        Messenger.m(source, "gi Filled " + affected + " blocks");

        return affected;
    }

    private static int drawPyramid(CommandContext<CommandSource> ctx, String base, boolean solid) throws CommandSyntaxException {
        BlockPos pos;
        double radius;
        int height;
        boolean pointup;
        String orientation;
        BlockStateInput block;
        Predicate<BlockWorldState> replacement;
        try {
            pos = getArg(ctx, BlockPosArgument::getBlockPos, "center");
            radius = getArg(ctx, IntegerArgumentType::getInteger, "radius")+0.5D;
            height = getArg(ctx, IntegerArgumentType::getInteger, "height");
            pointup = getArg(ctx, StringArgumentType::getString, "pointing").equalsIgnoreCase("up");
            orientation = getArg(ctx, StringArgumentType::getString,"orientation");
            block = getArg(ctx, BlockStateArgument::getBlockState, "block");
            replacement = getArg(ctx, BlockPredicateArgument::getBlockPredicate, "filter", true);
        }
        catch (ErrorHandled ignored) { return 0; }

        CommandSource source = ctx.getSource();

        int affected = 0;
        BlockPos.MutableBlockPos mbpos = new BlockPos.MutableBlockPos(pos);

        List<BlockPos> list = Lists.newArrayList();

        WorldServer world = source.getWorld();

        CarpetSettings.impendingFillSkipUpdates = !CarpetSettings.fillUpdates;

        boolean isSquare = base.equalsIgnoreCase("square");

        for(int i =0; i<height;++i) {
            double r = pointup ? radius - radius * i / height - 1 : radius * i / height;
            affected+= fillFlat(world, pos, i, r, isSquare, orientation, block, replacement, list, mbpos);
        }

        CarpetSettings.impendingFillSkipUpdates = false;

        if (CarpetSettings.fillUpdates) {

            for (BlockPos blockpos1 : list) {
                Block blokc = world.getBlockState(blockpos1).getBlock();world.notifyNeighbors(blockpos1, blokc);
            }
        }

        Messenger.m(source, "gi Filled " + affected + " blocks");

        return affected;
    }

    private static int drawPrism(CommandContext<CommandSource> ctx, String base){
        BlockPos pos;
        double radius;
        int height;
        String orientation;
        BlockStateInput block;
        Predicate<BlockWorldState> replacement;
        try {
            pos = getArg(ctx, BlockPosArgument::getBlockPos, "center");
            radius = getArg(ctx, IntegerArgumentType::getInteger, "radius")+0.5D;
            height = getArg(ctx, IntegerArgumentType::getInteger, "height");
            orientation = getArg(ctx, StringArgumentType::getString,"orientation");
            block = getArg(ctx, BlockStateArgument::getBlockState, "block");
            replacement = getArg(ctx, BlockPredicateArgument::getBlockPredicate, "filter", true);
        }
        catch (ErrorHandled | CommandSyntaxException ignored) { return 0; }

        CommandSource source = ctx.getSource();

        int affected = 0;
        BlockPos.MutableBlockPos mbpos = new BlockPos.MutableBlockPos(pos);

        List<BlockPos> list = Lists.newArrayList();

        WorldServer world = source.getWorld();

        CarpetSettings.impendingFillSkipUpdates = !CarpetSettings.fillUpdates;

        boolean isSquare = base.equalsIgnoreCase("square");

        for(int i =0; i<height;++i)
        {
            affected+= fillFlat(world, pos, i, radius, isSquare, orientation, block, replacement, list, mbpos);
        }

        CarpetSettings.impendingFillSkipUpdates = false;

        if (CarpetSettings.fillUpdates) {

            for (BlockPos blockpos1 : list) {
                Block blokc = world.getBlockState(blockpos1).getBlock();world.notifyNeighbors(blockpos1, blokc);
            }
        }

        Messenger.m(source, "gi Filled " + affected + " blocks");

        return affected;
    }
}
