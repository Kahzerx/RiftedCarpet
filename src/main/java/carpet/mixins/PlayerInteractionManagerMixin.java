package carpet.mixins;

import carpet.helpers.BlockRotator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerInteractionManager.class)
public class PlayerInteractionManagerMixin {
    @Redirect(method = "processRightClickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;onBlockActivated(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Z"))
    private boolean activateWithOptionalCactus(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
        boolean flipped = BlockRotator.flipBlockWithCactus(worldIn, pos, state, player, hand, side, hitX, hitY, hitZ);
        if (flipped) return true;
        return state.onBlockActivated(worldIn, pos, player, hand, side, hitX, hitY, hitZ);
    }
}
