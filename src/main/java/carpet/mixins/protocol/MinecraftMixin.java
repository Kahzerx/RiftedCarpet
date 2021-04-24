package carpet.mixins.protocol;

import carpet.network.CarpetClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/client/gui/GuiScreen;)V", at = @At(value = "HEAD"))
    private void onCloseGame(WorldClient worldIn, GuiScreen loadingScreen, CallbackInfo ci) {
        if (worldIn == null) {
            CarpetClient.disconnect();
        }
    }
}
