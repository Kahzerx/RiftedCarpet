package carpet.mixins.rotator;

import carpet.helpers.BlockRotator;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnumFacing.class)
public abstract class EnumFacingMixin {
    @Redirect(
            method = "getFacingDirections",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getYaw(F)F"
            )
    )
    private static float getYaw(Entity entity, float partialTicks){
        float yaw = entity.getYaw(partialTicks);
        if (BlockRotator.flippinEligibility(entity)) {
            yaw += 180f;
        }
        return yaw;
    }
    @Redirect(
            method = "getFacingDirections",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getPitch(F)F"
            )
    )
    private static float getPitch(Entity entity, float partialTicks){
        float pitch = entity.getPitch(partialTicks);
        if (BlockRotator.flippinEligibility(entity)) {
            pitch = -pitch;
        }
        return pitch;
    }
}
