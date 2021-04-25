package carpet.commands;

import carpet.CarpetSettings;
import carpet.fakes.EntityPlayerMPInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.RotationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.ISuggestionProvider.suggest;

public class PlayerCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher){
    LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = literal("player").
            requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandPlayer)).
            then(argument("player", word()).
                    suggests((c, b) -> suggest(getPlayers(c.getSource()), b)).
                    then(literal("stop").executes(PlayerCommand::stop)).
                    then(makeActionCommand("use", EntityPlayerActionPack.ActionType.USE)).
                    then(makeActionCommand("jump", EntityPlayerActionPack.ActionType.JUMP)).
                    then(makeActionCommand("attack", EntityPlayerActionPack.ActionType.ATTACK)).
                    then(makeActionCommand("drop", EntityPlayerActionPack.ActionType.DROP_ITEM)).
                    then(makeDropCommand("drop", false)).
                    then(makeActionCommand("dropStack", EntityPlayerActionPack.ActionType.DROP_STACK)).
                    then(makeDropCommand("drop", true)).
                    then(makeActionCommand("swapHands", EntityPlayerActionPack.ActionType.SWAP_HANDS)).
                    then(literal("hotbar").
                            then(argument("slot", integer(1, 9)).
                                    executes(c -> manipulate(c, ap -> ap.setSlot(getInteger(c, "slot")))))).
                    then(literal("kill").executes(PlayerCommand::kill)).
                    then(literal("shadow").executes(PlayerCommand::shadow)).
                    then(literal("mount").executes(manipulation(EntityPlayerActionPack::mount))).
                    then(literal("dismount").executes(manipulation(EntityPlayerActionPack::dismount))).
                    then(literal("sneak").executes(manipulation(ap -> ap.setSneaking(true)))).
                    then(literal("unsneak").executes(manipulation(ap -> ap.setSneaking(false)))).
                    then(literal("sprint").executes(manipulation(ap -> ap.setSprinting(true)))).
                    then(literal("unsprint").executes(manipulation(ap -> ap.setSprinting(false)))).
                    then(literal("look").
                            then(literal("north").executes(manipulation(ap -> ap.look(EnumFacing.NORTH)))).
                            then(literal("south").executes(manipulation(ap -> ap.look(EnumFacing.SOUTH)))).
                            then(literal("east").executes(manipulation(ap -> ap.look(EnumFacing.EAST)))).
                            then(literal("west").executes(manipulation(ap -> ap.look(EnumFacing.WEST)))).
                            then(literal("up").executes(manipulation(ap -> ap.look(EnumFacing.UP)))).
                            then(literal("down").executes(manipulation(ap -> ap.look(EnumFacing.DOWN)))).
                            then(literal("at").
                                    then(argument("position", Vec3Argument.vec3()).executes(PlayerCommand::lookAt))).
                            then(argument("direction", RotationArgument.rotation()).
                                    executes(c -> manipulate(c, ap -> ap.look(RotationArgument.getRotation(c, "direction").getRotation(c.getSource())))))).
                    then(literal("turn").
                            then(literal("left").executes(c -> manipulate(c, ap -> ap.turn(-90, 0)))).
                            then(literal("right").executes(c -> manipulate(c, ap -> ap.turn(90, 0)))).
                            then(literal("back").executes(c -> manipulate(c, ap -> ap.turn(180, 0)))).
                            then(argument("rotation", RotationArgument.rotation()).
                                    executes(c -> manipulate(c, ap -> ap.turn(RotationArgument.getRotation(c, "rotation").getRotation(c.getSource())))))).
                    then(literal("move").executes(c -> manipulate(c, EntityPlayerActionPack::stopMovement)).
                            then(literal("forward").executes(c -> manipulate(c, ap -> ap.setForward(1)))).
                            then(literal("backward").executes(c -> manipulate(c, ap -> ap.setForward(-1)))).
                            then(literal("left").executes(c -> manipulate(c, ap -> ap.setStrafing(1)))).
                            then(literal("right").executes(c -> manipulate(c, ap -> ap.setStrafing(-1))))).
                    then(literal("spawn").executes(PlayerCommand::spawn).
                            then(literal("at").then(argument("position", Vec3Argument.vec3()).
                                    executes(PlayerCommand::spawn).
                                    then(literal("facing").then(argument("direction", RotationArgument.rotation()).
                                            executes(PlayerCommand::spawn).
                                            then(literal("in").then(argument("dimension", DimensionArgument.getDimension()).
                                                    executes(PlayerCommand::spawn)))))))));
    dispatcher.register(literalArgumentBuilder);
    }

    private static int stop(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface) player).getActionPack().stopAll();
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSource> makeActionCommand(String actionName, EntityPlayerActionPack.ActionType type) {
        return literal(actionName)
                .executes(c -> action(c, type, EntityPlayerActionPack.Action.once()))
                .then(literal("once").executes(c -> action(c, type, EntityPlayerActionPack.Action.once())))
                .then(literal("continuous").executes(c -> action(c, type, EntityPlayerActionPack.Action.continuous())))
                .then(literal("interval").then(argument("ticks", IntegerArgumentType.integer(1))
                        .executes(c -> action(c, type, EntityPlayerActionPack.Action.interval(IntegerArgumentType.getInteger(c, "ticks"))))));
    }

    private static int action(CommandContext<CommandSource> context, EntityPlayerActionPack.ActionType type, EntityPlayerActionPack.Action action) {
        return manipulate(context, ap -> ap.start(type, action));
    }

    private static int manipulate(CommandContext<CommandSource> context, Consumer<EntityPlayerActionPack> action) {
        if (cantManipulate(context)) {
            return 0;
        }
        EntityPlayerMP player = getPlayer(context);
        action.accept(((EntityPlayerMPInterface) player).getActionPack());
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSource> makeDropCommand(String actionName, boolean dropAll) {
        return literal(actionName)
                .then(literal("all").executes(c ->manipulate(c, ap -> ap.drop(-2,dropAll))))
                .then(literal("mainhand").executes(c ->manipulate(c, ap -> ap.drop(-1,dropAll))))
                .then(literal("offhand").executes(c ->manipulate(c, ap -> ap.drop(40,dropAll))))
                .then(argument("slot", IntegerArgumentType.integer(0, 40)).
                        executes(c ->manipulate(c, ap -> ap.drop(
                                IntegerArgumentType.getInteger(c,"slot"),
                                dropAll
                        ))));
    }

    private static Command<CommandSource> manipulation(Consumer<EntityPlayerActionPack> action) {
        return c -> manipulate(c, action);
    }

    private static int lookAt(CommandContext<CommandSource> context) {
        return manipulate(context, ap -> {
            try {
                ap.lookAt(Vec3Argument.getVec3(context, "position"));
            } catch (CommandSyntaxException ignored) {}
        });
    }

    private static Collection<String> getPlayers(CommandSource source) {
        Set<String> players = Sets.newLinkedHashSet( Arrays.asList("Steve","Alex"));
        players.addAll(source.getPlayerNames());
        return players;
    }

    private static EntityPlayerMP getPlayer(CommandContext<CommandSource> context) {
        String playerName = getString(context,"player");
        MinecraftServer server = context.getSource().getServer();
        return server.getPlayerList().getPlayerByUsername(playerName);
    }

    private static boolean cantSpawn(CommandContext<CommandSource> context)
    {
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftServer server = context.getSource().getServer();
        PlayerList manager = server.getPlayerList();
        EntityPlayer player = manager.getPlayerByUsername(playerName);
        if (player != null) {
            Messenger.m(context.getSource(), "r Player ", "rb " + playerName, "r  is already logged on");
            return true;
        }
        GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
        if (profile == null) {
            Messenger.m(context.getSource(), "r Player "+playerName+" is either banned by Mojang, or auth servers are down. " +
                    "Banned players can only be summoned in Singleplayer and in servers in off-line mode.");
            return true;
        }
        if (manager.getBannedPlayers().isBanned(profile)) {
            Messenger.m(context.getSource(), "r Player ", "rb " + playerName, "r  is banned on this server");
            return true;
        }
        if (manager.isWhiteListEnabled() && manager.getWhitelistedPlayers().isWhitelisted(profile) && !context.getSource().hasPermissionLevel(2)) {
            Messenger.m(context.getSource(), "r Whitelisted players can only be spawned by operators");
            return true;
        }
        return false;
    }

    private static boolean cantReMove(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return true;
        EntityPlayer player = getPlayer(context);
        if (player instanceof EntityPlayerMPFake) return false;
        Messenger.m(context.getSource(), "r Only fake players can be moved or killed");
        return true;
    }

    @FunctionalInterface
    interface SupplierWithCommandSyntaxException<T>
    {
        T get() throws CommandSyntaxException;
    }

    private static <T> T tryGetArg(SupplierWithCommandSyntaxException<T> a, SupplierWithCommandSyntaxException<T> b) throws CommandSyntaxException
    {
        try
        {
            return a.get();
        }
        catch (IllegalArgumentException e)
        {
            return b.get();
        }
    }

    private static boolean cantManipulate(CommandContext<CommandSource> context) {
        EntityPlayer player = getPlayer(context);
        if (player == null) {
            Messenger.m(context.getSource(),"r Can only manipulate existing players");
            return true;
        }
        EntityPlayer sendingPlayer;
        try {
            sendingPlayer = context.getSource().asPlayer();
        }
        catch (CommandSyntaxException e) {
            return false;
        }

        if (!(context.getSource().getServer().getPlayerList().canSendCommands(sendingPlayer.getGameProfile()))) {
            if (!(sendingPlayer == player || player instanceof EntityPlayerMPFake)) {
                Messenger.m(context.getSource(),"r Non OP players can't control other real players");
                return true;
            }
        }
        return false;
    }

    private static int kill(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        getPlayer(context).onKillCommand();
        return 1;
    }

    private static int shadow(CommandContext<CommandSource> context) {
        EntityPlayerMP player = getPlayer(context);
        if (player instanceof EntityPlayerMPFake) {
            Messenger.m(context.getSource(), "r Cannot shadow fake players");
            return 0;
        }
        EntityPlayerMP sendingPlayer = null;
        try {
            sendingPlayer = context.getSource().asPlayer();
        } catch (CommandSyntaxException ignored) { }

        if (sendingPlayer != player && cantManipulate(context)) return 0;
        EntityPlayerMPFake.createShadow(player.server, player);
        return 1;
    }

    private static int spawn(CommandContext<CommandSource> context) throws CommandSyntaxException
    {
        if (cantSpawn(context)) return 0;
        CommandSource source = context.getSource();
        Vec3d pos = tryGetArg(
                () -> Vec3Argument.getVec3(context, "position"),
                source::getPos
        );
        Vec2f facing = tryGetArg(
                () -> RotationArgument.getRotation(context, "direction").getRotation(context.getSource()),
                source::getRotation
        );

        DimensionType dimType = tryGetArg(
                () -> DimensionArgument.func_212592_a(context, "dimension"),
                () -> source.getWorld().getDimension().getType()
        );

        GameType mode = GameType.CREATIVE;
        try {
            EntityPlayerMP player = context.getSource().asPlayer();
            mode = player.interactionManager.getGameType();
        } catch (CommandSyntaxException ignored) {}
        String playerName = StringArgumentType.getString(context, "player");
        if (playerName.length()>40) {
            Messenger.m(context.getSource(), "rb Player name: "+playerName+" is too long");
            return 0;
        }

        MinecraftServer server = source.getServer();
        if (!World.isValid(new BlockPos(pos.x, pos.y, pos.z))) {
            Messenger.m(context.getSource(), "rb Player "+playerName+" cannot be placed outside of the world");
            return 0;
        }
        EntityPlayer player = EntityPlayerMPFake.createFake(playerName, server, pos.x, pos.y, pos.z, facing.y, facing.x, dimType, mode);
        if (player == null) {
            Messenger.m(context.getSource(), "rb Player " + StringArgumentType.getString(context, "player") + " doesn't exist " +
                    "and cannot spawn in online mode. Turn the server offline to spawn non-existing players");
            return 0;
        }
        return 1;
    }
}
