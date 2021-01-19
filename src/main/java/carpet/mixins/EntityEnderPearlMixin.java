package carpet.mixins;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.network.NetworkManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityEnderPearl.class)
public abstract class EntityEnderPearlMixin extends EntityThrowable {
    protected EntityEnderPearlMixin(EntityType<?> type, World p_i48540_2_) {
        super(type, p_i48540_2_);
    }
    @Redirect(method = "onImpact", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;isChannelOpen()Z"))
    private boolean isConnectionGood(NetworkManager networkManager){
        return networkManager.isChannelOpen() || getThrower() instanceof EntityPlayerMPFake;
    }
}
