package carpet.mixins;

import carpet.CarpetSettings;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;

@Mixin(Teleporter.class)
public class Teleporter_portalSuffocationMixin {
    @Shadow @Final private Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache;
    @Shadow @Final private WorldServer world;
    private static final BlockPortal BLOCK_NETHER_PORTAL = (BlockPortal) Blocks.NETHER_PORTAL;
    double d5;
    double d7;

    @Inject(method = "placeInExistingPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;getAxis()Lnet/minecraft/util/EnumFacing$Axis;", ordinal = 1))
    public void placeInPortal(Entity entityIn, float rotationYaw, CallbackInfoReturnable<Boolean> cir){
        double d0 = -1.0D;
        int j = MathHelper.floor(entityIn.posX);
        int k = MathHelper.floor(entityIn.posZ);
        boolean flag = true;
        BlockPos blockpos = BlockPos.ORIGIN;
        long l = ChunkPos.asLong(j, k);
        if (this.destinationCoordinateCache.containsKey(l)){
            d0 = 0.0D;
            Teleporter.PortalPosition teleporter$portalposition = this.destinationCoordinateCache.get(l);
            blockpos = teleporter$portalposition;
            flag = false;
        }
        else{
            BlockPos blockpos3 = new BlockPos(entityIn);
            for (int i1 = -128; i1 <= 128; ++i1){
                BlockPos blockpos2;
                for (int j1 = -128; j1 <= 128; ++j1){
                    for (BlockPos blockpos1 = blockpos3.add(i1, this.world.getActualHeight() - 1 - blockpos3.getY(), j1); blockpos1.getY() >= 0; blockpos1 = blockpos2){
                        blockpos2 = blockpos1.down();
                        if (this.world.getBlockState(blockpos1).getBlock() == BLOCK_NETHER_PORTAL){
                            for (blockpos2 = blockpos1.down(); this.world.getBlockState(blockpos2).getBlock() == BLOCK_NETHER_PORTAL; blockpos2 = blockpos2.down()){
                                blockpos1 = blockpos2;
                            }
                            double d1 = blockpos1.distanceSq(blockpos3);
                            if (d0 < 0.0D || d1 < d0){
                                d0 = d1;
                                blockpos = blockpos1;
                            }
                        }
                    }
                }
            }
        }
        if (d0 >= 0.0D){
            this.d5 = (double)blockpos.getX() + 0.5D;
            this.d7 = (double)blockpos.getZ() + 0.5D;
            BlockPattern.PatternHelper blockpattern$patternhelper = BLOCK_NETHER_PORTAL.createPatternHelper(this.world, blockpos);
            boolean flag1 = blockpattern$patternhelper.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
            double d2 = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? (double)blockpattern$patternhelper.getFrontTopLeft().getZ() : (double)blockpattern$patternhelper.getFrontTopLeft().getX();
            if (flag1) ++d2;
            double offset = (1.0D - entityIn.getLastPortalVec().x) * (double)blockpattern$patternhelper.getWidth() * (double)blockpattern$patternhelper.getForwards().rotateY().getAxisDirection().getOffset();
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
