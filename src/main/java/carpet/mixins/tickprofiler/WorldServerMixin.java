package carpet.mixins.tickprofiler;

import carpet.helpers.TickSpeed;
import carpet.utils.CarpetProfiler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.BooleanSupplier;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {
    @Shadow private boolean insideTick;

    @Shadow public abstract boolean areAllPlayersAsleep();

    @Shadow protected abstract void wakeAllPlayers();

    @Shadow @Final private WorldEntitySpawner entitySpawner;

    @Shadow public abstract ChunkProviderServer getChunkProvider();

    @Shadow public abstract void tickPending();

    @Shadow @Final private PlayerChunkMap playerChunkMap;

    @Shadow @Final protected VillageSiege villageSiege;

    @Shadow @Final private Teleporter worldTeleporter;

    @Shadow protected abstract void sendQueuedBlockEvents();

    @Shadow protected abstract void playerCheckLight();

    @Shadow protected abstract BlockPos adjustPosToNearbyEntity(BlockPos pos);

    protected WorldServerMixin(ISaveHandler p_i49813_1_, @Nullable WorldSavedDataStorage p_i49813_2_, WorldInfo p_i49813_3_, Dimension p_i49813_4_, Profiler p_i49813_5_, boolean p_i49813_6_) {
        super(p_i49813_1_, p_i49813_2_, p_i49813_3_, p_i49813_4_, p_i49813_5_, p_i49813_6_);
    }

    /**
     * @author Kahzerx
     */
    @Overwrite
    public void tick(BooleanSupplier p_72835_1_){
        this.insideTick = true;
        super.tick(p_72835_1_);

        if (this.getWorldInfo().isHardcore() && this.getDifficulty() != EnumDifficulty.HARD)
        {
            this.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
        }

        this.chunkProvider.getChunkGenerator().getBiomeProvider().tick();

        if (this.areAllPlayersAsleep())
        {
            if (this.getGameRules().getBoolean("doDaylightCycle"))
            {
                long i = this.worldInfo.getDayTime() + 24000L;
                this.worldInfo.setDayTime(i - i % 24000L);
            }

            this.wakeAllPlayers();
        }

        int did = this.dimension.getType().getId();
        String world_name = (did==0)?"Overworld":((did<0?"The Nether":"The End"));

        if (TickSpeed.process_entities){
            this.profiler.startSection("spawner");
            CarpetProfiler.start_section(world_name, "Spawning");

            if (this.getGameRules().getBoolean("doMobSpawning") && this.worldInfo.getGenerator() != WorldType.DEBUG_ALL_BLOCK_STATES)
            {
                this.entitySpawner.findChunksForSpawning((WorldServer)(Object)this, this.spawnHostileMobs, this.spawnPeacefulMobs, this.worldInfo.getGameTime() % 400L == 0L);
                this.getChunkProvider().spawnMobs(this, this.spawnHostileMobs, this.spawnPeacefulMobs);
            }
            CarpetProfiler.end_current_section();
        }

        this.profiler.endStartSection("chunkSource");
        this.chunkProvider.tick(p_72835_1_);
        int j = this.calculateSkylightSubtracted(1.0F);

        if (j != this.getSkylightSubtracted())
        {
            this.setSkylightSubtracted(j);
        }
        if (TickSpeed.process_entities){

            this.worldInfo.setGameTime(this.worldInfo.getGameTime() + 1L);

            if (this.getGameRules().getBoolean("doDaylightCycle"))
            {
                this.worldInfo.setDayTime(this.worldInfo.getDayTime() + 1L);
            }

            this.profiler.endStartSection("tickPending");
            CarpetProfiler.start_section(world_name, "Blocks");
            this.tickPending();
            CarpetProfiler.end_current_section();
        }
        this.profiler.endStartSection("tickBlocks");
        CarpetProfiler.start_section(world_name, "Blocks");
        this.tickBlocks();
        CarpetProfiler.end_current_section();
        this.profiler.endStartSection("chunkMap");
        this.playerChunkMap.tick();
        if (TickSpeed.process_entities){
            this.profiler.endStartSection("village");
            CarpetProfiler.start_section(world_name, "Villages");
            this.villageCollection.tick();
            this.villageSiege.tick();
            CarpetProfiler.end_current_section();
            this.profiler.endStartSection("portalForcer");
            this.worldTeleporter.tick(this.getGameTime());
        }
        this.profiler.endSection();
        this.sendQueuedBlockEvents();
        this.insideTick = false;
    }

    /**
     * @author Kahzerx
     */
    @Overwrite
    protected void tickBlocks(){
        this.playerCheckLight();

        if (this.worldInfo.getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES)
        {
            Iterator<Chunk> iterator1 = this.playerChunkMap.getChunkIterator();

            while (iterator1.hasNext())
            {
                ((Chunk)iterator1.next()).tick(false);
            }
        }
        else
        {
            int i = this.getGameRules().getInt("randomTickSpeed");
            boolean flag = this.isRaining();
            boolean flag1 = this.isThundering();
            this.profiler.startSection("pollingChunks");

            for (Iterator<Chunk> iterator = this.playerChunkMap.getChunkIterator(); iterator.hasNext(); this.profiler.endSection())
            {
                this.profiler.startSection("getChunk");
                Chunk chunk = iterator.next();
                int j = chunk.x * 16;
                int k = chunk.z * 16;
                this.profiler.endStartSection("checkNextLight");
                chunk.enqueueRelightChecks();
                this.profiler.endStartSection("tickChunk");
                chunk.tick(false);
                if (!TickSpeed.process_entities){
                    this.profiler.endSection();
                    continue;
                }
                this.profiler.endStartSection("thunder");

                if (flag && flag1 && this.rand.nextInt(100000) == 0)
                {
                    this.updateLCG = this.updateLCG * 3 + 1013904223;
                    int l = this.updateLCG >> 2;
                    BlockPos blockpos = this.adjustPosToNearbyEntity(new BlockPos(j + (l & 15), 0, k + (l >> 8 & 15)));

                    if (this.isRainingAt(blockpos))
                    {
                        DifficultyInstance difficultyinstance = this.getDifficultyForLocation(blockpos);
                        boolean flag2 = this.getGameRules().getBoolean("doMobSpawning") && this.rand.nextDouble() < (double)difficultyinstance.getAdditionalDifficulty() * 0.01D;

                        if (flag2)
                        {
                            EntitySkeletonHorse entityskeletonhorse = new EntitySkeletonHorse(this);
                            entityskeletonhorse.setTrap(true);
                            entityskeletonhorse.setGrowingAge(0);
                            entityskeletonhorse.setPosition((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                            this.spawnEntity(entityskeletonhorse);
                        }

                        this.addWeatherEffect(new EntityLightningBolt(this, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, flag2));
                    }
                }

                this.profiler.endStartSection("iceandsnow");

                if (this.rand.nextInt(16) == 0)
                {
                    this.updateLCG = this.updateLCG * 3 + 1013904223;
                    int i2 = this.updateLCG >> 2;
                    BlockPos blockpos1 = this.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(j + (i2 & 15), 0, k + (i2 >> 8 & 15)));
                    BlockPos blockpos2 = blockpos1.down();
                    Biome biome = this.getBiome(blockpos1);

                    if (biome.doesWaterFreeze(this, blockpos2))
                    {
                        this.setBlockState(blockpos2, Blocks.ICE.getDefaultState());
                    }

                    if (flag && biome.doesSnowGenerate(this, blockpos1))
                    {
                        this.setBlockState(blockpos1, Blocks.SNOW.getDefaultState());
                    }

                    if (flag && this.getBiome(blockpos2).getPrecipitation() == Biome.RainType.RAIN)
                    {
                        this.getBlockState(blockpos2).getBlock().fillWithRain(this, blockpos2);
                    }
                }

                this.profiler.endStartSection("tickBlocks");

                if (i > 0)
                {
                    for (ChunkSection chunksection : chunk.getSections())
                    {
                        if (chunksection != Chunk.EMPTY_SECTION && chunksection.needsRandomTickAny())
                        {
                            for (int j2 = 0; j2 < i; ++j2)
                            {
                                this.updateLCG = this.updateLCG * 3 + 1013904223;
                                int i1 = this.updateLCG >> 2;
                                int j1 = i1 & 15;
                                int k1 = i1 >> 8 & 15;
                                int l1 = i1 >> 16 & 15;
                                IBlockState iblockstate = chunksection.get(j1, l1, k1);
                                IFluidState ifluidstate = chunksection.getFluidState(j1, l1, k1);
                                this.profiler.startSection("randomTick");

                                if (iblockstate.needsRandomTick())
                                {
                                    iblockstate.randomTick(this, new BlockPos(j1 + j, l1 + chunksection.getYLocation(), k1 + k), this.rand);
                                }

                                if (ifluidstate.getTickRandomly())
                                {
                                    ifluidstate.randomTick(this, new BlockPos(j1 + j, l1 + chunksection.getYLocation(), k1 + k), this.rand);
                                }

                                this.profiler.endSection();
                            }
                        }
                    }
                }
            }

            this.profiler.endSection();
        }
    }
}
