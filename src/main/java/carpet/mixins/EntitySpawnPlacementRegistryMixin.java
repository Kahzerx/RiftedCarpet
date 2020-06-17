package carpet.mixins;

import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.world.gen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntitySpawnPlacementRegistry.class)
public class EntitySpawnPlacementRegistryMixin {
    @Shadow private static void register(EntityType<?> entityTypeIn, EntitySpawnPlacementRegistry.SpawnPlacementType placementType, Heightmap.Type heightMapType){
    }

    static {
        //thonk
        register(EntityType.SHULKER, EntitySpawnPlacementRegistry.SpawnPlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES);
    }
}
