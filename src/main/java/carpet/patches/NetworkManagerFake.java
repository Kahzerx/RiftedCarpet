package carpet.patches;

import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;

public class NetworkManagerFake extends NetworkManager {
    public NetworkManagerFake(EnumPacketDirection packetDirection) {
        super(packetDirection);
    }

    @Override
    public void disableAutoRead() { }

    @Override
    public void handleDisconnection() { }
}
