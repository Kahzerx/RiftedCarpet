package carpet.utils;

import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MobAI {
    private static Map<EntityType, Set<TrackingType>> aiTrackers = new HashMap<>();

    public static void resetTrackers()
    {
        aiTrackers.clear();
    }

    public enum TrackingType {
        IRON_GOLEM_SPAWNING("iron_golem_spawning", Sets.newHashSet(EntityType.VILLAGER)),
        VILLAGER_BREEDING("breeding", Sets.newHashSet(EntityType.VILLAGER));
        public Set<EntityType> types;
        public String name;
        TrackingType(String name, Set<EntityType> applicableTypes) {
            this.name = name;
            types = applicableTypes;
        }

        public static TrackingType byName(String aspect) {
            for (TrackingType type: values()) if (type.name.equalsIgnoreCase(aspect)) return type;
            return null;
        }
    }
}
