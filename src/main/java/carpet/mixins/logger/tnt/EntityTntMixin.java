package carpet.mixins.logger.tnt;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TNTLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTNTPrimed.class)
public abstract class EntityTntMixin extends Entity {
    private TNTLogHelper logHelper = null;
    public EntityTntMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;)V",
            at = @At(
                    value = "RETURN"
            )
    )
    public void initLogger(World worldIn, CallbackInfo ci){
        if (LoggerRegistry.__tnt && !worldIn.isRemote) {
            logHelper = new TNTLogHelper();
        }
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "HEAD"
            )
    )
    public void onPrimed(CallbackInfo ci){
        if (LoggerRegistry.__tnt && logHelper != null && !logHelper.initialized) {
            logHelper.onPrimed(posX, posY, posZ, new Vec3d(motionX, motionY, motionZ));
        }
    }

    @Inject(
            method = "explode",
            at = @At(
                    value = "HEAD"
            )
    )
    public void onExplode(CallbackInfo ci){
        if (LoggerRegistry.__tnt && logHelper != null) {
            logHelper.onExploded(posX, posY, posZ);
        }
    }
}
