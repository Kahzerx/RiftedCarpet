package carpet.mixins.rule.portalDelay;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin {
    @Shadow public PlayerCapabilities abilities;

    @Inject(
            method = "getMaxInPortalTime",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void onMaxNetherPortalTime(CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.portalCreativeDelay != 1 && this.abilities.disableDamage) {
            cir.setReturnValue(CarpetSettings.portalCreativeDelay);
        } else if(CarpetSettings.portalSurvivalDelay != 80 && !this.abilities.disableDamage) {
            cir.setReturnValue(CarpetSettings.portalSurvivalDelay);
        }
    }
}
