package carpet.mixins.rule.xpNoCooldown;

import carpet.CarpetSettings;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityXPOrb.class)
public class EntityXPOrbMixin {
    @Inject(
            method = "onCollideWithPlayer",
            at = @At("HEAD")
    )
    void removeDelay(EntityPlayer entityPlayer, CallbackInfo ci) {
        if (CarpetSettings.xpNoCooldown) entityPlayer.xpCooldown = 0;
    }
}
