package carpet.mixins.rule.commands.fillLimit;

import carpet.CarpetSettings;
import net.minecraft.command.impl.FillCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FillCommand.class)
public class FillCommandMixin {
    @ModifyConstant(
            method = "doFill",
            constant = @Constant(
                    intValue = 32768
            )
    )
    private static int fillLimit(int original) {
        return CarpetSettings.fillLimit;
    }
}
