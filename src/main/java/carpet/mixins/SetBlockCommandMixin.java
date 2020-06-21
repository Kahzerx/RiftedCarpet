package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.command.impl.SetBlockCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SetBlockCommand.class)
public class SetBlockCommandMixin {
    @Redirect(method = "setBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;notifyNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"))
    private static void setBlock(WorldServer worldServer, BlockPos pos, Block blockIn){
        if (CarpetSettings.fillUpdates) worldServer.notifyNeighbors(pos, blockIn);
    }
}
