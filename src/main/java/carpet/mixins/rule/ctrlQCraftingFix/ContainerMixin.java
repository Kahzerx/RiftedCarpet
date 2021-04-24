package carpet.mixins.rule.ctrlQCraftingFix;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Container.class)
public abstract class ContainerMixin {
    @Shadow public List<Slot> inventorySlots;

    @Shadow public abstract ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player);

    @Shadow public abstract void detectAndSendChanges();

    @Inject(
            method = "slotClick",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private void onThrowClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer entityPlayer, CallbackInfoReturnable<ItemStack> cir) {
        if (clickTypeIn == ClickType.THROW
                && CarpetSettings.ctrlQCraftingFix
                && entityPlayer.inventory.getItemStack().isEmpty()
                && slotId >= 0) {

            ItemStack itemStack_1 = ItemStack.EMPTY;
            Slot slot_4 = inventorySlots.get(slotId);
            if (slot_4 != null
                    && slot_4.getHasStack()
                    && slot_4.canTakeStack(entityPlayer)) {

                if (slotId == 0 && dragType == 1) {
                    Item craftedItem = slot_4.getStack().getItem();
                    while (slot_4.getHasStack()
                            && slot_4.getStack().getItem() == craftedItem) {

                        this.slotClick(slotId, 0, ClickType.THROW, entityPlayer);
                    }
                    this.detectAndSendChanges();
                    cir.setReturnValue(itemStack_1);
                    cir.cancel();
                }
            }
        }
    }
}
