package carpet.mixins.rule.tnt.tntPrimerMomentumRemoved;

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
    private void initTntLogger(World world, double x, double y, double z, EntityLivingBase igniter, CallbackInfo ci){
        if (CarpetSettings.tntPrimerMomentumRemoved){
            this.motionX = 0.0;
            this.motionY = 0.20000000298023224D;
            this.motionZ = 0.0;
        }
    }
}
