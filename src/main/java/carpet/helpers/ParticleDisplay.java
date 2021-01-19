package carpet.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.ParticleArgument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Map;

public class ParticleDisplay {

    private static Map<String, IParticleData> particleCache = new HashMap<>();

    public static void drawParticleLine(EntityPlayerMP player, Vec3d from, Vec3d to, String main, String accent, int count, double spread)
    {
        IParticleData accentParticle = getEffect(accent);
        IParticleData mainParticle = getEffect(main);

        if (accentParticle != null) ((WorldServer)player.world).spawnParticle(
                player,
                accentParticle,
                true,
                to.x, to.y, to.z, count,
                spread, spread, spread, 0.0);

        double lineLengthSq = from.squareDistanceTo(to);
        if (lineLengthSq == 0) return;

        Vec3d incvec = to.subtract(from).normalize();//    multiply(50/sqrt(lineLengthSq));
        int pcount = 0;
        for (Vec3d delta = new Vec3d(0.0,0.0,0.0);
             delta.lengthSquared()<lineLengthSq;
             delta = delta.add(incvec.scale(player.world.rand.nextFloat())))
        {
            ((WorldServer)player.world).spawnParticle(
                    player,
                    mainParticle,
                    true,
                    delta.x+from.x, delta.y+from.y, delta.z+from.z, 1,
                    0.0, 0.0, 0.0, 0.0);
        }
    }

    public static IParticleData getEffect(String name)
    {
        if (name == null) return null;
        IParticleData res = particleCache.get(name);
        if (res != null) return res;
        particleCache.put(name, parseParticle(name));
        return particleCache.get(name);
    }

    private static IParticleData parseParticle(String name)
    {
        try
        {
            return ParticleArgument.parseParticle(new StringReader(name));
        }
        catch (CommandSyntaxException e)
        {
            throw new RuntimeException("No such particle: "+name);
        }
    }
}
