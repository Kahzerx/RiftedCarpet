package carpet.script;

import carpet.CarpetServer;
import carpet.script.value.BlockValue;
import carpet.script.value.EntityValue;
import carpet.script.value.ListValue;
import carpet.script.value.NumericValue;
import carpet.script.value.StringValue;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CarpetEventServer
{
    public static class Callback
    {
        public String host;
        public String udf;

        public Callback(String host, String udf)
        {
            this.host = host;
            this.udf = udf;
        }

        @Override
        public String toString()
        {
            return udf+((host==null)?"":"(from "+host+")");
        }
    }

    public static class ScheduledCall extends Callback
    {
        public List<LazyValue> args;
        public CommandSource context_source;
        public BlockPos context_origin;
        public long dueTime;

        public ScheduledCall(CarpetContext context, String udf, List<LazyValue> args, long dueTime)
        {
            super(context.host.getName(), udf);
            this.args = args;
            this.context_source = context.s;
            this.context_origin = context.origin;
            this.dueTime = dueTime;
        }

        public void execute()
        {
            CarpetServer.scriptServer.runas(context_origin, context_source, host, udf, args);
        }
    }

    public static class CallbackList
    {

        public List<Callback> callList;
        public int reqArgs;

        public CallbackList(int reqArgs)
        {
            this.callList = new ArrayList<>();
            this.reqArgs = reqArgs;
        }

        public void call(Supplier<List<LazyValue>> argumentSupplier, Supplier<CommandSource> cmdSourceSupplier)
        {
            if (callList.size() > 0)
            {
                List<LazyValue> argv = argumentSupplier.get(); // empty for onTickDone
                CommandSource source = cmdSourceSupplier.get();
                assert argv.size() == reqArgs;
                callList.removeIf(call -> !CarpetServer.scriptServer.runas(source, call.host, call.udf, argv)); // this actually does the calls
            }
        }
        public boolean addEventCall(String hostName, String funName)
        {
            ScriptHost host = CarpetServer.scriptServer.getHostByName(hostName);
            if (host == null)
            {
                // impossible call to add
                return false;
            }
            UserDefinedFunction udf = host.globalFunctions.get(funName);
            if (udf == null || udf.getArguments().size() != reqArgs)
            {
                // call won't match arguments
                return false;
            }
            //all clear
            //remove duplicates
            removeEventCall(hostName, funName);
            callList.add(new Callback(hostName, funName));
            return true;
        }
        public void removeEventCall(String hostName, String callName)
        {
            callList.removeIf((c)->  c.udf.equalsIgnoreCase(callName) && ( hostName == null || c.host.equalsIgnoreCase(hostName) ) );
        }
    }

    public Map<String, CallbackList> eventHandlers = new HashMap<>();

    public List<ScheduledCall> scheduledCalls = new LinkedList<>();

    public void tick()
    {
        Iterator<ScheduledCall> eventIterator = scheduledCalls.iterator();
        List<ScheduledCall> currentCalls = new ArrayList<>();
        while(eventIterator.hasNext())
        {
            ScheduledCall call = eventIterator.next();
            call.dueTime--;
            if (call.dueTime <= 0)
            {
                currentCalls.add(call);
                eventIterator.remove();
            }
        }
        for (ScheduledCall call: currentCalls)
        {
            call.execute();
        }

    }
    public void scheduleCall(CarpetContext context, String function, List<LazyValue> args, long due)
    {
        scheduledCalls.add(new ScheduledCall(context, function, args, due));
    }


    public CarpetEventServer()
    {
        eventHandlers.put("tick",new CallbackList(0));
        eventHandlers.put("tick_nether",new CallbackList(0));
        eventHandlers.put("tick_ender",new CallbackList(0));
        eventHandlers.put("player_jumps",new CallbackList(1));
        eventHandlers.put("player_deploys_elytra",new CallbackList(1));
        eventHandlers.put("player_wakes_up",new CallbackList(1));
        eventHandlers.put("player_rides",new CallbackList(5));
        eventHandlers.put("player_uses_item",new CallbackList(3));
        eventHandlers.put("player_clicks_block",new CallbackList(3));
        eventHandlers.put("player_right_clicks_block",new CallbackList(6));
        eventHandlers.put("player_breaks_block",new CallbackList(2));
        eventHandlers.put("player_interacts_with_entity",new CallbackList(3));
        eventHandlers.put("player_attacks_entity",new CallbackList(2));
        eventHandlers.put("player_starts_sneaking",new CallbackList(1));
        eventHandlers.put("player_stops_sneaking",new CallbackList(1));
        eventHandlers.put("player_starts_sprinting",new CallbackList(1));
        eventHandlers.put("player_stops_sprinting",new CallbackList(1));
    }

    public boolean addEvent(String event, String host, String funName)
    {
        if (!eventHandlers.containsKey(event))
        {
            return false;
        }
        return eventHandlers.get(event).addEventCall(host, funName);
    }

    public boolean removeEvent(String event, String funName)
    {

        if (!eventHandlers.containsKey(event))
            return false;
        Callback callback= decodeCallback(funName);
        eventHandlers.get(event).removeEventCall(callback.host, callback.udf);
        return true;
    }

    private Callback decodeCallback(String funName)
    {
        Pattern find = Pattern.compile("(\\w+)\\(from (\\w+)\\)");
        Matcher matcher = find.matcher(funName);
        if(matcher.matches())
        {
            return new Callback(matcher.group(2), matcher.group(1));
        }
        return new Callback(null, funName);
    }

    public void onTick()
    {
        eventHandlers.get("tick").call(Collections::emptyList, CarpetServer.minecraft_server::getCommandSource);
        eventHandlers.get("tick_nether").call(Collections::emptyList, () ->
                CarpetServer.minecraft_server.getCommandSource().withWorld(CarpetServer.minecraft_server.getWorld(DimensionType.NETHER)));
        eventHandlers.get("tick_ender").call(Collections::emptyList, () ->
                CarpetServer.minecraft_server.getCommandSource().withWorld(CarpetServer.minecraft_server.getWorld(DimensionType.THE_END)));
    }

    public void onPlayerJumped(EntityPlayer player)
    {
        eventHandlers.get("player_jumps").call( () -> Arrays.asList(((c, t) -> new EntityValue(player))), player::getCommandSource);
    }

    public void onElytraDeploy(EntityPlayerMP player)
    {
        eventHandlers.get("player_deploys_elytra").call( () -> Arrays.asList(((c, t) -> new EntityValue(player))), player::getCommandSource);
    }

    public void onOutOfBed(EntityPlayerMP player)
    {
        eventHandlers.get("player_wakes_up").call( () -> Arrays.asList(((c, t) -> new EntityValue(player))), player::getCommandSource);
    }

    public void onMountControls(EntityPlayer player, float strafeSpeed, float forwardSpeed, boolean jumping, boolean sneaking)
    {
        eventHandlers.get("player_rides").call( () -> Arrays.asList(
                ((c, t) -> new EntityValue(player)),
                ((c, t) -> new NumericValue(forwardSpeed)),
                ((c, t) -> new NumericValue(strafeSpeed)),
                ((c, t) -> new NumericValue(jumping)),
                ((c, t) -> new NumericValue(sneaking))
        ), player::getCommandSource);
    }

    public void onRightClick(EntityPlayerMP player, ItemStack itemstack, EnumHand enumhand)
    {
        eventHandlers.get("player_uses_item").call( () -> Arrays.asList(
                ((c, t) -> new EntityValue(player)),
                ((c, t) -> ListValue.of(
                        new StringValue(IRegistry.ITEM.getKey(itemstack.getItem()).getPath()),
                        new NumericValue(itemstack.getCount()),
                        new StringValue(itemstack.write(new NBTTagCompound()).getString())
                )),
                ((c, t) -> new StringValue(enumhand==EnumHand.MAIN_HAND?"mainhand":"offhand"))
        ), player::getCommandSource);
    }

    public void onBlockClicked(EntityPlayerMP player, BlockPos blockpos, EnumFacing facing)
    {
        eventHandlers.get("player_clicks_block").call( () -> Arrays.asList(
                ((c, t) -> new EntityValue(player)),
                ((c, t) -> new BlockValue(null, player.world, blockpos)),
                ((c, t) -> new StringValue(facing.getName()))
        ), player::getCommandSource);
    }

    public void onRightClickBlock(EntityPlayerMP player, ItemStack itemstack, EnumHand enumhand, BlockPos blockpos, EnumFacing enumfacing, Vec3d vec3d)
    {
        eventHandlers.get("player_right_clicks_block").call( () -> Arrays.asList(
                ((c, t) -> new EntityValue(player)),
                ((c, t) -> ListValue.of(
                        new StringValue(IRegistry.ITEM.getKey(itemstack.getItem()).getPath()),
                        new NumericValue(itemstack.getCount()),
                        new StringValue(itemstack.write(new NBTTagCompound()).getString())
                )),
                ((c, t) -> new StringValue(enumhand==EnumHand.MAIN_HAND?"mainhand":"offhand")),
                ((c, t) -> new BlockValue(null, player.world, blockpos)),
                ((c, t) -> new StringValue(enumfacing.getName())),
                ((c, t) -> ListValue.of(
                        new NumericValue(vec3d.x),
                        new NumericValue(vec3d.y),
                        new NumericValue(vec3d.z)
                ))
        ), player::getCommandSource);
    }

    public void onBlockBroken(EntityPlayer player, World world, BlockPos pos, IBlockState previousBS)
    {
        eventHandlers.get("player_breaks_block").call( () -> Arrays.asList(
                ((c, t) -> new EntityValue(player)),
                ((c, t) -> new BlockValue(previousBS, world, pos))
        ), player::getCommandSource);
    }

    public void onEntityInteracted(EntityPlayerMP player, Entity entity, EnumHand enumhand)
    {
        eventHandlers.get("player_interacts_with_entity").call( () -> Arrays.asList(
                ((c, t) -> new EntityValue(player)),
                ((c, t) -> new EntityValue(entity)),
                ((c, t) -> new StringValue(enumhand==EnumHand.MAIN_HAND?"mainhand":"offhand"))
        ), player::getCommandSource);
    }

    public void onEntityAttacked(EntityPlayerMP player, Entity entity)
    {
        eventHandlers.get("player_attacks_entity").call( () -> Arrays.asList(
                ((c, t) -> new EntityValue(player)),
                ((c, t) -> new EntityValue(entity))
        ), player::getCommandSource);
    }

    public void onStartSneaking(EntityPlayer player)
    {
        eventHandlers.get("player_starts_sneaking").call( () -> Arrays.asList(((c, t) -> new EntityValue(player))), player::getCommandSource);
    }

    public void onStopSneaking(EntityPlayer player)
    {
        eventHandlers.get("player_stops_sneaking").call( () -> Arrays.asList(((c, t) -> new EntityValue(player))), player::getCommandSource);
    }

    public void onStartSprinting(EntityPlayer player)
    {
        eventHandlers.get("player_starts_sprinting").call( () -> Arrays.asList(((c, t) -> new EntityValue(player))), player::getCommandSource);
    }

    public void onStopSprinting(EntityPlayer player)
    {
        eventHandlers.get("player_stops_sprinting").call( () -> Arrays.asList(((c, t) -> new EntityValue(player))), player::getCommandSource);
    }
}
