package carpet.mixins.rule.creativeNoClip;

import carpet.CarpetSettings;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityPiston;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityPiston.class)
public class TileEntityPistonMixin {
    @Redirect(
            method = "moveCollidedEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getPushReaction()Lnet/minecraft/block/material/EnumPushReaction;"
            )
    )
    private EnumPushReaction ignoreOffset(Entity entity) {
        if (entity instanceof EntityPlayer && (CarpetSettings.creativeNoClip && ((EntityPlayer)entity).isCreative()) && ((EntityPlayer)entity).abilities.isFlying) {
            return EnumPushReaction.IGNORE;
        }
        return entity.getPushReaction();
    }
}
