package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockSilverfish.class)
public abstract class BlockSilverfishMixin extends Block {
    public BlockSilverfishMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "dropBlockAsItemWithChance", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/entity/monster/EntitySilverfish;spawnExplosionParticle()V"))
    private void ondropBlockAsItemWithChance(IBlockState state, World worldIn, BlockPos pos, float chancePerItem, int fortune, CallbackInfo ci){
        if (CarpetSettings.silverFishDropGravel) spawnAsEntity(worldIn, pos, new ItemStack(Blocks.GRAVEL));
    }
}
