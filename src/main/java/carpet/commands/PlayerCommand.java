package carpet.commands;

import carpet.CarpetSettings;
import carpet.fakes.EntityPlayerMPInterface;
import carpet.patches.EntityPlayerMPFake;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.dimension.DimensionType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

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
                    suggests( (c, b) -> suggest(getPlayers(c.getSource()), b)).
                    then(literal("stop").
                            executes(PlayerCommand::stop)).
                    then(literal("use").
                            executes(PlayerCommand::useOnce).
                            then(literal("once").
                                    executes(PlayerCommand::useOnce)).
                            then(literal("continuous").
                                    executes(PlayerCommand::useContinuous)).
                            then(literal("interval").
                                    then(argument("ticks", integer(2)).
                                            executes(PlayerCommand::useInterval)))).
                    then(literal("jump").
                            executes(PlayerCommand::jumpOnce).
                            then(literal("once").
                                    executes(PlayerCommand::jumpOnce)).
                            then(literal("continuous").
                                    executes(PlayerCommand::jumpContinuous)).
                            then(literal("interval").
                                    then(argument("ticks", integer(2)).
                                            executes(PlayerCommand::jumpInterval)))).
                    then(literal("attack").
                            executes(PlayerCommand::attackOnce).
                            then(literal("once").
                                    executes(PlayerCommand::attackOnce)).
                            then(literal("continuous").
                                    executes(PlayerCommand::attackContinuous)).
                            then(literal("interval").
                                    then(argument("ticks",integer(2)).
                                            executes(PlayerCommand::attackInterval)))).
                    then(literal("drop").
                            executes(PlayerCommand::dropItem)).
                    then(literal("swapHands").
                            executes(PlayerCommand::swapHands)).
                    then(literal("kill").
                            executes(PlayerCommand::kill)).
                    then(literal("shadow").
                            executes(PlayerCommand::shadow)).
                    then(literal("mount").
                            executes(PlayerCommand::mount)).
                    then(literal("dismount").
                            executes(PlayerCommand::dismount)).
                    then(literal("sneak").
                            executes(PlayerCommand::sneak)).
                    then(literal("sprint").
                            executes(PlayerCommand::sprint)).
                    then(literal("look").
                            then(literal("north").
                                    executes(PlayerCommand::lookNorth)).
                            then(literal("south").
                                    executes(PlayerCommand::lookSouth)).
                            then(literal("east").
                                    executes(PlayerCommand::lookEast)).
                            then(literal("west").
                                    executes(PlayerCommand::lookWest)).
                            then(literal("up").
                                    executes(PlayerCommand::lookUp)).
                            then(literal("down").
                                    executes(PlayerCommand::lookDown)).
                            then(argument("direction", RotationArgument.rotation()).
                                    executes(PlayerCommand::lookAround))).
                    then(literal("turn").
                            then(literal("left").
                                    executes(PlayerCommand::turnLeft)).
                            then(literal("right").
                                    executes(PlayerCommand::turnRight)).
                            then(literal("back").
                                    executes(PlayerCommand::turnBack)).
                            then(argument("direction",RotationArgument.rotation()).
                                    executes(PlayerCommand::turn))).
                    then(literal("move").
                            then(literal("forward").
                                    executes(PlayerCommand::moveForward)).
                            then(literal("backward").
                                    executes(PlayerCommand::moveBackward)).
                            then(literal("left").
                                    executes(PlayerCommand::strafeLeft)).
                            then(literal("right").
                                    executes(PlayerCommand::strafeRight))).
                    then(literal("spawn").
                            executes(PlayerCommand::spawn).
                            then(literal("at").
                                    then(argument("position", Vec3Argument.vec3()).
                                            executes(PlayerCommand::spawn).
                                            then(literal("facing").
                                                    then(argument("direction",RotationArgument.rotation()).
                                                            executes(PlayerCommand::spawn).
                                                            then(literal("in").
                                                                    then(argument("dimension", DimensionArgument.getDimension()).
                                                                            executes(PlayerCommand::spawn)))))))));
    dispatcher.register(literalArgumentBuilder);
    }


    private static Collection<String> getPlayers(CommandSource source) {
        Set<String> players = Sets.newLinkedHashSet( Arrays.asList("Steve","Alex"));
        players.addAll(source.getPlayerNames());
        return players;
    }

    private static EntityPlayerMP getPlayer(CommandContext<CommandSource> context) {
        String playerName = getString(context,"player");
        MinecraftServer server = context.getSource().getServer();
        return (EntityPlayerMP) server.getPlayerList().getPlayerByUsername(playerName);
    }

    private static boolean cantSpawn(CommandContext<CommandSource> context) {
        String playerName = getString(context,"player");
        EntityPlayer player = context.getSource().getServer().getPlayerList().getPlayerByUsername(playerName);
        if (player != null) {
            Messenger.m(context.getSource(), "r Player ", "rb "+playerName, "r is already logged on");
            return true;
        }
        return false;
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

    private static boolean cantShadow(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return true;
        EntityPlayer player = getPlayer(context);
        if (player instanceof EntityPlayerMPFake) {
            Messenger.m(context.getSource(), "r Only real players can be shadowed");
            return true;
        }
        return false;
    }

    private static boolean cantReMove(CommandContext<CommandSource> context) {
        if (cantManipulate(context))
            return true;
        EntityPlayer player = getPlayer(context);
        if (!(player instanceof EntityPlayerMPFake)) {
            Messenger.m(context.getSource(), "r Only fake players can be moved or killed");
            return true;
        }
        return false;
    }

    private static int stop(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().stop();
        return 1;
    }

    private static int useOnce(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().useOnce();
        return 1;
    }

    private static int useContinuous(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().setUseForever();
        return 1;
    }

    private static int useInterval(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        int ticks = getInteger(context, "ticks");
        ((EntityPlayerMPInterface)player).getActionPack().setUse(ticks, 0);
        return 1;
    }

    private static int jumpOnce(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().jumpOnce();
        return 1;
    }

    private static int jumpContinuous(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().setJumpForever();
        return 1;
    }

    private static int jumpInterval(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        int ticks = getInteger(context, "ticks");
        ((EntityPlayerMPInterface)player).getActionPack().setJump(ticks, 0);
        return 1;
    }

    private static int attackOnce(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().attackOnce();
        ((EntityPlayerMPInterface)player).getActionPack().resetBlockRemoving(true);

        return 1;
    }

    private static int attackContinuous(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().setAttackForever();
        return 1;
    }

    private static int attackInterval(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        int ticks = getInteger(context, "ticks");
        ((EntityPlayerMPInterface)player).getActionPack().setAttack(ticks, 0);
        return 1;
    }

    private static int dropItem(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().dropItem();
        return 1;
    }

    private static int swapHands(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().swapHands();
        return 1;
    }

    private static int kill(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        getPlayer(context).onKillCommand();
        return 1;
    }

    private static int shadow(CommandContext<CommandSource> context) {
        if (cantShadow(context)) return 0;
        EntityPlayerMPFake.createShadow(
                context.getSource().getServer(),
                getPlayer(context));
        return 1;
    }

    private static int mount(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().mount();
        return 1;
    }

    private static int dismount(CommandContext<CommandSource> context) {
        if (cantManipulate(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().dismount();
        return 1;
    }

    private static int sneak(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().setSneaking(true);
        return 1;
    }

    private static int sprint(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().setSprinting(true);
        return 1;
    }

    private static int lookNorth(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().look("north");
        return 1;
    }

    private static int lookSouth(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().look("south");
        return 1;
    }

    private static int lookEast(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().look("east");
        return 1;
    }

    private static int lookWest(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().look("west");
        return 1;
    }

    private static int lookUp(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().look("up");
        return 1;
    }

    private static int lookDown(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().look("down");
        return 1;
    }

    private static int lookAround(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        Vec2f vec2f = RotationArgument.getRotation(context, "direction").getRotation(context.getSource());
        ((EntityPlayerMPInterface)player).getActionPack().look(vec2f.y, vec2f.x);
        return 1;
    }

    private static int turnRight(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().turn("right");
        return 1;
    }

    private static int turnLeft(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().turn("left");
        return 1;
    }

    private static int turnBack(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().turn("back");
        return 1;
    }

    private static int turn(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        Vec2f vec2f = RotationArgument.getRotation(context, "direction").getRotation(context.getSource());
        ((EntityPlayerMPInterface)player).getActionPack().turn(vec2f.y,vec2f.x);
        return 1;
    }

    private static int moveForward(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().setForward(1.0F);
        return 1;
    }

    private static int moveBackward(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().setForward(-1.0F);
        return 1;
    }

    private static int strafeLeft(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().setStrafing(-1.0F);
        return 1;
    }

    private static int strafeRight(CommandContext<CommandSource> context) {
        if (cantReMove(context)) return 0;
        EntityPlayerMP player = getPlayer(context);
        ((EntityPlayerMPInterface)player).getActionPack().setStrafing(1.0F);
        return 1;
    }

    private static int spawn(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (cantSpawn(context)) return 0;
        Vec3d pos;
        Vec2f facing;
        DimensionType dim;
        try {
            pos = Vec3Argument.getVec3(context, "position");
        }
        catch (IllegalArgumentException e) {
            pos = context.getSource().getPos();
        }
        try {
            facing = RotationArgument.getRotation(context, "direction").getRotation(context.getSource());
        }
        catch (IllegalArgumentException e) {
            facing = context.getSource().getRotation();
        }
        try {
            dim = DimensionArgument.func_212592_a(context, "dimension");
        }
        catch (IllegalArgumentException e) {
            dim = context.getSource().getWorld().dimension.getType();
        }
        GameType mode = GameType.CREATIVE;
        try {
            EntityPlayerMP player = context.getSource().asPlayer();
            mode = player.interactionManager.getGameType();
        }
        catch (CommandSyntaxException ignored) { }
        EntityPlayer p = EntityPlayerMPFake.createFake(
                getString(context,"player"),
                context.getSource().getServer(),
                pos.x, pos.y, pos.z,
                facing.y, facing.x,
                dim,
                mode);
        if (p == null) {
            Messenger.m(context.getSource(), "rb Player "+getString(context,"player")+" doesn't exist " +
                    "and cannot spawn in online mode. Turn the server offline to spawn non-existing players");
        }
        return 1;
    }
}
