package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.command.impl.CloneCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CloneCommand.class)
public abstract class CloneCommandMixin {
    @ModifyConstant(method = "doClone", constant = @Constant(intValue = 32768))
    private static int fillLimit(int original) {
        return CarpetSettings.fillLimit;
    }

    @Redirect(method = "doClone", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;notifyNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"))
    private static void conditionalUpdating(WorldServer worldServer, BlockPos pos, Block blockIn){
        if (CarpetSettings.fillUpdates) worldServer.notifyNeighbors(pos, blockIn);
    }
}
