package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityPiston;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityPiston.class)
public class TileEntityPistonNoClipMixin {
    @Redirect(method = "moveCollidedEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    private void ignoreMovement(Entity entity, MoverType moverType, double x, double y, double z){
        if (entity instanceof EntityPlayer && (CarpetSettings.creativeNoClip && ((EntityPlayer)entity).isCreative()) && ((EntityPlayer)entity).abilities.isFlying) return;
        entity.move(MoverType.PISTON, x, y, z);
    }
}
