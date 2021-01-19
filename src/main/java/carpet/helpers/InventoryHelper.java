package carpet.helpers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class InventoryHelper {
    public static final int TAG_LIST        = 9;
    public static final int TAG_COMPOUND    = 10;
    public static boolean cleanUpShulkerBoxTag(ItemStack stack) {
        boolean changed = false;
        NBTTagCompound tag = stack.getTag();

        if (tag == null || !tag.contains("BlockEntityTag", TAG_COMPOUND))
            return false;

        NBTTagCompound bet = tag.getCompound("BlockEntityTag");
        if (bet.contains("Items", TAG_LIST) && bet.getList("Items", TAG_COMPOUND).isEmpty()) {
            bet.remove("Items");
            changed = true;
        }

        if (bet.isEmpty()) {
            tag.remove("BlockEntityTag");
            changed = true;
        }
        if (tag.isEmpty()) {
            stack.setTag(null);
            changed = true;
        }
        return changed;
    }

    public static boolean shulkerBoxHasItems(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTag();

        if (tag == null || !tag.contains("BlockEntityTag", TAG_COMPOUND))
            return false;

        NBTTagCompound bet = tag.getCompound("BlockEntityTag");
        return bet.contains("Items", TAG_LIST) && !bet.getList("Items", TAG_COMPOUND).isEmpty();
    }
}
