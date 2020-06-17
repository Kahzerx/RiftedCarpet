package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPistonBase.class)
public class BlockPistonQCMixin {
    @Inject(method = "shouldBeExtended", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;up()Lnet/minecraft/util/math/BlockPos;"))
    private void cancelUpCheck(World worldIn, BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Boolean> cir){
        if (!CarpetSettings.quasiConnectivity){
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
