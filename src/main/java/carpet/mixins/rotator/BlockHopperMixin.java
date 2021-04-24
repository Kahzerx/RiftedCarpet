package carpet.mixins.rotator;

import carpet.helpers.BlockRotator;
import net.minecraft.block.BlockHopper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockHopper.class)
public class BlockHopperMixin {
    @Redirect(
            method = "getStateForPlacement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/BlockItemUseContext;getFace()Lnet/minecraft/util/EnumFacing;"
            )
    )
    private EnumFacing getOpposite(BlockItemUseContext blockItemUseContext) {
        if (BlockRotator.flippinEligibility(blockItemUseContext.getPlayer())) {
            return blockItemUseContext.getFace().getOpposite();
        }

        return blockItemUseContext.getFace();
    }
}
