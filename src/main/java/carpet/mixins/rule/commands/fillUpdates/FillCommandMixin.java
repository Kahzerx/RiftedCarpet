package carpet.mixins.rule.commands.fillUpdates;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.command.impl.FillCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FillCommand.class)
public class FillCommandMixin {
    @Redirect(
            method = "doFill",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/arguments/BlockStateInput;place(Lnet/minecraft/world/WorldServer;Lnet/minecraft/util/math/BlockPos;I)Z"
            )
    )
    private static boolean state(BlockStateInput blockStateInput, WorldServer worldIn, BlockPos pos, int flags) {
        return blockStateInput.place(worldIn, pos, flags | (CarpetSettings.fillUpdates ? 0 : 1024));
    }

    @Redirect(
            method = "doFill",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldServer;notifyNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"
            )
    )
    private static void conditionalUpdating(WorldServer worldServer, BlockPos pos, Block blockIn) {
        if (CarpetSettings.fillUpdates) {
            worldServer.notifyNeighbors(pos, blockIn);
        }
    }
}
