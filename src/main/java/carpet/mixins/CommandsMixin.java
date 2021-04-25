package carpet.mixins;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.command.Commands.class)
public class CommandsMixin {
    @Shadow @Final private CommandDispatcher<CommandSource> dispatcher;
    @Inject(
            method = "<init>",
            at = @At(
                    value = "RETURN"
            )
    )
    private void onRegister(boolean boolean_1, CallbackInfo ci) {
        CarpetServer.registerCarpetCommands(this.dispatcher);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = "handleCommand",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;isDebugEnabled()Z"
            ),
    require = 0
    )
    private boolean doesOutputCommandStackTrace(Logger logger) {
        if (CarpetSettings.superSecretSetting) {
            return true;
        }
        return logger.isDebugEnabled();
    }
}
