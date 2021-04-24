package carpet.mixins.tickprofiler;

import carpet.helpers.TickSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow public WorldClient world;

    @Inject(
            method = "runTick",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onClientTick(CallbackInfo ci) {
        if (this.world != null) {
            TickSpeed.tick();
        }
    }
}
