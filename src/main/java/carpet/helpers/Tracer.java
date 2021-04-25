package carpet.helpers;

import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.List;

public class Tracer {
    public static RayTraceResult rayTrace(EntityPlayerMP source, double reach) {

        World world = source.getEntityWorld();
        RayTraceResult result;

        Entity pointedEntity = null;
        result = rayTraceBlocks(source, reach);
        Vec3d eyeVec = source.getEyePosition(1.0F);
        boolean flag = !source.isCreative();
        if (source.isCreative()) reach = 6.0D;
        double extendedReach = reach;

        if (result != null)
        {
            extendedReach = result.hitVec.distanceTo(eyeVec);
            if (world.getBlockState(result.getBlockPos()).getMaterial() == Material.AIR)
                result = null;
        }

        Vec3d lookVec = source.getLook(1.0F);
        Vec3d pointVec = eyeVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
        Vec3d hitVec = null;
        List<Entity> list = world.getEntitiesInAABBexcluding(
                source,
                source.getBoundingBox().expand(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach).grow(1.0D, 1.0D, 1.0D),
                EntitySelectors.NOT_SPECTATING.and((Predicate<Entity>) e -> e != null && e.canBeCollidedWith())
        );
        double d2 = extendedReach;

        for (Entity entity1 : list) {
            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(entity1.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(eyeVec, pointVec);

            if (axisalignedbb.contains(eyeVec)) {
                if (d2 >= 0.0D) {
                    pointedEntity = entity1;
                    hitVec = raytraceresult == null ? eyeVec : raytraceresult.hitVec;
                    d2 = 0.0D;
                }
            } else if (raytraceresult != null) {
                double d3 = eyeVec.distanceTo(raytraceresult.hitVec);

                if (d3 < d2 || d2 == 0.0D) {
                    if (entity1.getLowestRidingEntity() == source.getLowestRidingEntity()) {
                        if (d2 == 0.0D) {
                            pointedEntity = entity1;
                            hitVec = raytraceresult.hitVec;
                        }
                    } else {
                        pointedEntity = entity1;
                        hitVec = raytraceresult.hitVec;
                        d2 = d3;
                    }
                }
            }
        }

        if (pointedEntity != null && flag && eyeVec.distanceTo(hitVec) > 3.0D)
        {
            pointedEntity = null;
            result = new RayTraceResult(RayTraceResult.Type.MISS, hitVec, null, new BlockPos(hitVec));
        }

        if (pointedEntity != null && (d2 < extendedReach || result == null))
        {
            result = new RayTraceResult(pointedEntity, hitVec);
        }

        return result;
    }

    private static RayTraceResult rayTraceBlocks(Entity source, double blockReachDistance) {
        Vec3d eyeVec = source.getEyePosition(1.0F);
        Vec3d lookVec = source.getLook(1.0F);
        Vec3d pointVec = eyeVec.add(lookVec.x * blockReachDistance, lookVec.y * blockReachDistance, lookVec.z * blockReachDistance);
        return source.getEntityWorld().rayTraceBlocks(eyeVec, pointVec, RayTraceFluidMode.NEVER, false, true);
    }
}
