package carpet.mixins.rule.onePlayerSleeping;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {
    @Shadow private boolean allPlayersSleeping;

    protected WorldServerMixin(ISaveHandler p_i49813_1_, @Nullable WorldSavedDataStorage p_i49813_2_, WorldInfo p_i49813_3_, Dimension p_i49813_4_, Profiler p_i49813_5_, boolean p_i49813_6_) {
        super(p_i49813_1_, p_i49813_2_, p_i49813_3_, p_i49813_4_, p_i49813_5_, p_i49813_6_);
    }

    @Inject(
            method = "updateAllPlayersSleepingFlag",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void updateOnePlayerSleeping(CallbackInfo ci) {
        if (CarpetSettings.onePlayerSleeping) {
            allPlayersSleeping = false;
            for (EntityPlayer p : playerEntities){
                if (p.isPlayerSleeping() && !p.isSpectator()) {
                    allPlayersSleeping = true;
                    ci.cancel();
                    return;
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "areAllPlayersAsleep", at = @At("HEAD"), cancellable = true)
    private void checkAllPlayers(CallbackInfoReturnable<Boolean> cir){
        if (allPlayersSleeping && !this.isRemote && CarpetSettings.onePlayerSleeping) {
            for (EntityPlayer entityplayer : playerEntities) {
                if (!entityplayer.isSpectator() && entityplayer.isPlayerFullyAsleep()) {
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }
            }
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
