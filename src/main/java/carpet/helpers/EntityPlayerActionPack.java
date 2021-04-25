package carpet.helpers;

import carpet.fakes.EntityPlayerMPInterface;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EntityPlayerActionPack {
    private final EntityPlayerMP player;
    private final Map<ActionType, Action> actions = new TreeMap<>();

    private BlockPos currentBlock;
    private int blockHitDelay;
    private boolean isHittingBlock;
    private float curBlockDamageMP;

    private boolean sneaking;
    private boolean sprinting;
    private float forward;
    private float strafing;

    private int itemUseCooldown;

    public EntityPlayerActionPack(EntityPlayerMP playerIn) {
        player = playerIn;
        stopAll();
    }

    public void copyFrom(EntityPlayerActionPack other) {
        actions.putAll(other.actions);
        currentBlock = other.currentBlock;
        blockHitDelay = other.blockHitDelay;
        isHittingBlock = other.isHittingBlock;
        curBlockDamageMP = other.curBlockDamageMP;

        sneaking = other.sneaking;
        sprinting = other.sprinting;
        forward = other.forward;
        strafing = other.strafing;

        itemUseCooldown = other.itemUseCooldown;
    }

    public EntityPlayerActionPack start(ActionType type, Action action) {
        Action previous = actions.remove(type);
        if (previous != null) {
            type.stop(player, previous);
        }
        if (action != null) {
            actions.put(type, action);
            type.start(player, action); // noop
        }
        return this;
    }

    public EntityPlayerActionPack setSneaking(boolean doSneak) {
        sneaking = doSneak;
        player.setSneaking(doSneak);
        if (sprinting && sneaking)
            setSprinting(false);
        return this;
    }

    public EntityPlayerActionPack setSprinting(boolean doSprint) {
        sprinting = doSprint;
        player.setSprinting(doSprint);
        if (sneaking && sprinting)
            setSneaking(false);
        return this;
    }

    public EntityPlayerActionPack setForward(float value) {
        forward = value;
        return this;
    }

    public EntityPlayerActionPack setStrafing(float value) {
        strafing = value;
        return this;
    }

    public EntityPlayerActionPack look(EnumFacing direction) {
        switch (direction) {
            case NORTH: return look(180, 0);
            case SOUTH: return look(0, 0);
            case EAST: return look(-90, 0);
            case WEST: return look(90, 0);
            case UP: return look(player.rotationYaw, -90);
            case DOWN: return look(player.rotationYaw, 90);
        }
        return this;
    }

    public EntityPlayerActionPack look(Vec2f rotation) {
        return look(rotation.x, rotation.y);
    }

    public EntityPlayerActionPack look(float yaw, float pitch) {
        player.rotationYaw = yaw % 360;
        player.rotationPitch = MathHelper.clamp(pitch, -90, 90);
        // maybe player.setPositionAndAngles(player.x, player.y, player.z, yaw, MathHelper.clamp(pitch,-90.0F, 90.0F));
        return this;
    }

    public EntityPlayerActionPack lookAt(Vec3d position) {
        player.lookAt(EntityAnchorArgument.Type.EYES, position);
        return this;
    }

    public EntityPlayerActionPack turn(float yaw, float pitch) {
        return look(player.rotationYaw + yaw, player.rotationPitch + pitch);
    }

    public EntityPlayerActionPack turn(Vec2f rotation) {
        return turn(rotation.x, rotation.y);
    }

    public EntityPlayerActionPack stopMovement() {
        setSneaking(false);
        setSprinting(false);
        forward = 0.0F;
        strafing = 0.0F;
        return this;
    }

    public EntityPlayerActionPack stopAll() {
        for (ActionType type : actions.keySet()) type.stop(player, actions.get(type));
        actions.clear();
        return stopMovement();
    }

    public EntityPlayerActionPack mount() {
        //test what happens
        List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player,player.getBoundingBox().expand(3.0D, 1.0D, 3.0D));
        if (entities.size()==0) {
            return this;
        }

        Entity closest = null;
        double distance = Double.POSITIVE_INFINITY;
        Entity currentVehicle = player.getRidingEntity();
        for (Entity e: entities)
        {
            if (e == player || (currentVehicle == e))
                continue;
            double dd = player.getDistanceSq(e);
            if (dd<distance)
            {
                distance = dd;
                closest = e;
            }
        }
        if (closest == null) return this;
        player.startRiding(closest,true);
        return this;
    }

    public EntityPlayerActionPack dismount() {
        player.stopRiding();
        return this;
    }

    public void onUpdate() {
        Map<ActionType, Boolean> actionAttempts = new HashMap<>();
        actions.entrySet().removeIf((e) -> e.getValue().done);
        for (Map.Entry<ActionType, Action> e : actions.entrySet()) {
            Action action = e.getValue();
            // skipping attack if use was successful
            if (!(actionAttempts.getOrDefault(ActionType.USE, false) && e.getKey() == ActionType.ATTACK)) {
                Boolean actionStatus = action.tick(this, e.getKey());
                if (actionStatus != null) {
                    actionAttempts.put(e.getKey(), actionStatus);
                }
            }
            // optionally retrying use after successful attack and unsuccessful use
            if ( e.getKey() == ActionType.ATTACK
                    && actionAttempts.getOrDefault(ActionType.ATTACK, false)
                    && !actionAttempts.getOrDefault(ActionType.USE, true) ) {
                // according to MinecraftClient.handleInputEvents
                Action using = actions.get(ActionType.USE);
                if (using != null) {  // this is always true - we know use worked, but just in case
                    using.retry(this, ActionType.USE);
                }
            }
        }
        if (forward != 0.0F) {
            player.moveForward = forward * (sneaking ? 0.3F : 1.0F);
        }
        if (strafing != 0.0F) {
            player.moveStrafing = strafing * (sneaking ? 0.3F : 1.0F);
        }
    }

    static RayTraceResult getTarget(EntityPlayerMP player) {
        double reach = player.interactionManager.isCreative() ? 5 : 4.5f;
        return Tracer.rayTrace(player, reach);
    }

    private void dropItemFromSlot(int slot, boolean dropAll) {
        InventoryPlayer inv = player.inventory;
        if (!inv.getStackInSlot(slot).isEmpty()) {
            player.dropItem(inv.decrStackSize(slot,
                    dropAll ? inv.getStackInSlot(slot).getCount() : 1
            ), false, true); // scatter, keep owner
        }
    }

    public void drop(int selectedSlot, boolean dropAll) {
        InventoryPlayer inv = player.inventory;
        if (selectedSlot == -2) {  // all
            for (int i = inv.getSizeInventory(); i >= 0; i--) {
                dropItemFromSlot(i, dropAll);
            }
        } else {  // One slot
            if (selectedSlot == -1) {
                selectedSlot = inv.currentItem;
            }
            dropItemFromSlot(selectedSlot, dropAll);
        }
    }

    public void setSlot(int slot) {
        player.inventory.currentItem = slot-1;
        player.connection.sendPacket(new SPacketHeldItemChange(slot-1));
    }

    public enum ActionType {
        USE (true) {
            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                EntityPlayerActionPack ap = ((EntityPlayerMPInterface) player).getActionPack();
                if (ap.itemUseCooldown > 0) {
                    ap.itemUseCooldown--;
                    return true;
                }
                if (player.isHandActive()) {
                    return true;
                }
                RayTraceResult hit = getTarget(player);
                for (EnumHand hand : EnumHand.values()) {
                    switch (hit.type) {
                        case BLOCK:
                            player.markPlayerActive();
                            WorldServer worldServer = player.getServerWorld();
                            BlockPos blockPos = hit.getBlockPos();
                            EnumFacing enumFacing = hit.sideHit;
                            if (blockPos.getY() < player.server.getBuildLimit() - (enumFacing == EnumFacing.UP ? 1 : 0) && worldServer.isBlockModifiable(player, blockPos)) {
                                EnumActionResult res = player.interactionManager.processRightClickBlock(
                                        player,
                                        worldServer,
                                        player.getHeldItem(hand),
                                        hand,
                                        blockPos,
                                        enumFacing,
                                        (float)hit.hitVec.x,
                                        (float)hit.hitVec.y,
                                        (float)hit.hitVec.z);
                                if (res == EnumActionResult.SUCCESS) {
                                    player.swingArm(hand);
                                    ap.itemUseCooldown = 3;
                                    return true;
                                }
                            }
                            break;
                        case ENTITY:
                            player.markPlayerActive();
                            Entity entity = hit.entity;
                            boolean handWasEmpty = player.getHeldItem(hand).isEmpty();
                            boolean itemFrameEmpty = (entity instanceof EntityItemFrame) && ((EntityItemFrame) entity).getDisplayedItem().isEmpty();
                            Vec3d relativeHitPos = new Vec3d(hit.hitVec.x - entity.posX, hit.hitVec.y - entity.posY, hit.hitVec.z - entity.posZ);
                            if (entity.applyPlayerInteraction(player, relativeHitPos, hand) == EnumActionResult.SUCCESS) {
                                ap.itemUseCooldown = 3;
                                return true;
                            }
                            if (player.interactOn(entity, hand) == EnumActionResult.SUCCESS && !(handWasEmpty && itemFrameEmpty)) {
                                ap.itemUseCooldown = 3;
                                return true;
                            }
                            break;
                    }
                    ItemStack handItem = player.getHeldItem(hand);
                    if (player.interactionManager.processRightClick(player, player.getServerWorld(), handItem, hand) == EnumActionResult.SUCCESS) {
                        ap.itemUseCooldown = 3;
                        return true;
                    }
                }
                return false;
            }

            @Override
            void inactiveTick(EntityPlayerMP player, Action action) {
                EntityPlayerActionPack ap = ((EntityPlayerMPInterface) player).getActionPack();
                ap.itemUseCooldown = 0;
                player.stopActiveHand();
            }
        },
        ATTACK(true) {
            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                RayTraceResult hit = getTarget(player);
                switch (hit.type) {
                    case ENTITY:
                        Entity entity = hit.entity;
                        if (!action.isContinuous) {
                            player.attackTargetEntityWithCurrentItem(entity);
                            player.swingArm(EnumHand.MAIN_HAND);
                        }
                        player.resetCooldown();
                        player.markPlayerActive();
                        return true;
                    case BLOCK:
                        EntityPlayerActionPack ap = ((EntityPlayerMPInterface) player).getActionPack();
                        if (ap.blockHitDelay > 0) {
                            ap.blockHitDelay--;
                            return false;
                        }
                        System.out.println(hit);
                        BlockPos blockPos = hit.getBlockPos();
                        EnumFacing enumFacing = hit.sideHit;
                        if (ap.currentBlock != null && player.world.getBlockState(ap.currentBlock).isAir()) {
                            ap.currentBlock = null;
                            return false;
                        }
                        IBlockState state = player.world.getBlockState(blockPos);
                        boolean blockBroken = false;
                        if (player.interactionManager.getGameType() == GameType.CREATIVE) {
                            player.connection.processPlayerDigging(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, enumFacing.getOpposite()));
                            ap.blockHitDelay = 5;
                            blockBroken = true;
                        } else if (ap.currentBlock == null || ap.currentBlock.equals(blockPos)) {
                            if (ap.currentBlock != null) {
                                player.connection.processPlayerDigging(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, blockPos, enumFacing.getOpposite()));
                            }
                            player.connection.processPlayerDigging(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, enumFacing.getOpposite()));
                            boolean notAir = !state.isAir();
                            if (notAir && ap.curBlockDamageMP == 0) {
                                state.getBlock().onBlockClicked(state, player.world, blockPos, player);
                            }

                            if (notAir && state.getPlayerRelativeBlockHardness(player, player.world, blockPos) >= 1) {
                                ap.currentBlock = null;
                                blockBroken = true;
                            } else {
                                ap.currentBlock = blockPos;
                                ap.curBlockDamageMP = 0;
                            }
                        } else {
                            ap.curBlockDamageMP += state.getPlayerRelativeBlockHardness(player, player.world, blockPos);
                            if (ap.curBlockDamageMP >= 1) {
                                player.connection.processPlayerDigging(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, enumFacing.getOpposite()));
                                ap.currentBlock = null;
                                ap.blockHitDelay = 5;
                                blockBroken = true;
                            }
                            player.world.sendBlockBreakProgress(player.getEntityId(), ap.currentBlock, (int)(ap.curBlockDamageMP * 10.0F) - 1);
                        }
                        player.markPlayerActive();
                        player.swingArm(EnumHand.MAIN_HAND);
                        return blockBroken;
                }
                return false;
            }

            @Override
            void inactiveTick(EntityPlayerMP player, Action action) {
                EntityPlayerActionPack ap = ((EntityPlayerMPInterface) player).getActionPack();
                if (ap.currentBlock == null) return;
                player.world.sendBlockBreakProgress(-1, ap.currentBlock, -1);
                player.connection.processPlayerDigging(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, ap.currentBlock, EnumFacing.DOWN));
                ap.currentBlock = null;
            }
        },
        JUMP(true) {
            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                if (action.limit == 1) {
                    if (player.onGround) {
                        player.jump();
                    }
                } else {
                    player.setJumping(true);
                }
                return false;
            }

            @Override
            void inactiveTick(EntityPlayerMP player, Action action) {
                player.setJumping(false);
            }
        },
        DROP_ITEM(true) {
            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                player.markPlayerActive();
                player.dropItem(false);
                return false;
            }
        },
        DROP_STACK(true) {
            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                player.markPlayerActive();
                player.dropItem(true);
                return false;
            }
        },
        SWAP_HANDS(true) {
            @Override
            boolean execute(EntityPlayerMP player, Action action) {
                player.markPlayerActive();
                ItemStack itemStack = player.getHeldItem(EnumHand.OFF_HAND);
                player.setHeldItem(EnumHand.OFF_HAND, player.getHeldItem(EnumHand.MAIN_HAND));
                player.setHeldItem(EnumHand.MAIN_HAND, itemStack);
                return false;
            }
        };

        public final boolean preventSpectator;
        ActionType(boolean preventSpectator) {
            this.preventSpectator = preventSpectator;
        }

        void start(EntityPlayerMP player, Action action) {}
        abstract boolean execute(EntityPlayerMP player, Action action);
        void inactiveTick(EntityPlayerMP player, Action action) {}
        void stop(EntityPlayerMP player, Action action) {
            inactiveTick(player, action);
        }
    }

    public static class Action {
        public boolean done = false;
        public final int limit;
        public final int interval;
        public final int offset;
        private int count;
        private int next;
        private final boolean isContinuous;
        private Action(int limit, int interval, int offset, boolean continuous) {
            this.limit = limit;
            this.interval = interval;
            this.offset = offset;
            this.next = interval + offset;
            this.isContinuous = continuous;
        }

        public static Action once() {
            return new Action(1, 1, 0, false);
        }

        public static Action continuous() {
            return new Action(-1, 1, 0, true);
        }

        public static Action interval(int interval) {
            return new Action(-1, interval, 0, false);
        }

        public static Action interval(int interval, int offset) {
            return new Action(-1, interval, offset, false);
        }

        Boolean tick(EntityPlayerActionPack actionPack, ActionType type) {
            next--;
            Boolean cancel = null;
            if (next <= 0) {
                if (interval == 1 && !isContinuous) {
                    // need to allow entity to tick, otherwise won't have effect (bow)
                    // actions are 20 tps, so need to clear status mid tick, allowing entities process it till next time
                    if (!type.preventSpectator || !actionPack.player.isSpectator()) {
                        type.inactiveTick(actionPack.player, this);
                    }
                }

                if (!type.preventSpectator || !actionPack.player.isSpectator()) {
                    cancel = type.execute(actionPack.player, this);
                }
                count++;
                if (count == limit) {
                    type.stop(actionPack.player, null);
                    done = true;
                    return cancel;
                }
                next = interval;
            } else {
                if (!type.preventSpectator || !actionPack.player.isSpectator()) {
                    type.inactiveTick(actionPack.player, this);
                }
            }
            return cancel;
        }

        void retry(EntityPlayerActionPack actionPack, ActionType type) {
            //assuming action run but was unsuccesful that tick, but opportunity emerged to retry it, lets retry it.
            if (!type.preventSpectator || !actionPack.player.isSpectator()) {
                type.execute(actionPack.player, this);
            }
            count++;
            if (count == limit) {
                type.stop(actionPack.player, null);
                done = true;
            }
        }
    }

}
