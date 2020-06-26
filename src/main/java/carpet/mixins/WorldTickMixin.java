package carpet.mixins;

import carpet.helpers.TickSpeed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEntityReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public abstract class WorldTickMixin implements IEntityReader, IWorld, IWorldReader, AutoCloseable{
    @Shadow public abstract void tickEntity(Entity ent);

    @Redirect(method = "tickEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;tickEntity(Lnet/minecraft/entity/Entity;)V"))
    public void tickEntity(World world, Entity ent){
        if (TickSpeed.process_entities || ent instanceof EntityPlayer) this.tickEntity(ent);
        else return;
    }

    @Redirect(method = "tickEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/math/BlockPos;)Z"))
    public boolean isLoaded(World world, BlockPos pos){
        if (TickSpeed.process_entities) return isBlockLoaded(pos);
        else return false;
    }
}
