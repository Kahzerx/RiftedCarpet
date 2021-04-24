package carpet.mixins.rule.missingTools;

import carpet.CarpetSettings;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemPickaxe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemPickaxe.class)
public class ItemPickaxeMixin {
    @Redirect(
            method = "getDestroySpeed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/state/IBlockState;getMaterial()Lnet/minecraft/block/material/Material;"
            )
    )
    private Material getCustomMaterial(IBlockState blockState) {
        Material material = blockState.getMaterial();
        if (CarpetSettings.missingTools
                && (material == Material.PISTON
                || material == Material.GLASS)) {
            material = Material.ROCK;
        }
        return material;
    }
}
