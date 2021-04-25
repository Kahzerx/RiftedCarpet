package carpet.mixins.rule.tnt.hardcodeTNTangle;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTNTPrimed.class)
public abstract class EntityTNTPrimedMixin extends Entity {
    public EntityTNTPrimedMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/EntityLivingBase;)V",
            at = @At(
                    value = "RETURN"
            )
    )
    private void modifyTNTAngle(World worldIn, double x, double y, double z, EntityLivingBase igniter, CallbackInfo ci) {
        if (CarpetSettings.hardcodeTNTangle != -1.0D) {
            setVelocity(
                    -Math.sin(CarpetSettings.hardcodeTNTangle) * 0.02,
                    0.2,
                    -Math.cos(CarpetSettings.hardcodeTNTangle) * 0.02);
        }
    }
}
