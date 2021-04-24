package carpet.mixins.rule.stackableShulkerBoxes;

import carpet.CarpetSettings;
import carpet.fakes.EntityItemInterface;
import carpet.helpers.InventoryHelper;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityItem.class)
public abstract class EntityItemMixin extends Entity implements EntityItemInterface {
    private static final int SHULKERBOX_MAX_STACK_AMOUNT = 64;

    @Shadow private int age;
    @Shadow private int pickupDelay;

    public EntityItemMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }
    @Override
    public int getAgeCM() {
        return this.age;
    }

    @Override
    public int getPickupDelayCM() {
        return this.pickupDelay;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V",
            at = @At(
                    value = "RETURN"
            )
    )
    private void removeEmptyShulkerBoxTags(World worldIn, double x, double y, double z, ItemStack stack, CallbackInfo ci){
        if (CarpetSettings.stackableShulkerBoxes && stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() instanceof BlockShulkerBox) {
            if (InventoryHelper.cleanUpShulkerBoxTag(stack)) {
                ((EntityItem) (Object) this).setItem(stack);
            }
        }
    }

    @Redirect(
            method = "combineItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getMaxStackSize()I"
            )
    )
    private int getItemStackMaxAmount(ItemStack stack) {
        if (CarpetSettings.stackableShulkerBoxes && stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() instanceof BlockShulkerBox)
            return SHULKERBOX_MAX_STACK_AMOUNT;
        return stack.getMaxStackSize();
    }

    @Inject(
            method = "combineItems",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private void tryStackShulkerBoxes(EntityItem other, CallbackInfoReturnable<Boolean> cir){
        EntityItem self = (EntityItem)(Object)this;
        ItemStack selfStack = self.getItem();
        if (!CarpetSettings.stackableShulkerBoxes || !(selfStack.getItem() instanceof ItemBlock) || !(((ItemBlock)selfStack.getItem()).getBlock() instanceof BlockShulkerBox)) {
            return;
        }

        ItemStack otherStack = other.getItem();
        if (selfStack.getItem() == otherStack.getItem()
                && !InventoryHelper.shulkerBoxHasItems(selfStack)
                && !InventoryHelper.shulkerBoxHasItems(otherStack)
                && selfStack.hasTag() == otherStack.hasTag()
                && selfStack.getCount() + otherStack.getCount() <= SHULKERBOX_MAX_STACK_AMOUNT) {

            int amount = Math.min(otherStack.getCount(), SHULKERBOX_MAX_STACK_AMOUNT - selfStack.getCount());
            selfStack.grow(amount);
            self.setItem(selfStack);
            this.pickupDelay = Math.max(((EntityItemInterface)other).getPickupDelayCM(), this.pickupDelay);
            this.age = Math.min(((EntityItemInterface)other).getAgeCM(), this.age);
            otherStack.shrink(amount);

            if (otherStack.isEmpty()) {
                other.remove();
            } else {
                other.setItem(otherStack);
            }
            cir.cancel();
        }
    }
}
