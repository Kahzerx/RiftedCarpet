package carpet.mixins;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.PathfindingVisualizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PathNavigate.class)
public class PathNavigatePathfindingMixin {
    @Shadow protected EntityLiving entity;
    long start = 0;
    @Inject(method = "getPathToPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/pathfinding/PathNavigate;getPathSearchRange()F"))
    public void start1(BlockPos pos, CallbackInfoReturnable<Path> cir){
        start = 0;
        if (LoggerRegistry.__pathfinding){
            start = System.nanoTime();
        }
    }

    @Inject(method = "getPathToPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void end1(BlockPos pos, CallbackInfoReturnable<Path> cir, float f, BlockPos blockpos, int i, IBlockReader iblockreader, Path path){
        if (LoggerRegistry.__pathfinding){
            long end = System.nanoTime();
            float duration = (1.0F*((end - start)/1000))/1000;
            PathfindingVisualizer.slowPath(entity, new Vec3d(pos), duration, path != null);
        }
    }

    @Inject(method = "getPathToEntityLiving", at = @At(value = "INVOKE", target = "Lnet/minecraft/pathfinding/PathNavigate;getPathSearchRange()F"))
    public void start2(Entity entityIn, CallbackInfoReturnable<Path> cir){
        start = 0;
        if (LoggerRegistry.__pathfinding){
            start = System.nanoTime();
        }
    }

    @Inject(method = "getPathToEntityLiving", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void end2(Entity entityIn, CallbackInfoReturnable<Path> cir, BlockPos blockpos, float f, BlockPos blockpos1, int i, IBlockReader iblockreader, Path path){
        if (LoggerRegistry.__pathfinding){
            long end = System.nanoTime();
            float duration = (1.0F*((end - start)/1000))/1000;
            PathfindingVisualizer.slowPath(entity, entity.getPositionVector(), duration, path != null);
        }
    }
}
