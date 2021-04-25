package carpet.mixins.events;

import carpet.CarpetServer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(
            method = "run",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onInit(CallbackInfo ci) {
        CarpetServer.onGameStarted();
    }
}
