package carpet.mixins;

import com.google.common.collect.Lists;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.EndCityConfig;
import net.minecraft.world.gen.feature.structure.EndCityStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(EndCityStructure.class)
public abstract class EndCityStructureMixin extends Structure<EndCityConfig> {
    private static final List<Biome.SpawnListEntry> spawnList = Lists.newArrayList(new Biome.SpawnListEntry(EntityType.SHULKER, 10, 4, 4));
    @Override
    public List<Biome.SpawnListEntry> getSpawnList(){
        return spawnList;
    }
}
