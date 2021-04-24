package carpet.mixins.spawn;

import net.minecraft.util.WeightedRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeightedRandom.Item.class)
public interface WeightedRandomMixin {
    @Accessor("itemWeight") int getWeight();
}
