package carpet.mixins;

import carpet.CarpetSettings;
import carpet.fakes.XPOrbInterface;
import carpet.helpers.XPCombine;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityXPOrb.class)
public abstract class EntityXPOrbMixin implements XPOrbInterface {
    @Shadow private int xpValue;
    public int combineDelay = 50;
    public int getCombineDelay() { return combineDelay; }
    public void setCombineDelay(int what) { combineDelay = what; }
    public void setAmount(int amount) {
        this.xpValue = amount;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityXPOrb;move(Lnet/minecraft/entity/MoverType;DDD)V", shift = At.Shift.AFTER))
    void checkCombineAtTick(CallbackInfo ci){
        if(CarpetSettings.combineXPOrbs){
            if (getCombineDelay() > 0){
                setCombineDelay(getCombineDelay() - 1);
            }
            if (getCombineDelay() == 0){
                XPCombine.searchForOtherXPNearby((EntityXPOrb) (Object) this);
            }
        }
    }

    @Inject(method = "onCollideWithPlayer", at = @At("HEAD"))
    void removeDelay(EntityPlayer entityPlayer, CallbackInfo ci){
        if (CarpetSettings.xpNoCooldown) entityPlayer.xpCooldown = 0;
    }
}
