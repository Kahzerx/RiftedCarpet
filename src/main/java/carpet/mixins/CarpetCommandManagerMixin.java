package carpet.mixins;

import carpet.CarpetServer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CarpetCommandManagerMixin {
    @Shadow @Final private CommandDispatcher<CommandSource> dispatcher;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onRegister(boolean boolean_1, CallbackInfo ci) {
        CarpetServer.registerCarpetCommands(this.dispatcher);
    }
}
