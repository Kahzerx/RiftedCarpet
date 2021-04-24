package carpet.mixins.rotator;

import carpet.fakes.PistonBlockInterface;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockPistonBase.class)
public abstract class BlockPistonBaseMixin implements PistonBlockInterface {
    @Shadow protected abstract boolean shouldBeExtended(World worldIn, BlockPos pos, EnumFacing facing);

    @Override
    public boolean publicShouldExtend(World worldIn, BlockPos pos, EnumFacing facing){
        return shouldBeExtended(worldIn, pos, facing);
    }
}
