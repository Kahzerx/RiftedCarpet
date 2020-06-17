package carpet.mixins;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import carpet.utils.WoolTool;
import net.minecraft.block.BlockHopper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityHopper.class)
public abstract class TileEntityHopperMixin extends TileEntityLockableLoot {
    @Shadow public abstract double getXPos();

    @Shadow public abstract double getYPos();

    @Shadow public abstract double getZPos();

    @Shadow public abstract int getSizeInventory();

    @Shadow public abstract void setInventorySlotContents(int index, ItemStack stack);

    protected TileEntityHopperMixin(TileEntityType<?> typeIn) {
        super(typeIn);
    }

    @Inject(method = "transferItemsOut", at = @At("HEAD"), cancellable = true)
    private void onInsert(CallbackInfoReturnable<Boolean> cir){
        if (CarpetSettings.hopperCounters){
            EnumDyeColor wool_color = WoolTool.getWoolColorAtPosition(getWorld(), new BlockPos(getXPos(), getYPos(), getZPos()).offset(this.getBlockState().get(BlockHopper.FACING)));
            if (wool_color != null) {
                for (int i = 0; i < this.getSizeInventory(); i++) {
                    if (!this.getStackInSlot(i).isEmpty()){
                        ItemStack itemstack = this.getStackInSlot(i);
                        HopperCounter.COUNTERS.get(wool_color).add(this.getWorld().getServer(), itemstack);
                        this.setInventorySlotContents(i, ItemStack.EMPTY);
                    }
                }
                cir.setReturnValue(true);
            }
        }
    }
}
