package carpet.mixins;

import carpet.helpers.BlockRotator;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockDispenser.class)
public abstract class BlockDispenserCactusMixin {
    @Inject(method = "getBehavior", at = @At("HEAD"), cancellable = true)
    private void registerCarpetBehaviors(ItemStack stack, CallbackInfoReturnable<IBehaviorDispenseItem> cir){
        Item item = stack.getItem();
        if (item == Blocks.CACTUS.asItem()) cir.setReturnValue(new BlockRotator.CactusDispenserBehaviour());
    }
}
