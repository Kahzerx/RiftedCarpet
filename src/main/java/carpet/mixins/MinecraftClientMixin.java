package carpet.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Inject(method = "run", at = @At(value = "HEAD"))
    private void inInit(CallbackInfo ci){
        //CarpetServer.onGameStarted();
        System.out.println("o/");
    }
}
