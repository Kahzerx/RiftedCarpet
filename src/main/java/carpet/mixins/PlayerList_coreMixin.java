package carpet.mixins;

import carpet.CarpetServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerList_coreMixin {
    @Inject(method = "initializeConnectionToPlayer", at = @At("RETURN"))
    private void onPlayerConnected(NetworkManager netManager, EntityPlayerMP playerIn, CallbackInfo ci){
        CarpetServer.onPlayerLoggedIn(playerIn);
    }
}
