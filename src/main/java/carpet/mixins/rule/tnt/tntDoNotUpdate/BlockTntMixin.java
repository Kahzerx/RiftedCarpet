package carpet.mixins.rule.tnt.tntDoNotUpdate;

import carpet.CarpetSettings;
import net.minecraft.block.BlockTNT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockTNT.class)
public class BlockTntMixin {
    @Redirect(
            method = "onBlockAdded",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;isBlockPowered(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean isTNTDoNotUpdate(World world, BlockPos pos) {
        return !CarpetSettings.tntDoNotUpdate && world.isBlockPowered(pos);
    }
}