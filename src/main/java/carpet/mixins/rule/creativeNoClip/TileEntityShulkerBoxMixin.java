package carpet.mixins.rule.creativeNoClip;

import carpet.CarpetSettings;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityShulkerBox.class)
public class TileEntityShulkerBoxMixin {
    @Redirect(
            method = "moveCollidedEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getPushReaction()Lnet/minecraft/block/material/EnumPushReaction;"
            )
    )
    private EnumPushReaction ignorePush(Entity entity) {
        if (entity instanceof EntityPlayer && (CarpetSettings.creativeNoClip && ((EntityPlayer)entity).isCreative()) && ((EntityPlayer)entity).abilities.isFlying) {
            return EnumPushReaction.IGNORE;
        }
        return entity.getPushReaction();
    }
}
