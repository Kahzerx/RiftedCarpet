package carpet.fakes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public interface CoralFeatureInterface {
    boolean growSpecific(World worldIn, Random random, BlockPos pos, IBlockState blockUnder);
}
