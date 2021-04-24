package carpet.mixins.tickprofiler;

import carpet.helpers.TickSpeed;
import carpet.utils.CarpetProfiler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEntityReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(World.class)
public abstract class WorldMixin implements IEntityReader, IWorld, IWorldReader, AutoCloseable{
    String world_name;

    CarpetProfiler.ProfilerToken tok_entities;
    CarpetProfiler.ProfilerToken tok;

    Boolean started = false;

    @Shadow public abstract void tickEntity(Entity ent);

    @Shadow @Final public Dimension dimension;

    @Shadow @Final public boolean isRemote;

    @Shadow @Final private WorldBorder worldBorder;

    @Redirect(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;tickEntity(Lnet/minecraft/entity/Entity;)V"
            )
    )
    public void tickEntity(World world, Entity ent) {
        if (TickSpeed.process_entities || ent instanceof EntityPlayer) {
            this.tickEntity(ent);
        }
    }

    @Redirect(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    public boolean contains(WorldBorder worldBorder, BlockPos pos){
        if (TickSpeed.process_entities) return this.worldBorder.contains(pos);
        else return false;
    }

    @Inject(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
                    ordinal = 1
            )
    )
    public void startTokEntities(CallbackInfo ci){
        int did = this.dimension.getType().getId();
        this.world_name = (did==0)?"Overworld":((did<0?"The Nether":"The End"));
        this.tok_entities = CarpetProfiler.start_section_concurrent(world_name, "Entities", this.isRemote);
    }

    @Inject(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getRidingEntity()Lnet/minecraft/entity/Entity;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void startTok(CallbackInfo ci, int i1, Entity entity2) {
        this.tok = CarpetProfiler.start_entity_section(world_name, entity2);
    }

    @Inject(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;endSection()V",
                    ordinal = 1
            )
    )
    public void endTok(CallbackInfo ci) {
        CarpetProfiler.end_current_entity_section(this.tok);
    }

    @Inject(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                    ordinal = 2
            )
    )
    public void endTokEntities(CallbackInfo ci) {
        CarpetProfiler.end_current_section_concurrent(this.tok_entities);
        this.tok_entities = CarpetProfiler.start_section_concurrent(this.world_name, "Tile Entities", this.isRemote);
    }

    @Inject(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/TileEntity;isRemoved()Z",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void startTok2(CallbackInfo ci, Iterator iterator, TileEntity tileentity){
        if (this.started) CarpetProfiler.end_current_entity_section(this.tok);
        this.tok = CarpetProfiler.start_tileentity_section(this.world_name, tileentity);
        this.started = true;
    }

    @Inject(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                    ordinal = 3
            )
    )
    public void endTok2(CallbackInfo ci){
        if (this.started) CarpetProfiler.end_current_entity_section(this.tok);
        this.started = false;
    }

    @Inject(
            method = "tickEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;endSection()V",
                    ordinal = 3
            )
    )
    public void endTokEntities2(CallbackInfo ci) {
        CarpetProfiler.end_current_section_concurrent(this.tok_entities);
    }
}
