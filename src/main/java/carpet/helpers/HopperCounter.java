package carpet.helpers;

import carpet.utils.Messenger;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.*;
import java.util.stream.Collectors;

public class HopperCounter {
    public static final Map<EnumDyeColor, HopperCounter> COUNTERS;
    static {
        EnumMap<EnumDyeColor, HopperCounter> counterMap = new EnumMap<>(EnumDyeColor.class);
        for (EnumDyeColor color : EnumDyeColor.values()) {
            counterMap.put(color, new HopperCounter(color));
        }
        COUNTERS = Maps.immutableEnumMap(counterMap);
    }

    public final EnumDyeColor color;
    private final Object2LongMap<Item> counter = new Object2LongLinkedOpenHashMap<>();
    private long startTick;
    private long startMillis;

    public HopperCounter(EnumDyeColor color) {
        this.color = color;
    }

    public static List<ITextComponent> formatAll(MinecraftServer server, boolean realtime) {
        List<ITextComponent> text = new ArrayList<>();
        for (HopperCounter counter : COUNTERS.values()) {
            List<ITextComponent> temp = counter.format(server, realtime, false);
            if (temp.size() > 1) {
                if (!text.isEmpty()) text.add(Messenger.s(""));
                text.addAll(temp);
            }
        }
        if (text.isEmpty()) {
            text.add(Messenger.s("No items have been counted yet."));
        }
        return text;
    }

    public List<ITextComponent> format(MinecraftServer server, boolean realTime, boolean brief) {
        if (counter.isEmpty()) {
            if (brief) {
                return Collections.singletonList(Messenger.c("g "+color+": -, -/h, - min "));
            }
            return Collections.singletonList(Messenger.s(String.format("No items for %s yet", color.getName())));
        }
        long total = getTotalItems();
        long ticks = Math.max(realTime ? (System.currentTimeMillis() - startMillis) / 50 : server.getTickCounter() - startTick, 1);
        if (total == 0) {
            if (brief) {
                return Collections.singletonList(Messenger.c(String.format("c %s: 0, 0/h, %.1f min ", color, ticks / (20.0 * 60.0))));
            }
            return Collections.singletonList(Messenger.c(String.format("w No items for %s yet (%.2f min.%s)", color.getName(), ticks / (20.0 * 60.0), (realTime ? " - real time" : "")), "nb  [X]", "^g reset", "!/counter " + color.getName() +" reset"));
        }
        if (brief) {
            return Collections.singletonList(Messenger.c(String.format("c %s: %d, %d/h, %.1f min ", color.getName(), total, total * (20 * 60 * 60) / ticks, ticks / (20.0 * 60.0))));
        }
        List<ITextComponent> items = new ArrayList<>();
        items.add(Messenger.c(String.format("w Items for %s (%.2f min.%s), total: %d, (%.1f/h):", color, ticks*1.0/(20*60), (realTime?" - real time":""), total, total*1.0*(20*60*60)/ticks), "nb [X]", "^g reset", "!/counter "+color+" reset"));
        items.addAll(counter.object2LongEntrySet().stream().sorted((e, f) -> Long.compare(f.getLongValue(), e.getLongValue())).map(e -> {
            ITextComponent itemName = new TextComponentTranslation(e.getKey().getTranslationKey());
            long count = e.getLongValue();
            return Messenger.c("w - ", itemName, String.format("w : %d, %.1f/h", count, count * (20.0 * 60.0 * 60.0) / ticks)); }).collect(Collectors.toList()));
        return items;
    }

    public long getTotalItems()
    {
        return counter.values().stream().mapToLong(Long::longValue).sum();
    }

    public static void resetAll(MinecraftServer server) {
        for (HopperCounter counter : COUNTERS.values()) {
            counter.reset(server);
        }
    }

    public void reset(MinecraftServer server) {
        counter.clear();
        startTick = server.getTickCounter();
        startMillis = System.currentTimeMillis();
    }

    public static HopperCounter getCounter(String color) {
        try {
            EnumDyeColor colorEnum = EnumDyeColor.valueOf(color.toUpperCase(Locale.ROOT));
            return COUNTERS.get(colorEnum);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void add(MinecraftServer server, ItemStack stack) {
        if (startTick == 0) {
            startTick = server.getTickCounter();
            startMillis = System.currentTimeMillis();
        }
        Item item = stack.getItem();
        counter.put(item, counter.getLong(item) + stack.getCount());
    }
}
