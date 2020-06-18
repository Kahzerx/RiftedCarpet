package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityGuardian.class)
public abstract class EntityGuardianMixin extends EntityMob {
    protected EntityGuardianMixin(EntityType<?> type, World p_i48553_2_) {
        super(type, p_i48553_2_);
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt lightningBolt){
        if (!this.world.isRemote && !this.removed && CarpetSettings.renewableSponges && !((Object)this instanceof EntityElderGuardian)){
            EntityElderGuardian elderGuardian = new EntityElderGuardian(this.world);
            elderGuardian.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            elderGuardian.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(elderGuardian)), (IEntityLivingData)null, (NBTTagCompound)null);
            elderGuardian.setNoAI(this.isAIDisabled());

            if (this.hasCustomName()){
                elderGuardian.setCustomName(this.getCustomName());
                elderGuardian.setCustomNameVisible(this.isCustomNameVisible());
            }
            this.world.spawnEntity(elderGuardian);
            this.remove();
        }
        else {
            super.onStruckByLightning(lightningBolt);
        }
    }
}
