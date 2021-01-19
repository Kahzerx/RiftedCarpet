package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseEntityCollisionMixin extends Entity{
    @Shadow protected abstract void collideWithEntity(Entity entityIn);

    public EntityLivingBaseEntityCollisionMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Inject(method = "collideWithNearbyEntities", at = @At("HEAD"), cancellable = true)
    private void tickPushingReplacement(CallbackInfo ci){
        List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox(), EntitySelectors.pushableBy(this));
        if (!list.isEmpty()){
            int i = this.world.getGameRules().getInt("maxEntityCramming");
            if (i > 0 && list.size() > i - 1 && this.rand.nextInt(4) == 0){
                int j = 0;

                for (int k = 0; k < list.size(); ++k) {
                    if (!((Entity)list.get(k)).isPassenger()) {
                        ++j;
                    }
                }

                if (j > i - 1) {
                    this.attackEntityFrom(DamageSource.CRAMMING, 6.0F);
                }
            }
            int limit = list.size();
            if (CarpetSettings.maxEntityCollisions > 0) limit = Math.min(limit, CarpetSettings.maxEntityCollisions);
            for (int j = 0; j < limit; ++j) {
                Entity entity = (Entity) list.get(j);
                this.collideWithEntity(entity);
            }
        }
        ci.cancel();
    }
}
