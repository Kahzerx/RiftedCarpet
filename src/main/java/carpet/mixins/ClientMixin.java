package carpet.mixins;

import carpet.CarpetServer;
import carpet.helpers.TickSpeed;
import carpet.network.CarpetClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ClientMixin {
    @Shadow public WorldClient world;

    @Inject(method = "run", at = @At(value = "HEAD"))
    private void onInit(CallbackInfo ci) {
        CarpetServer.onGameStarted();
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void onClientTick(CallbackInfo ci) {
        if (this.world != null) {
            TickSpeed.tick();
        }
    }
}
