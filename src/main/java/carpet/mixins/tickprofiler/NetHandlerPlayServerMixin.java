package carpet.mixins.tickprofiler;

import carpet.CarpetServer;
import carpet.helpers.TickSpeed;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP player;

    @Inject(
            method = "processInput",
            at = @At(
                    value = "RETURN"
            )
    )
    public void resetPlayerActive(CPacketInput packetIn, CallbackInfo ci){
        if (packetIn.getStrafeSpeed() != 0.0F || packetIn.getForwardSpeed() != 0.0F || packetIn.isJumping() || packetIn.isSneaking()) {
            CarpetServer.scriptServer.events.onMountControls(
                    player,
                    packetIn.getStrafeSpeed(),
                    packetIn.getForwardSpeed(),
                    packetIn.isJumping(),
                    packetIn.isSneaking());
            TickSpeed.reset_player_active_timeout();
        }
    }

    @Inject(
            method = "processPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayerMP;isPlayerSleeping()Z",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void resetPlayer2(CPacketPlayer packetIn, CallbackInfo ci, WorldServer worldserver, double d0, double d1, double d2, double d3, double d4, double d5, double d6, float f, float f1, double d7, double d8, double d9, double d10, double d11){
        if (d11 > 0.0001D) {
            TickSpeed.reset_player_active_timeout();
        }
    }
}
