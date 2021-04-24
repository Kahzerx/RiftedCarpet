package carpet.mixins.rule.shulkerSpawningInEndCities;

import carpet.CarpetSettings;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.AbstractChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.EndGenSettings;
import net.minecraft.world.gen.feature.Feature;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(ChunkGeneratorEnd.class)
public abstract class ChunkGeneratorEndMixin extends AbstractChunkGenerator<EndGenSettings> {
    public ChunkGeneratorEndMixin(IWorld worldIn, BiomeProvider biomeProviderIn) {
        super(worldIn, biomeProviderIn);
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (CarpetSettings.shulkerSpawningInEndCities && EnumCreatureType.MONSTER == creatureType) {
            if (Feature.END_CITY.isPositionInsideStructure(this.world, pos)) {
                return Feature.END_CITY.getSpawnList();
            }
        }
        return this.world.getBiome(pos).getSpawns(creatureType);
    }
}
