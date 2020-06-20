package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.command.impl.ForceLoadCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ForceLoadCommand.class)
public class ForceLoadCommandMixin {
    @ModifyConstant(method = "func_212719_a", constant = @Constant(longValue = 256L))
    private static long forceloadLimit(long original) {
        return CarpetSettings.forceloadLimit;
    }

    @ModifyConstant(method = "func_212719_a", constant = @Constant(intValue = 256))
    private static int forceloadLimitError(int original) {
        return CarpetSettings.forceloadLimit;
    }
}
