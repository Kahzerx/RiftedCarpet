package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.state.BlockPistonStructureHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = BlockPistonStructureHelper.class, priority = 420)
public class BlockPistonStructureHelperPushLimitMixin {
    @ModifyConstant(method = "addBlockLine", constant = @Constant(intValue = 12), expect = 3)
    private int pushLimit(int original){
        return CarpetSettings.pushLimit;
    }
}
