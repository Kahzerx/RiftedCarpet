package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.BlockDispenser;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockDispenser.class)
public class BlockDispenserQCMixin {
    @Redirect(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockPowered(Lnet/minecraft/util/math/BlockPos;)Z", ordinal = 1))
    private boolean checkUpPower(World world, BlockPos pos){
        if (!CarpetSettings.quasiConnectivity) return false;
        return world.isBlockPowered(pos);
    }
}
