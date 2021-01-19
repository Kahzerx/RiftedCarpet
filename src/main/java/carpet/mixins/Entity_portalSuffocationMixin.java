package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class Entity_portalSuffocationMixin {
    @Shadow public abstract AxisAlignedBB getBoundingBox();

    @Inject(method = "func_212321_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Teleporter;placeInExistingPortal(Lnet/minecraft/entity/Entity;F)Z"))
    private void registerEntityDimensionChange(DimensionType newDimension, CallbackInfoReturnable<Entity> cir){
        if (CarpetSettings.portalSuffocationFix){
            CarpetSettings.currentTelepotingEntityBox = getBoundingBox();
            CarpetSettings.fixedPosition = null;
        }
    }

    @Redirect(method = "func_212321_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToBlockPosAndAngles(Lnet/minecraft/util/math/BlockPos;FF)V"))
    private void alternativeSetPositionAndAngles(Entity entity, BlockPos blockPos_1, float float_1, float float_2){
        if(CarpetSettings.portalSuffocationFix && CarpetSettings.fixedPosition != null) entity.setLocationAndAngles(CarpetSettings.fixedPosition.x, CarpetSettings.fixedPosition.y, CarpetSettings.fixedPosition.z, float_1, float_2);
        else entity.moveToBlockPosAndAngles(blockPos_1, float_1, float_2);
    }

    @Inject(method = "func_212321_a", at = @At("RETURN"))
    private void removeEntity(DimensionType dimensionType_1, CallbackInfoReturnable<Entity> cir){
        CarpetSettings.currentTelepotingEntityBox = null;
        CarpetSettings.fixedPosition = null;
    }
}
