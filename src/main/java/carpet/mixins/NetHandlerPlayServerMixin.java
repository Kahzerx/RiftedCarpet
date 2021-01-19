package carpet.mixins;

import carpet.fakes.CPacketCustomPayloadInterface;
import carpet.network.CarpetClient;
import carpet.network.ServerNetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP player;

    @Inject(method = "processCustomPayload", at = @At(value = "HEAD"), cancellable = true)
    private void onCustomCarpetPayload(CPacketCustomPayload packetIn, CallbackInfo ci){
        ResourceLocation channel = ((CPacketCustomPayloadInterface)packetIn).getPacketChannel();
        if (CarpetClient.CARPET_CHANNEL.equals(channel)){
            ServerNetworkHandler.handleData(((CPacketCustomPayloadInterface)packetIn).getPacketData(), player);
            ci.cancel();
        }
    }
}
