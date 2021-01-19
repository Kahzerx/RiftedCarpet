package carpet.helpers;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BlockSaplingHelper {
    public static boolean hasWater(IWorld worldIn, BlockPos pos){
        for (BlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-4, -4, -4), pos.add(4, 1, 4))){
            if (worldIn.getBlockState(blockpos$mutableblockpos).getMaterial() == Material.WATER) return true;
        }
        return false;
    }
}
