package carpet.mixins;

import carpet.CarpetServer;
import com.google.gson.JsonElement;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {
    @Inject(method = "loadAllWorlds", at = @At("HEAD"))
    private void onSetupServerIntegrated(String saveName, String worldNameIn, long seed, WorldType type, JsonElement generatorOptions, CallbackInfo ci){
        CarpetServer.onServerLoaded((IntegratedServer) (Object) this);
    }
}
