package carpet.mixins;

import carpet.patches.EntityPlayerMPFake;
import carpet.patches.NetHandlerPlayServerFake;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListFakePlayerMixin {

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "readPlayerDataFromFile", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void fixStartingPos(EntityPlayerMP playerIn, CallbackInfoReturnable<NBTTagCompound> cir){
        if (playerIn instanceof EntityPlayerMPFake){
            ((EntityPlayerMPFake)playerIn).fixStartingPosition.run();
        }
    }

    @Redirect(method = "initializeConnectionToPlayer", at = @At(value = "NEW", target = "net/minecraft/network/NetHandlerPlayServer"))
    private NetHandlerPlayServer replaceNetworkHandler(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn){
        boolean isServerPlayerEntity = playerIn instanceof EntityPlayerMPFake;
        if (isServerPlayerEntity) return new NetHandlerPlayServerFake(this.server, networkManagerIn, playerIn);
        else return new NetHandlerPlayServer(this.server, networkManagerIn, playerIn);
    }

    @Redirect(method = "createPlayerForUser", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;disconnect(Lnet/minecraft/util/text/ITextComponent;)V"))
    public void disc(NetHandlerPlayServer netHandlerPlayServer, ITextComponent textComponent){
        if (netHandlerPlayServer.player instanceof EntityPlayerMPFake){
            netHandlerPlayServer.player.onKillCommand();
        }
        else{
            netHandlerPlayServer.disconnect(new TextComponentTranslation("multiplayer.disconnect.duplicate_login", new Object[0]));
        }
    }
}
