package carpet.mixins;

import carpet.helpers.TickSpeed;
import carpet.utils.CarpetProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerTickMixin {
    @Shadow private boolean serverRunning;

    @Shadow private long serverTime;

    @Shadow private long timeOfLastWarning;

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract void tick(BooleanSupplier p_71217_1_);

    @Shadow protected abstract boolean isAheadOfTime();

    @Shadow private boolean serverIsRunning;
    @Shadow @Final private Map<DimensionType, WorldServer> worlds;
    private float carpetMsptAccum = 0.0f;

    @Redirect(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;serverRunning:Z"))
    private boolean cancelRunLoop(MinecraftServer server)
    {
        return false;
    }

    @Inject(method = "run", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/MinecraftServer;applyServerIconToResponse(Lnet/minecraft/network/ServerStatusResponse;)V"))
    private void modifiedRunLoop(CallbackInfo ci) throws InterruptedException {
        while (this.serverRunning) {
            long msThisTick = 0L;
            long long_1 = 0L;
            if (TickSpeed.time_warp_start_time != 0 && TickSpeed.continueWarp()) {
                //making sure server won't flop after the warp or if the warp is interrupted
                this.serverTime = this.timeOfLastWarning = Util.milliTime();
                carpetMsptAccum = TickSpeed.mspt;
            }
            else {
                if (Math.abs(carpetMsptAccum - TickSpeed.mspt) > 1.0f){
                    carpetMsptAccum = TickSpeed.mspt;
                }
                msThisTick = (long)carpetMsptAccum; // regular tick
                carpetMsptAccum += TickSpeed.mspt - msThisTick;
                long_1 = Util.milliTime() - this.serverTime;
            }
            //end tick deciding
            //smoothed out delay to include mcpt component. With 50L gives defaults.
            if (long_1 > /*2000L*/1000L + 20 * TickSpeed.mspt && this.serverTime - this.timeOfLastWarning >= /*15000L*/10000L + 100 * TickSpeed.mspt){
                long long_2 = (long)(long_1 / TickSpeed.mspt);//50L;
                LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", long_1, long_2);
                this.serverTime += (long)(long_2 * TickSpeed.mspt);//50L;
                this.timeOfLastWarning = this.serverTime;
            }
            this.serverTime += msThisTick;//50L;
            this.tick(this::isAheadOfTime);
            while (this.isAheadOfTime())
            {
                Thread.sleep(1L);
            }
            this.serverIsRunning = true;
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 0))
    public void startProfiler(BooleanSupplier p_71217_1_, CallbackInfo ci){
        if (CarpetProfiler.tick_health_requested != 0L) CarpetProfiler.start_tick_profiling();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 1))
    public void startSave(BooleanSupplier p_71217_1_, CallbackInfo ci){
        CarpetProfiler.start_section(null, "Autosave");
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 0))
    public void endSave(BooleanSupplier p_71217_1_, CallbackInfo ci){
        CarpetProfiler.end_current_section();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 3, shift = At.Shift.AFTER))
    public void endProfiler(BooleanSupplier p_71217_1_, CallbackInfo ci){
        if (CarpetProfiler.tick_health_requested != 0L) CarpetProfiler.end_tick_profiling((MinecraftServer)(Object)this);
    }

    @Inject(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", ordinal = 2))
    public void startNetwork(BooleanSupplier p_71190_1_, CallbackInfo ci){
        CarpetProfiler.start_section(null, "Network");
    }

    @Inject(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", ordinal = 4))
    public void endNetwork(BooleanSupplier p_71190_1_, CallbackInfo ci){
        CarpetProfiler.end_current_section();
    }
}
