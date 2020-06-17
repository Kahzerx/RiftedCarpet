package carpet.utils;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

public class WoolTool {
    private static final HashMap<MaterialColor,EnumDyeColor> Material2Dye = new HashMap<>();
    static
    {
        for (EnumDyeColor color: EnumDyeColor.values())
        {
            Material2Dye.put(color.getMapColor(),color);
        }
    }
    public static EnumDyeColor getWoolColorAtPosition(World worldIn, BlockPos pos)
    {
        IBlockState state = worldIn.getBlockState(pos);
        if (state.getMaterial() != Material.CLOTH || !state.isFullCube())
            return null;
        return Material2Dye.get(state.getMapColor(worldIn, pos));
    }
}
