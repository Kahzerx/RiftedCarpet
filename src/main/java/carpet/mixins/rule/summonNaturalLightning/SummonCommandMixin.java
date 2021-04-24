package carpet.mixins.rule.summonNaturalLightning;

import carpet.CarpetSettings;
import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.SummonCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SummonCommand.class)
public class SummonCommandMixin {
    @Inject(
            method = "summonEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldServer;addWeatherEffect(Lnet/minecraft/entity/Entity;)Z",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private static void addRiders(CommandSource source, ResourceLocation type, Vec3d pos, NBTTagCompound nbt, boolean randomizeProperties, CallbackInfoReturnable<Integer> cir, NBTTagCompound nbttagcompound, Entity entity1){
        if (CarpetSettings.summonNaturalLightning && !source.getWorld().isRemote) {
            WorldServer world = source.getWorld();
            BlockPos at = new BlockPos(pos);
            DifficultyInstance difficulty = source.getWorld().getDifficultyForLocation(at);
            boolean bl = world.getGameRules().getBoolean("doMobSpawning") && world.rand.nextDouble() < (double)difficulty.getAdditionalDifficulty() * 0.01D;
            if (bl) {
                EntitySkeletonHorse entityskeletonhorse = new EntitySkeletonHorse(source.getWorld());
                entityskeletonhorse.setTrap(true);
                entityskeletonhorse.setGrowingAge(0);
                entityskeletonhorse.setPosition(pos.x, pos.y, pos.z);
                source.getWorld().spawnEntity(entityskeletonhorse);
                world.spawnEntity(entityskeletonhorse);
            }
            world.addWeatherEffect(entity1);
            source.sendFeedback(new TextComponentTranslation("commands.summon.success", entity1.getDisplayName()), true);
            cir.setReturnValue(1);
            cir.cancel();
        }
    }
}
