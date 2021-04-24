package carpet.mixins.carpets;

import carpet.utils.WoolTool;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockCarpet.class)
public abstract class BlockCarpetMixin extends Block {
    public BlockCarpetMixin(Properties properties) {
        super(properties);
    }
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        IBlockState state = super.getStateForPlacement(context);
        if (context.getPlayer() != null && !context.getWorld().isRemote) {
            WoolTool.carpetPlacedAction(((BlockCarpet)(Object)this).getColor(), context.getPlayer(), context.getPos(), (WorldServer)context.getWorld());
        }
        return state;
    }
}
