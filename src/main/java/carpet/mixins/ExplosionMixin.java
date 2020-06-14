package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow @Final private List<BlockPos> affectedBlockPositions;
    @Inject(method = "doExplosionB", at = @At("HEAD"), cancellable = true)
    private void onExplosionB(boolean spawnParticles, CallbackInfo ci){
        if (CarpetSettings.explosionNoBlockDamage) affectedBlockPositions.clear();
    }

    @Redirect(method = "doExplosionA", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState onExplosionA(World world, BlockPos blockPos){
        if (CarpetSettings.explosionNoBlockDamage) return Blocks.BEDROCK.getDefaultState();
        return world.getBlockState(blockPos);
    }
}
