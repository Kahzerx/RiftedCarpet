package carpet.fakes;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public interface CPacketCustomPayloadInterface {
    ResourceLocation getPacketChannel();
    PacketBuffer getPacketData();
}
