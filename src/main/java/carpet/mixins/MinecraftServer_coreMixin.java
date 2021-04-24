package carpet.mixins;

import carpet.CarpetServer;
import carpet.utils.CarpetProfiler;
import com.google.gson.JsonElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServer_coreMixin {
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;updateTimeLightAndEntities(Ljava/util/function/BooleanSupplier;)V",
                    shift = At.Shift.BEFORE,
                    ordinal = 0))
    private void onTick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        CarpetServer.tick((MinecraftServer) (Object) this);
    }

    @Inject(method = "loadAllWorlds", at = @At("HEAD"))
    private void serverLoaded(String saveName, String worldNameIn, long seed, WorldType type, JsonElement generatorOptions, CallbackInfo ci) {
        CarpetServer.onServerLoaded((IntegratedServer) (Object) this);
    }

    @Inject(method = "loadAllWorlds", at = @At("HEAD"))
    private void serverLoadedWorlds(String saveName, String worldNameIn, long seed, WorldType type, JsonElement generatorOptions, CallbackInfo ci) {
        CarpetServer.onServerLoadedWorlds((IntegratedServer) (Object) this);
    }

    @Inject(method = "stopServer", at = @At("RETURN"))
    private void serverClosed(CallbackInfo ci) {
        CarpetServer.onServerClosed((MinecraftServer)(Object) this);
    }

    @Inject(method = "reload", at = @At("HEAD"))
    private void onReload(CallbackInfo ci) {
        CarpetServer.onReload((MinecraftServer)(Object) this);
    }
}
