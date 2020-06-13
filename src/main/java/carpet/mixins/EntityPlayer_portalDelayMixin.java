package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public class EntityPlayer_portalDelayMixin {
    @Final @Shadow public PlayerCapabilities abilities;

    @Inject(method = "getMaxInPortalTime", at = @At("HEAD"), cancellable = true)
    private void onMaxNetherPortalTime(CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.portalCreativeDelay != 1 && this.abilities.disableDamage) cir.setReturnValue(CarpetSettings.portalCreativeDelay);
        else if(CarpetSettings.portalSurvivalDelay != 80 && !this.abilities.disableDamage) cir.setReturnValue(CarpetSettings.portalSurvivalDelay);
    }
}
