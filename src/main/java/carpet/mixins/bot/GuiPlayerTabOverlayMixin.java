package carpet.mixins.bot;

import carpet.fakes.PlayerListHudInterface;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class GuiPlayerTabOverlayMixin implements PlayerListHudInterface {

    @Shadow private ITextComponent footer;
    @Shadow private ITextComponent header;

    public boolean hasFooterOrHeader() {
        return footer != null || header != null;
    }
}
