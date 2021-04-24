package carpet.mixins.rule.creativeNoClip;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {
    protected EntityPlayerMixin(EntityType<?> type, World p_i48577_2_) {
        super(type, p_i48577_2_);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"
            )
    )
    private boolean canClipTroughWorld(EntityPlayer entityPlayer){
        return entityPlayer.isSpectator()
                || (CarpetSettings.creativeNoClip
                && entityPlayer.isCreative()
                && entityPlayer.abilities.isFlying);
    }

    @Redirect(
            method = "livingTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"
            )
    )
    private boolean collidesWithEntities(EntityPlayer entityPlayer){
        return entityPlayer.isSpectator()
                || (CarpetSettings.creativeNoClip
                && entityPlayer.isCreative()
                && entityPlayer.abilities.isFlying);
    }
}
