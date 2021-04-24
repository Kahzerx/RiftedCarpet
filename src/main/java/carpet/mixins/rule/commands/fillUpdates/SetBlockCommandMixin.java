package carpet.mixins.rule.commands.fillUpdates;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.command.impl.SetBlockCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SetBlockCommand.class)
public class SetBlockCommandMixin {
    @Redirect(
            method = "setBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/arguments/BlockStateInput;place(Lnet/minecraft/world/WorldServer;Lnet/minecraft/util/math/BlockPos;I)Z"
            )
    )
    private static boolean onPlace(BlockStateInput blockStateInput, WorldServer worldIn, BlockPos pos, int flags) {
        return blockStateInput.place(worldIn, pos, flags | (CarpetSettings.fillUpdates ? 0 : 1024));
    }

    @Redirect(
            method = "setBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldServer;notifyNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"
            )
    )
    private static void setBlock(WorldServer worldServer, BlockPos pos, Block blockIn){
        if (CarpetSettings.fillUpdates) {
            worldServer.notifyNeighbors(pos, blockIn);
        }
    }
}
