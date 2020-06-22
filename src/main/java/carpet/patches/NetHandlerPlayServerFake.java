package carpet.patches;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

public class NetHandlerPlayServerFake extends NetHandlerPlayServer {
    public NetHandlerPlayServerFake(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {
        super(server, networkManagerIn, playerIn);
    }

    @Override
    public void sendPacket(final Packet<?> packetIn) { }

    @Override
    public void disconnect(ITextComponent message) { }
}
