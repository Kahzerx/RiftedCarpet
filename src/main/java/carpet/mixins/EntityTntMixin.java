package carpet.mixins;

import carpet.CarpetSettings;
import carpet.fakes.EntityTntInterface;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TNTLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityTNTPrimed.class)
public abstract class EntityTntMixin extends Entity implements EntityTntInterface {
    private TNTLogHelper logHelper = null;
    public EntityTntMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }
    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/EntityLivingBase;)V", at = @At(value = "RETURN"))
    private void initTntLogger(World world, double x, double y, double z, EntityLivingBase igniter, CallbackInfo ci){
        if (CarpetSettings.tntPrimerMomentumRemoved){
            this.motionX = 0.0;
            this.motionY = 0.20000000298023224D;
            this.motionZ = 0.0;
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/EntityLivingBase;)V", at = @At(value = "RETURN"))
    private void modifyTNTAngle(World worldIn, double x, double y, double z, EntityLivingBase igniter, CallbackInfo ci){
        if (CarpetSettings.hardcodeTNTangle != -1.0D) setVelocity(-Math.sin(CarpetSettings.hardcodeTNTangle) * 0.02, 0.2, -Math.cos(CarpetSettings.hardcodeTNTangle) * 0.02);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;)V", at = @At(value = "RETURN"))
    public void initLogger(World worldIn, CallbackInfo ci){
        if (LoggerRegistry.__tnt && !worldIn.isRemote) logHelper = new TNTLogHelper();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onPrimed(CallbackInfo ci){
        if (LoggerRegistry.__tnt && logHelper != null && !logHelper.initialized){
            logHelper.onPrimed(posX, posY, posZ, new Vec3d(motionX, motionY, motionZ));
        }
    }

    @Inject(method = "explode", at = @At("HEAD"))
    public void onExplode(CallbackInfo ci){
        if (LoggerRegistry.__tnt && logHelper != null) logHelper.onExploded(posX, posY, posZ);
    }
}
