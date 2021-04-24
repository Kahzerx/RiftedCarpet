package carpet.mixins.rule.fogOff;

import carpet.CarpetSettings;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FogRenderer.class, priority = 69420)
public class FogRendererMixin {
    @Redirect(
            method = "setupFog",
            require = 0,
            expect = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;doesXZShowFog(II)Z"
            )
    )
    private boolean isFogThick(Dimension dimension, int x, int z) {
        if (CarpetSettings.fogOff) {
            return false;
        }
        return dimension.doesXZShowFog(x, z);
    }
}
