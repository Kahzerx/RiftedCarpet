package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.command.impl.CloneCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CloneCommand.class)
public abstract class CloneCommandMixin {
    @ModifyConstant(method = "doClone", constant = @Constant(intValue = 32768))
    private static int fillLimit(int original) {
        return CarpetSettings.fillLimit;
    }
}
