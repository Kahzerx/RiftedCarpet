package carpet.mixins;

import carpet.fakes.EntityPlayerMPInterface;
import carpet.helpers.EntityPlayerActionPack;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin implements EntityPlayerMPInterface {
    public EntityPlayerActionPack actionPack;
    public EntityPlayerActionPack getActionPack(){
        return actionPack;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onEntityPlayerMPContructor(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn, CallbackInfo ci){
        this.actionPack = new EntityPlayerActionPack((EntityPlayerMP)(Object)this);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci){
        actionPack.onUpdate();
    }
}
