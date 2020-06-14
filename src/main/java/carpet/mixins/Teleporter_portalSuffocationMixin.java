package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;

@Mixin(Teleporter.class)
public class Teleporter_portalSuffocationMixin {
    double d5;
    double d7;
    @Inject(method = "placeInExistingPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;getAxis()Lnet/minecraft/util/EnumFacing$Axis;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onPlaceInExistingPortal(Entity entityIn, float rotationYaw, CallbackInfoReturnable<Boolean> cir, int i, double d0, int j, int k, boolean flag, BlockPos blockpos, long l, double d5, double d7, BlockPattern.PatternHelper blockpattern$patternhelper, boolean flag1, double d2, double d6){
        double offset = (1.0D - entityIn.getLastPortalVec().x) * (double)blockpattern$patternhelper.getWidth() * (double)blockpattern$patternhelper.getForwards().rotateY().getAxisDirection().getOffset();
        this.d5 = d5;
        this.d7 = d7;
        if (CarpetSettings.portalSuffocationFix){
            double entity_corrected_radius = 1.02 * (double) entityIn.width / 2;
            if (entity_corrected_radius >= (double)blockpattern$patternhelper.getWidth() - entity_corrected_radius){
                //entity is wider than portal, so will suffocate anyways, so place it directly in the middle
                entity_corrected_radius = (double)blockpattern$patternhelper.getWidth() / 2-0.001;
            }
            if (offset >= 0){
                offset = MathHelper.clamp(offset, entity_corrected_radius, (double)blockpattern$patternhelper.getWidth() - entity_corrected_radius);
            }
            else{
                offset = MathHelper.clamp(offset, -(double)blockpattern$patternhelper.getWidth() + entity_corrected_radius, -entity_corrected_radius);
            }
        }
        if (blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X){
            this.d7 = d2 + offset;
        }
        else {
            this.d5 = d2 + offset;
        }
    }

    @Redirect(method = "placeInExistingPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setLocationAndAngles(DDDFF)V"))
    private void FixSetLocationAndAngles(Entity entity, double x, double y, double z, float yaw, float pitch){
        entity.setLocationAndAngles(d5, y, d7, yaw, pitch);
    }

    @Redirect(method = "placeInExistingPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;setPlayerLocation(DDDFF)V"))
    private void FixSetPlayerLocation(NetHandlerPlayServer netHandlerPlayServer, double x, double y, double z, float yaw, float pitch){
        netHandlerPlayServer.setPlayerLocation(d5, y, d7, yaw, pitch, Collections.emptySet());
    }
}
