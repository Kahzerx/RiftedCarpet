package carpet.mixins;

import carpet.CarpetServer;
import com.google.gson.JsonElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldSavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServer_coreMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;updateTimeLightAndEntities(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.BEFORE, ordinal = 0))
    private void onTick(BooleanSupplier booleanSupplier_1, CallbackInfo ci){
        CarpetServer.tick((MinecraftServer) (Object) this);
    }

    // Dedicated server only
    @Inject(method = "loadAllWorlds", at = @At("HEAD"))
    private void serverLoaded(String saveName, String worldNameIn, long seed, WorldType type, JsonElement generatorOptions, CallbackInfo ci){
        CarpetServer.onServerLoaded((MinecraftServer) (Object) this);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void serverClosed(CallbackInfo ci){
        CarpetServer.onServerClosed((MinecraftServer) (Object) this);
    }

    @Inject(method = "initialWorldChunkLoad", at = @At("RETURN"))
    private void afterSpawnCreated(WorldSavedDataStorage worldSavedDataStorage, CallbackInfo ci){

    }
}
