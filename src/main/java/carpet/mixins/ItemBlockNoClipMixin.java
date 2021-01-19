package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlock.class)
public class ItemBlockNoClipMixin {
    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    public void canPlace(BlockItemUseContext ctx, IBlockState state, CallbackInfoReturnable<Boolean> cir){
        EntityPlayer player = ctx.getPlayer();
        if (CarpetSettings.creativeNoClip && player != null && player.isCreative() && player.abilities.isFlying) {
            VoxelShape voxelShape = state.getCollisionShape(ctx.getWorld(), ctx.getPos());
            cir.setReturnValue(voxelShape.isEmpty() || ctx.getWorld().checkNoEntityCollision(player, voxelShape.withOffset(ctx.getPos().getX(), ctx.getPos().getY(), ctx.getPos().getZ())));
        }
    }
}
