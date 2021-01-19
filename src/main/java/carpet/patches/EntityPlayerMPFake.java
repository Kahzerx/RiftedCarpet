package carpet.patches;

import carpet.fakes.EntityPlayerMPInterface;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;

@SuppressWarnings("EntityConstructor")
public class EntityPlayerMPFake extends EntityPlayerMP {
    public EntityPlayerMPFake(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn) {
        super(server, worldIn, profile, interactionManagerIn);
    }

    public Runnable fixStartingPosition = () -> {};

    public static EntityPlayerMPFake createFake(String username, MinecraftServer server, double d0, double d1, double d2, double yaw, double pitch, DimensionType dimension, GameType gamemode)
    {
        //prolly half of that crap is not necessary, but it works
        WorldServer worldIn = server.getWorld(dimension);
        PlayerInteractionManager interactionManagerIn = new PlayerInteractionManager(worldIn);
        GameProfile gameprofile = server.getPlayerProfileCache().getGameProfileForUsername(username);
        if (gameprofile == null)
        {
            return null;
        }
        if (gameprofile.getProperties().containsKey("textures"))
        {
            gameprofile = TileEntitySkull.updateGameProfile(gameprofile);
        }
        EntityPlayerMPFake instance = new EntityPlayerMPFake(server, worldIn, gameprofile, interactionManagerIn);
        instance.fixStartingPosition = () -> instance.setLocationAndAngles(d0, d1, d2, (float)yaw, (float)pitch);
        server.getPlayerList().initializeConnectionToPlayer(new NetworkManagerFake(EnumPacketDirection.CLIENTBOUND), instance);
        if (instance.dimension != dimension) //player was logged in in a different dimension
        {
            WorldServer old_world = server.getWorld(instance.dimension);
            instance.dimension = dimension;
            old_world.removeEntityDangerously(instance);
            instance.removed = false;
            worldIn.spawnEntity(instance);
            instance.setWorld(worldIn);
            server.getPlayerList().preparePlayer(instance, old_world);
            instance.connection.setPlayerLocation(d0, d1, d2, (float)yaw, (float)pitch);
            instance.interactionManager.setWorld(worldIn);
        }
        instance.setHealth(20.0F);
        instance.removed = false;
        instance.connection.setPlayerLocation(d0, d1, d2, (float)yaw, (float)pitch);
        instance.stepHeight = 0.6F;
        interactionManagerIn.setGameType(gamemode);
        server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityHeadLook(instance, (byte)(instance.rotationYawHead * 256 / 360) ),instance.dimension);
        server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityTeleport(instance),instance.dimension);
        server.getPlayerList().serverUpdateMovingPlayer(instance);
        instance.dataManager.set(PLAYER_MODEL_FLAG, (byte) 0x7f); // show all model layers (incl. capes)
        return instance;
    }

    public static EntityPlayerMPFake createShadow(MinecraftServer server, EntityPlayerMP player)
    {
        player.getServer().getPlayerList().playerLoggedOut(player);
        player.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.duplicate_login"));
        WorldServer worldIn = server.getWorld(player.dimension);
        PlayerInteractionManager interactionManagerIn = new PlayerInteractionManager(worldIn);
        GameProfile gameprofile = player.getGameProfile();
        EntityPlayerMPFake playerShadow = new EntityPlayerMPFake(server, worldIn, gameprofile, interactionManagerIn);
        server.getPlayerList().initializeConnectionToPlayer(new NetworkManagerFake(EnumPacketDirection.CLIENTBOUND), playerShadow);

        playerShadow.setHealth(player.getHealth());
        playerShadow.connection.setPlayerLocation(player.posX, player.posY,player.posZ, player.rotationYaw, player.rotationPitch);
        interactionManagerIn.setGameType(player.interactionManager.getGameType());
        ((EntityPlayerMPInterface)playerShadow).getActionPack().copyFrom(((EntityPlayerMPInterface)player).getActionPack());
        playerShadow.stepHeight = 0.6F;

        server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketEntityHeadLook(playerShadow, (byte)(player.rotationYawHead * 256 / 360) ),playerShadow.dimension);
        server.getPlayerList().sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, playerShadow));
        server.getPlayerList().serverUpdateMovingPlayer(playerShadow);
        return playerShadow;
    }

    @Override
    public void onKillCommand()
    {
        //super.onKillCommand();
        //check for 1.14compatibility
        this.getServer().addScheduledTask( () -> this.getServer().getPlayerList().playerLoggedOut(this) );
    }

    @Override
    public void tick()
    {
        if(this.getServer().getTickCounter() % 10 == 0)
        {
            this.connection.captureCurrentPosition();
            this.getServer().getPlayerList().serverUpdateMovingPlayer(this);
        }
        super.tick();
        this.playerTick();
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        //check for 1.14 compatibility
        super.onDeath(cause);
        this.getServer().addScheduledTask( () -> this.getServer().getPlayerList().playerLoggedOut(this) );
    }

    @Override
    public Entity func_212321_a(DimensionType p_212321_1_){
        Entity res = super.func_212321_a(p_212321_1_);
        this.getServer().addScheduledTask(() -> clearInvulnerableDimensionChange());
        return res;
    }
}
