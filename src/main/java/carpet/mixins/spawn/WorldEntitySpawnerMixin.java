package carpet.mixins.spawn;

import carpet.utils.SpawnReporter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(WorldEntitySpawner.class)
public abstract class WorldEntitySpawnerMixin {
    @Shadow @Final private Set<ChunkPos> eligibleChunksForSpawning;
    @Shadow @Final private static int MOB_COUNT_DIV;
    @Shadow protected static BlockPos getRandomChunkPosition(World worldIn, int x, int z) {
        return null;
    }
    @Shadow @Final private static Logger LOGGER;
    @Shadow public static boolean canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.SpawnPlacementType placeType, IWorldReaderBase worldIn, BlockPos pos, @Nullable EntityType<? extends EntityLiving> entityTypeIn) {
        return false;
    }

    /**
     * @author Kahzerx
     * @reason no injection point, not even using bytecode
     */
    @Overwrite
    public int findChunksForSpawning(WorldServer worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate) {
        if (!spawnHostileMobs && !spawnPeacefulMobs) {
            return 0;
        }
        else {
            this.eligibleChunksForSpawning.clear();
            int i = 0;
            for (EntityPlayer entityplayer : worldServerIn.playerEntities) {
                if (!entityplayer.isSpectator()) {
                    int j = MathHelper.floor(entityplayer.posX / 16.0D);
                    int k = MathHelper.floor(entityplayer.posZ / 16.0D);
                    int l = 8;
                    for (int i1 = -8; i1 <= 8; ++i1){
                        for (int j1 = -8; j1 <= 8; ++j1) {
                            boolean flag = i1 == -8 || i1 == 8 || j1 == -8 || j1 == 8;
                            ChunkPos chunkpos = new ChunkPos(i1 + j, j1 + k);
                            if (!this.eligibleChunksForSpawning.contains(chunkpos)) {
                                ++i;
                                if (!flag && worldServerIn.getWorldBorder().contains(chunkpos)) {
                                    PlayerChunkMapEntry playerchunkmapentry = worldServerIn.getPlayerChunkMap().getEntry(chunkpos.x, chunkpos.z);
                                    if (playerchunkmapentry != null && playerchunkmapentry.isSentToPlayers()) {
                                        this.eligibleChunksForSpawning.add(chunkpos);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            int k4 = 0;
            BlockPos blockpos1 = worldServerIn.getSpawnPoint();

            //CM start
            int did = worldServerIn.dimension.getType().getId();
            String level_suffix = (did==0)?"":((did<0?" (N)":" (E)"));
            //CM end

            for (EnumCreatureType enumcreaturetype : EnumCreatureType.values()) {
                //CM start
                String type_code = String.format("%s", enumcreaturetype);
                String group_code = type_code+level_suffix;
                if (SpawnReporter.track_spawns > 0L) {
                    SpawnReporter.overall_spawn_ticks.put(group_code, SpawnReporter.overall_spawn_ticks.get(group_code) + SpawnReporter.spawn_tries.get(type_code));
                }
                //CM end
                if ((!enumcreaturetype.getPeacefulCreature() || spawnPeacefulMobs) && (enumcreaturetype.getPeacefulCreature() || spawnHostileMobs) && (!enumcreaturetype.getAnimal() || spawnOnSetTickRate)) {
                    //CM replaced: //int l4 = enumcreaturetype.getMaxNumberOfCreature() * i / MOB_COUNT_DIV;
                    int l4 = (int)(Math.pow(2.0,(SpawnReporter.mobcap_exponent/4)) * enumcreaturetype.getMaxNumberOfCreature() * i / MOB_COUNT_DIV);

                    int i5 = worldServerIn.countEntities(enumcreaturetype.getBaseClass(), 100000); // CM replaced l4 with 100000 TODO consider calling it now specifically because of the limits

                    SpawnReporter.mobcaps.get(did).put(enumcreaturetype, new Tuple<>(i5, l4));
                    int tries = SpawnReporter.spawn_tries.get(type_code);
                    if (SpawnReporter.track_spawns > 0L) {
                        SpawnReporter.spawn_attempts.put(group_code, SpawnReporter.spawn_attempts.get(group_code) + tries);
                        SpawnReporter.spawn_cap_count.put(group_code, SpawnReporter.spawn_cap_count.get(group_code) + i5);
                    }
                    if (SpawnReporter.mock_spawns) { i5 = 0; } // no mobcaps
                    //CM end

                    if (i5 <= l4) {
                        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
                        /* carpet mod -> extra indentation */
                        for (int trie = 0; trie < tries; trie++) {
                            long local_spawns = 0;
                            /* end */

                            label158:

                            for (ChunkPos chunkpos1 : this.eligibleChunksForSpawning) {
                                BlockPos blockpos = getRandomChunkPosition(worldServerIn, chunkpos1.x, chunkpos1.z);
                                assert blockpos != null;
                                int k1 = blockpos.getX();
                                int l1 = blockpos.getY();
                                int i2 = blockpos.getZ();
                                IBlockState iblockstate = worldServerIn.getBlockState(blockpos);

                                if (!iblockstate.isNormalCube()) {
                                    int j2 = 0;

                                    for (int k2 = 0; k2 < 3; ++k2) {
                                        int l2 = k1;
                                        int i3 = l1;
                                        int j3 = i2;
                                        int k3 = 6;
                                        Biome.SpawnListEntry biome$spawnlistentry = null;
                                        IEntityLivingData ientitylivingdata = null;
                                        int l3 = MathHelper.ceil(Math.random() * 4.0D);
                                        int i4 = 0;

                                        for (int j4 = 0; j4 < l3; ++j4) {
                                            l2 += worldServerIn.rand.nextInt(6) - worldServerIn.rand.nextInt(6);
                                            i3 += worldServerIn.rand.nextInt(1) - worldServerIn.rand.nextInt(1);
                                            j3 += worldServerIn.rand.nextInt(6) - worldServerIn.rand.nextInt(6);
                                            blockpos$mutableblockpos.setPos(l2, i3, j3);
                                            float f = (float)l2 + 0.5F;
                                            float f1 = (float)j3 + 0.5F;
                                            EntityPlayer entityplayer1 = worldServerIn.func_212817_a(f, f1, -1.0D);

                                            if (entityplayer1 != null) {
                                                double d0 = entityplayer1.getDistanceSq((double)f, (double)i3, (double)f1);

                                                if (!(d0 <= 576.0D) && !(blockpos1.distanceSq((double)f, (double)i3, (double)f1) < 576.0D)) {
                                                    if (biome$spawnlistentry == null) {
                                                        biome$spawnlistentry = worldServerIn.getSpawnListEntryForTypeAt(enumcreaturetype, blockpos$mutableblockpos);

                                                        if (biome$spawnlistentry == null) {
                                                            break;
                                                        }

                                                        l3 = biome$spawnlistentry.minGroupCount + worldServerIn.rand.nextInt(1 + biome$spawnlistentry.maxGroupCount - biome$spawnlistentry.minGroupCount);
                                                    }

                                                    if (worldServerIn.canCreatureTypeSpawnHere(enumcreaturetype, biome$spawnlistentry, blockpos$mutableblockpos)) {
                                                        EntitySpawnPlacementRegistry.SpawnPlacementType entityspawnplacementregistry$spawnplacementtype = EntitySpawnPlacementRegistry.getPlacementType(biome$spawnlistentry.entityType);

                                                        if (entityspawnplacementregistry$spawnplacementtype != null && canCreatureTypeSpawnAtLocation(entityspawnplacementregistry$spawnplacementtype, worldServerIn, blockpos$mutableblockpos, biome$spawnlistentry.entityType)) {
                                                            EntityLiving entityliving;

                                                            try {
                                                                entityliving = biome$spawnlistentry.entityType.create(worldServerIn);
                                                            }
                                                            catch (Exception exception) {
                                                                LOGGER.warn("Failed to create mob", (Throwable)exception);
                                                                return k4;
                                                            }

                                                            entityliving.setLocationAndAngles((double)f, (double)i3, (double)f1, worldServerIn.rand.nextFloat() * 360.0F, 0.0F);

                                                            if ((d0 <= 16384.0D || !entityliving.canDespawn()) && entityliving.canSpawn(worldServerIn, false) && entityliving.isNotColliding(worldServerIn)) {
                                                                ientitylivingdata = entityliving.onInitialSpawn(worldServerIn.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata, (NBTTagCompound)null);

                                                                if (entityliving.isNotColliding(worldServerIn)) {
                                                                    ++j2;
                                                                    ++i4;
                                                                    //CM replacing //worldServerIn.spawnEntity(entityliving)
                                                                    ++local_spawns;
                                                                    if (SpawnReporter.track_spawns > 0L) {
                                                                        String species = IRegistry.ENTITY_TYPE.getKey(entityliving.getType()).toString().replaceFirst("minecraft:","");
                                                                        SpawnReporter.registerSpawn(entityliving, type_code, species, blockpos$mutableblockpos);
                                                                    }
                                                                    if (SpawnReporter.mock_spawns) {
                                                                        entityliving.remove();
                                                                    }
                                                                    else {
                                                                        worldServerIn.spawnEntity(entityliving);
                                                                    }
                                                                }
                                                                else {
                                                                    entityliving.remove();
                                                                }

                                                                if (j2 >= entityliving.getMaxSpawnedInChunk()) {
                                                                    continue label158;
                                                                }

                                                                if (entityliving.func_204209_c(i4)) {
                                                                    break;
                                                                }
                                                            }
                                                            k4 += j2;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            /* carpet mod */

                            if (SpawnReporter.track_spawns > 0L) {
                                if (local_spawns > 0) {
                                    SpawnReporter.spawn_ticks_succ.put(group_code, SpawnReporter.spawn_ticks_succ.get(group_code) + 1L);
                                    SpawnReporter.spawn_ticks_spawns.put(group_code, SpawnReporter.spawn_ticks_spawns.get(group_code) + local_spawns);
                                }
                                else {
                                    SpawnReporter.spawn_ticks_fail.put(group_code, SpawnReporter.spawn_ticks_fail.get(group_code) + 1L);
                                }
                            }
                        } //carpet mod <- extra indentation
                    }
                    else {
                        if (SpawnReporter.track_spawns > 0L) {
                            SpawnReporter.spawn_ticks_full.put(group_code, SpawnReporter.spawn_ticks_full.get(group_code) + SpawnReporter.spawn_tries.get(type_code));
                        }
                    }
                    /* end */
                }
            }

            return k4;
        }
    }
}
