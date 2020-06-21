package carpet.helpers;

import carpet.CarpetSettings;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRotator {
    public static boolean flipBlockWithCactus(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        if (!playerIn.abilities.allowEdit || !CarpetSettings.flippinCactus || !player_holds_cactus_mainhand(playerIn)){
            return false;
        }
        CarpetSettings.impendingFillSkipUpdates = true;
        boolean retval = flip_block(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        CarpetSettings.impendingFillSkipUpdates = false;
        return retval;
    }
    public static boolean flip_block(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        Block block = state.getBlock();
        if ((block instanceof BlockGlazedTerracotta) || (block instanceof BlockRedstoneDiode) || (block instanceof BlockRailBase) || (block instanceof BlockTrapDoor) || (block instanceof BlockLever) || (block instanceof BlockFenceGate)) {
            worldIn.setBlockState(pos, block.rotate(state, Rotation.CLOCKWISE_90), 2 | 1024);
        }
        else if ((block instanceof BlockObserver) || (block instanceof BlockEndRod)) {
            worldIn.setBlockState(pos, state.with(BlockDirectional.FACING, (EnumFacing)state.get(BlockDirectional.FACING).getOpposite()), 2 | 1024);
        }
        else if (block instanceof BlockDispenser) {
            worldIn.setBlockState(pos, state.with(BlockDispenser.FACING, state.get(BlockDispenser.FACING).getOpposite()), 2 | 1024);
        }
        else if (block instanceof BlockPistonBase) {
            if (!(state.get(BlockPistonBase.EXTENDED))) worldIn.setBlockState(pos, state.with(BlockDirectional.FACING, state.get(BlockDirectional.FACING).getOpposite()), 2 | 1024);
        }
        else if (block instanceof BlockSlab) {
            if (!((BlockSlab) block).isFullCube(state)) {
                worldIn.setBlockState(pos, state.with(BlockSlab.TYPE, state.get(BlockSlab.TYPE) == SlabType.TOP ? SlabType.BOTTOM : SlabType.TOP), 2 | 1024);
            }
        }
        else if (block instanceof BlockHopper) {
            if ((EnumFacing)state.get(BlockHopper.FACING) != EnumFacing.DOWN) {
                worldIn.setBlockState(pos, state.with(BlockHopper.FACING, state.get(BlockHopper.FACING).rotateY()), 2 | 1024);
            }
        }
        else if (block instanceof BlockStairs) {
            //LOG.error(String.format("hit with facing: %s, at side %.1fX, X %.1fY, Y %.1fZ",facing, hitX, hitY, hitZ));
            if ((facing == EnumFacing.UP && hitY == 1.0f) || (facing == EnumFacing.DOWN && hitY == 0.0f)) {
                worldIn.setBlockState(pos, state.with(BlockStairs.HALF, state.get(BlockStairs.HALF) == Half.TOP ? Half.BOTTOM : Half.TOP ), 2 | 1024);
            }
            else {
                boolean turn_right;
                if (facing == EnumFacing.NORTH) {
                    turn_right = (hitX <= 0.5);
                }
                else if (facing == EnumFacing.SOUTH) {
                    turn_right = !(hitX <= 0.5);
                }
                else if (facing == EnumFacing.EAST) {
                    turn_right = (hitZ <= 0.5);
                }
                else if (facing == EnumFacing.WEST) {
                    turn_right = !(hitZ <= 0.5);
                }
                else {
                    return false;
                }
                if (turn_right) {
                    worldIn.setBlockState(pos, block.rotate(state, Rotation.COUNTERCLOCKWISE_90), 2 | 1024);
                }
                else {
                    worldIn.setBlockState(pos, block.rotate(state, Rotation.CLOCKWISE_90), 2 | 1024);
                }
            }
        }
        else {
            return false;
        }
        worldIn.markBlockRangeForRenderUpdate(pos, pos);
        return true;
    }
    private static boolean player_holds_cactus_mainhand(EntityPlayer playerIn){
        return (!playerIn.getHeldItemMainhand().isEmpty() && playerIn.getHeldItemMainhand().getItem() instanceof ItemBlock && ((ItemBlock) (playerIn.getHeldItemMainhand().getItem())).getBlock() == Blocks.CACTUS);
    }

    public static boolean flippinEligibility(Entity entity) {
        if (CarpetSettings.flippinCactus && (entity instanceof EntityPlayer)) {
            EntityPlayer player = (EntityPlayer)entity;
            return (!player.getHeldItemOffhand().isEmpty() && player.getHeldItemOffhand().getItem() instanceof ItemBlock && ((ItemBlock) (player.getHeldItemOffhand().getItem())).getBlock() == Blocks.CACTUS);
        }
        return false;
    }
}
