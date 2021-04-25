package carpet.mixins.rule.commands.fillUpdates;

import carpet.CarpetSettings;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public class CommandsMixin {
    @Inject(method = "handleCommand", at = @At("HEAD"))
    private void onExecuteBegin(CommandSource source, String command, CallbackInfoReturnable<Integer> cir) {
        if (!CarpetSettings.fillUpdates) CarpetSettings.impendingFillSkipUpdates = true;
    }

    @Inject(method = "handleCommand", at = @At("RETURN"))
    private void onExecuteEnd(CommandSource source, String command, CallbackInfoReturnable<Integer> cir) {
        CarpetSettings.impendingFillSkipUpdates = false;
    }
}
