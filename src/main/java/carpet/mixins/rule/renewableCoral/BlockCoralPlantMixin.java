package carpet.mixins.rule.renewableCoral;

import carpet.CarpetSettings;
import carpet.fakes.CoralFeatureInterface;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCoralPlant;
import net.minecraft.block.BlockCoralPlantBase;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.CoralClawFeature;
import net.minecraft.world.gen.feature.CoralFeature;
import net.minecraft.world.gen.feature.CoralMushroomFeature;
import net.minecraft.world.gen.feature.CoralTreeFeature;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(BlockCoralPlant.class)
public abstract class BlockCoralPlantMixin implements IGrowable {

    public boolean canGrow(IBlockReader blockReader, BlockPos pos, IBlockState state, boolean isClient){
        return CarpetSettings.renewableCoral && state.get(BlockCoralPlantBase.WATERLOGGED) && blockReader.getFluidState(pos.up()).isTagged(FluidTags.WATER);
    }

    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state){
        return (double)worldIn.rand.nextFloat() < 0.15D;
    }

    public void grow(World worldIn, Random random, BlockPos pos, IBlockState blockUnder){
        CoralFeature coral;
        int variant = random.nextInt(3);
        if (variant == 0) coral = new CoralClawFeature();
        else if (variant == 1) coral = new CoralTreeFeature();
        else coral = new CoralMushroomFeature();
        MaterialColor color = blockUnder.getMapColor(worldIn, pos);
        IBlockState proper_block = blockUnder;
        for (Block block: BlockTags.CORAL_BLOCKS.getAllElements()){
            proper_block = block.getDefaultState();
            if (proper_block.getMapColor(worldIn,pos) == color) break;
        }
        worldIn.setBlockState(pos, Blocks.WATER.getDefaultState(), 4);
        if (!((CoralFeatureInterface)coral).growSpecific(worldIn, random, pos, proper_block)) worldIn.setBlockState(pos, blockUnder, 3);
        else {
            if (worldIn.rand.nextInt(10)==0){
                BlockPos randomPos = pos.add(worldIn.rand.nextInt(16) - 8,worldIn.rand.nextInt(8),worldIn.rand.nextInt(16) - 8);
                if (BlockTags.CORAL_BLOCKS.contains(worldIn.getBlockState(randomPos).getBlock())) worldIn.setBlockState(pos, Blocks.WET_SPONGE.getDefaultState(), 3);
            }
        }
    }
}
