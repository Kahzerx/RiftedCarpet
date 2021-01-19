package carpet.mixins;

import carpet.fakes.PlayerListHudInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiIngame.class)
public abstract class GuiIngameMixin {
    @Shadow @Final private Minecraft mc;

    @Shadow @Final private GuiPlayerTabOverlay overlayPlayerList;

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z"))
    private boolean onDraw(Minecraft minecraft){
        return this.mc.isIntegratedServerRunning() && !((PlayerListHudInterface)overlayPlayerList).hasFooterOrHeader();
    }
}
