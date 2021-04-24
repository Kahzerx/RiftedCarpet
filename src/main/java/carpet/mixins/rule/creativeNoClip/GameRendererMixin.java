package carpet.mixins.rule.creativeNoClip;

import carpet.CarpetSettings;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Redirect(
            method = "updateCameraAndRender(FJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z"
            )
    )
    private boolean canSeeWorld(EntityPlayerSP entityPlayerSP) {
        return entityPlayerSP.isSpectator()
                || (CarpetSettings.creativeNoClip
                && entityPlayerSP.isCreative());
    }
}
