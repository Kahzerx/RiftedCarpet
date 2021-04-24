package carpet.mixins.protocol;

import carpet.network.CarpetClient;
import carpet.network.ClientNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {
    @Shadow private Minecraft client;

    @Inject(
            method = "handleCustomPayload",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void handleCustomPayload(SPacketCustomPayload packetIn, CallbackInfo ci) {
        if (CarpetClient.CARPET_CHANNEL.equals(packetIn.getChannelName())) {
            ClientNetworkHandler.handleData(packetIn.getBufferData(), client.player);
            ci.cancel();
        }
    }

    @Inject(
            method = "handleJoinGame",
            at = @At(
                    value = "RETURN"
            )
    )
    private void handleJoinGame(SPacketJoinGame packetIn, CallbackInfo ci) {
        CarpetClient.gameJoined(client.player);
    }

    @Inject(
            method = "onDisconnect",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onCMDisconnected(ITextComponent reason, CallbackInfo ci) {
        CarpetClient.disconnect();
    }
}
