package carpet.mixins.rule.persistentParrots;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {
    @Shadow public PlayerCapabilities abilities;

    @Shadow public abstract boolean isPlayerSleeping();

    @Shadow protected abstract void spawnShoulderEntities();

    @Shadow protected abstract void spawnShoulderEntity(@Nullable NBTTagCompound p_192026_1_);

    @Shadow public abstract NBTTagCompound getLeftShoulderEntity();

    @Shadow protected abstract void setLeftShoulderEntity(NBTTagCompound tag);

    @Shadow public abstract NBTTagCompound getRightShoulderEntity();

    @Shadow protected abstract void setRightShoulderEntity(NBTTagCompound tag);

    protected EntityPlayerMixin(EntityType<?> type, World p_i48577_2_) {
        super(type, p_i48577_2_);
    }

    @Redirect(
            method = "livingTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;spawnShoulderEntities()V"
            )
    )
    private void cancelDropShoulderEntities1(EntityPlayer entityPlayer) {

    }

    @Inject(
            method = "livingTick",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    ordinal = 1,
                    target = "Lnet/minecraft/entity/player/EntityPlayer;playShoulderEntityAmbientSound(Lnet/minecraft/nbt/NBTTagCompound;)V"
            )
    )
    private void onLivingTick(CallbackInfo ci){
        boolean parrots_will_drop = !CarpetSettings.persistentParrots || this.abilities.disableDamage;
        if (!this.world.isRemote && ((parrots_will_drop && this.fallDistance > 0.5F) || this.isInWater() || this.abilities.isFlying || isPlayerSleeping())){
            this.spawnShoulderEntities();
        }
    }

    @Redirect(
            method = "attackEntityFrom",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;spawnShoulderEntities()V"
            )
    )
    private void cancelDropShoulderEntities2(EntityPlayer entityPlayer){

    }

    @Inject(
            method = "attackEntityFrom",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/entity/player/EntityPlayer;spawnShoulderEntities()V"
            )
    )
    private void onDamage(DamageSource damageSource, float float_1, CallbackInfoReturnable<Boolean> cir){
        if (CarpetSettings.persistentParrots && !this.isSneaking()) {
            if(this.rand.nextFloat() < ((float_1) / 15.0)) {
                this.dismount_left();
            }
            if(this.rand.nextFloat() < ((float_1) / 15.0)) {
                this.dismount_right();
            }
        }
    }

    protected void dismount_left() {
        this.spawnShoulderEntity(this.getLeftShoulderEntity());
        this.setLeftShoulderEntity(new NBTTagCompound());
    }

    protected void dismount_right() {
        this.spawnShoulderEntity(this.getRightShoulderEntity());
        this.setRightShoulderEntity(new NBTTagCompound());
    }
}
