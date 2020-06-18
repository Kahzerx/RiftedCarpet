package carpet.utils;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public static void carpetPlacedAction(EnumDyeColor color, EntityPlayer placer, BlockPos pos, WorldServer worldIn) {
        if (!CarpetSettings.carpets)
        {
            return;
        }
        switch (color){
            case GREEN:
                if (CarpetSettings.hopperCounters)
                {
                    EnumDyeColor under = getWoolColorAtPosition(worldIn, pos.down());
                    if (under == null) return;
                    HopperCounter counter = HopperCounter.getCounter(under.toString());
                    if (counter != null)
                        Messenger.send(placer, counter.format(worldIn.getServer(), false, false));
                }
                break;
            case RED:
                if (CarpetSettings.hopperCounters)
                {
                    EnumDyeColor under = getWoolColorAtPosition(worldIn, pos.down());
                    if (under == null) return;
                    HopperCounter counter = HopperCounter.getCounter(under.toString());
                    if (counter == null) return;
                    counter.reset(placer.getServer());
                    List<ITextComponent> res = new ArrayList<>();
                    res.add(Messenger.s(String.format("%s counter reset",under.toString())));
                    Messenger.send(placer, res);
                }
                break;
        }
    }
}