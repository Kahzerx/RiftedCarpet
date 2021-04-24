package carpet.mixins.protocol;

import carpet.fakes.CPacketCustomPayloadInterface;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CPacketCustomPayload.class)
public class CPacketCustomPayloadMixin implements CPacketCustomPayloadInterface {
    @Shadow private ResourceLocation channel;

    @Shadow private PacketBuffer data;

    @Override
    public ResourceLocation getPacketChannel() {
        return channel;
    }

    @Override
    public PacketBuffer getPacketData() {
        return new PacketBuffer(this.data.copy());
    }
}
