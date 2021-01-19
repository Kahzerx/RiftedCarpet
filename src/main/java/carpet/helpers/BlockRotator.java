package carpet.helpers;

import carpet.CarpetSettings;
import carpet.fakes.PistonBlockInterface;
import net.minecraft.block.*;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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

    public static class CactusDispenserBehaviour extends BehaviorDefaultDispenseItem implements IBehaviorDispenseItem {
        @Override
        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack){
            if (CarpetSettings.rotatorBlock) return BlockRotator.dispenserRotate(source, stack);
            else return super.dispenseStack(source, stack);
        }
    }

    public static ItemStack dispenserRotate(IBlockSource source, ItemStack stack){
        EnumFacing sourceFace = source.getBlockState().get(BlockDispenser.FACING);
        World world = source.getWorld();
        BlockPos pos = source.getBlockPos().offset(sourceFace);
        IBlockState iBlockState = world.getBlockState(pos);
        Block block = iBlockState.getBlock();

        if (block instanceof BlockDirectional || block instanceof BlockDispenser){
            EnumFacing face = iBlockState.get(BlockDirectional.FACING);
            if (block instanceof BlockPistonBase && iBlockState.get(BlockPistonBase.EXTENDED) || (((PistonBlockInterface)block).publicShouldExtend(world, pos, face) && (new BlockPistonStructureHelper(world, pos, face, true)).canMove())) return stack;
            EnumFacing rotated_face = rotateClockwise(face, sourceFace.getAxis());
            if (sourceFace.getIndex() % 2 == 0 || rotated_face == face) rotated_face = rotated_face.getOpposite();
            world.setBlockState(pos, iBlockState.with(BlockDirectional.FACING, rotated_face), 3);
        }

        else if (block instanceof BlockHorizontal){
            if (block instanceof BlockBed) return stack;
            EnumFacing face = iBlockState.get(BlockHorizontal.HORIZONTAL_FACING);
            face = rotateClockwise(face, EnumFacing.Axis.Y);
            if (sourceFace == EnumFacing.DOWN) face.getOpposite();
            world.setBlockState(pos, iBlockState.with(BlockHorizontal.HORIZONTAL_FACING, face), 3);
        }

        else if (block == Blocks.HOPPER){
            EnumFacing face = iBlockState.get(BlockHopper.FACING);
            if (face != EnumFacing.DOWN){
                face = rotateClockwise(face, EnumFacing.Axis.Y);
                world.setBlockState(pos, iBlockState.with(BlockHopper.FACING, face), 3);
            }
        }
        world.neighborChanged(pos, block, source.getBlockPos());
        return stack;
    }

    private static EnumFacing rotateClockwise(EnumFacing direction, EnumFacing.Axis direction$Axis_1) {
        switch(direction$Axis_1) {
            case X:
                if (direction != EnumFacing.WEST && direction != EnumFacing.EAST) {
                    return rotateXClockwise(direction);
                }

                return direction;
            case Y:
                if (direction != EnumFacing.UP && direction != EnumFacing.DOWN) {
                    return rotateYClockwise(direction);
                }

                return direction;
            case Z:
                if (direction != EnumFacing.NORTH && direction != EnumFacing.SOUTH) {
                    return rotateZClockwise(direction);
                }

                return direction;
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + direction$Axis_1);
        }
    }

    private static EnumFacing rotateYClockwise(EnumFacing dir) {
        switch(dir) {
            case NORTH:
                return EnumFacing.EAST;
            case EAST:
                return EnumFacing.SOUTH;
            case SOUTH:
                return EnumFacing.WEST;
            case WEST:
                return EnumFacing.NORTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + dir);
        }
    }

    private static EnumFacing rotateXClockwise(EnumFacing dir) {
        switch(dir) {
            case NORTH:
                return EnumFacing.DOWN;
            case EAST:
            case WEST:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + dir);
            case SOUTH:
                return EnumFacing.UP;
            case UP:
                return EnumFacing.NORTH;
            case DOWN:
                return EnumFacing.SOUTH;
        }
    }

    private static EnumFacing rotateZClockwise(EnumFacing dir) {
        switch(dir) {
            case EAST:
                return EnumFacing.DOWN;
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + dir);
            case WEST:
                return EnumFacing.UP;
            case UP:
                return EnumFacing.EAST;
            case DOWN:
                return EnumFacing.WEST;
        }
    }
}
