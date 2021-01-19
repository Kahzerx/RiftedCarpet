package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemShears.class)
public class ItemShearsMTMixin {
    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void getMat(ItemStack stack, IBlockState state, CallbackInfoReturnable<Float> cir){
        if (CarpetSettings.missingTools && (state.getMaterial() == Material.SPONGE)){
            cir.setReturnValue(15.0F);
            cir.cancel();
        }
    }
}
