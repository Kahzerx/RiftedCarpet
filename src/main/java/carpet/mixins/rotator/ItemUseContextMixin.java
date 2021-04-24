package carpet.mixins.rotator;

import carpet.helpers.BlockRotator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemUseContext.class)
public class ItemUseContextMixin {
    @Redirect(
            method = "getPlacementHorizontalFacing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;getHorizontalFacing()Lnet/minecraft/util/EnumFacing;"
            )
    )
    private EnumFacing getPlayerFacing(EntityPlayer entityPlayer) {
        EnumFacing enumFacing = entityPlayer.getHorizontalFacing();
        if (BlockRotator.flippinEligibility(entityPlayer)) {
            enumFacing = enumFacing.getOpposite();
        }
        return enumFacing;
    }
}
