package carpet.mixins;

import carpet.CarpetServer;
import carpet.network.CarpetClient;
import carpet.settings.SettingsManager;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {
    @Inject(method = "sendChatMessage", at = @At("HEAD"))
    private void processMessage(String str, CallbackInfo ci) {
        if (str.startsWith("/call ")) {
            String command = str.substring(6);
            CarpetClient.sendClientCommand(command);
        }
        if (CarpetServer.minecraft_server == null && !CarpetClient.isCarpet()) {
            EntityPlayerSP playerSource = (EntityPlayerSP)(Object) this;
            CarpetServer.settingsManager.inspectClientsideCommand(playerSource.getCommandSource(), str);
            CarpetServer.extensions.forEach(e -> {
                SettingsManager sm = e.customSettingsManager();
                if (sm != null) sm.inspectClientsideCommand(playerSource.getCommandSource(), str);
            });
        }
    }
}
