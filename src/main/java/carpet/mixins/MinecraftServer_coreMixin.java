package carpet.mixins;

import carpet.CarpetServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServer_coreMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;updateTimeLightAndEntities(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.BEFORE, ordinal = 0))
    private void onTick(BooleanSupplier booleanSupplier, CallbackInfo ci){
        CarpetServer.tick((MinecraftServer) (Object) this);
    }

}
