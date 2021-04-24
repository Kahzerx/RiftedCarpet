package carpet.mixins.rule.renewableCoral;

import carpet.fakes.CoralFeatureInterface;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.CoralFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(CoralFeature.class)
public abstract class CoralFeatureMixin implements CoralFeatureInterface {
    @Shadow protected abstract boolean func_204623_a(IWorld p_204623_1_, Random p_204623_2_, BlockPos p_204623_3_, IBlockState p_204623_4_);

    @Override
    public boolean growSpecific(World worldIn, Random random, BlockPos pos, IBlockState blockUnder) {
        return func_204623_a(worldIn, random, pos, blockUnder);
    }
}
