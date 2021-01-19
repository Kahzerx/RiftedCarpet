package carpet.utils;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Messenger
{
    public static final Logger LOG = LogManager.getLogger();

    /*
     messsage: "desc me ssa ge"
     desc contains:
     i = italic
     s = strikethrough
     u = underline
     b = bold
     o = obfuscated

     w = white
     y = yellow
     m = magenta (light purple)
     r = red
     c = cyan (aqua)
     l = lime (green)
     t = light blue (blue)
     f = dark gray
     g = gray
     d = gold
     p = dark purple (purple)
     n = dark red (brown)
     q = dark aqua
     e = dark green
     v = dark blue (navy)
     k = black

     / = action added to the previous component
     */

    public static Style parseStyle(String style)
    {
        //could be rewritten to be more efficient
        Style styleInstance = new Style();
        styleInstance.setItalic(style.indexOf('i')>=0);
        styleInstance.setStrikethrough(style.indexOf('s')>=0);
        styleInstance.setUnderlined(style.indexOf('u')>=0);
        styleInstance.setBold(style.indexOf('b')>=0);
        styleInstance.setObfuscated(style.indexOf('o')>=0);
        styleInstance.setColor(TextFormatting.WHITE);
        if (style.indexOf('w')>=0) styleInstance.setColor(TextFormatting.WHITE); // not needed
        if (style.indexOf('y')>=0) styleInstance.setColor(TextFormatting.YELLOW);
        if (style.indexOf('m')>=0) styleInstance.setColor(TextFormatting.LIGHT_PURPLE);
        if (style.indexOf('r')>=0) styleInstance.setColor(TextFormatting.RED);
        if (style.indexOf('c')>=0) styleInstance.setColor(TextFormatting.AQUA);
        if (style.indexOf('l')>=0) styleInstance.setColor(TextFormatting.GREEN);
        if (style.indexOf('t')>=0) styleInstance.setColor(TextFormatting.BLUE);
        if (style.indexOf('f')>=0) styleInstance.setColor(TextFormatting.DARK_GRAY);
        if (style.indexOf('g')>=0) styleInstance.setColor(TextFormatting.GRAY);
        if (style.indexOf('d')>=0) styleInstance.setColor(TextFormatting.GOLD);
        if (style.indexOf('p')>=0) styleInstance.setColor(TextFormatting.DARK_PURPLE);
        if (style.indexOf('n')>=0) styleInstance.setColor(TextFormatting.DARK_RED);
        if (style.indexOf('q')>=0) styleInstance.setColor(TextFormatting.DARK_AQUA);
        if (style.indexOf('e')>=0) styleInstance.setColor(TextFormatting.DARK_GREEN);
        if (style.indexOf('v')>=0) styleInstance.setColor(TextFormatting.DARK_BLUE);
        if (style.indexOf('k')>=0) styleInstance.setColor(TextFormatting.BLACK);
        return styleInstance;
    }
    public static String heatmap_color(double actual, double reference)
    {
        String color = "g";
        if (actual >= 0.0D) color = "e";
        if (actual > 0.5D*reference) color = "y";
        if (actual > 0.8D*reference) color = "r";
        if (actual > reference) color = "m";
        return color;
    }
    public static String creatureTypeColor(EnumCreatureType type)
    {
        switch (type)
        {
            case MONSTER:
                return "n";
            case CREATURE:
                return "e";
            case AMBIENT:
                return "f";
            case WATER_CREATURE:
                return "v";
        }
        return "w";
    }

    private static ITextComponent _getChatComponentFromDesc(String message, ITextComponent previous_message)
    {
        if (message.equalsIgnoreCase(""))
        {
            return new TextComponentString("");
        }
        if (Character.isWhitespace(message.charAt(0)))
        {
            message = "w"+message;
        }
        int limit = message.indexOf(' ');
        String desc = message;
        String str = "";
        if (limit >= 0)
        {
            desc = message.substring(0, limit);
            str = message.substring(limit+1);
        }
        if (desc.charAt(0) == '/') // deprecated
        {
            if (previous_message != null)
                previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message));
            return previous_message;
        }
        if (desc.charAt(0) == '?')
        {
            if (previous_message != null)
                previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message.substring(1)));
            return previous_message;
        }
        if (desc.charAt(0) == '!')
        {
            if (previous_message != null)
                previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, message.substring(1)));
            return previous_message;
        }
        if (desc.charAt(0) == '^')
        {
            if (previous_message != null)
                previous_message.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, c(message.substring(1))));
            return previous_message;
        }
        ITextComponent txt = new TextComponentString(str);
        txt.setStyle(parseStyle(desc));
        return txt;
    }
    public static ITextComponent tp(String desc, Vec3d pos) { return tp(desc, pos.x, pos.y, pos.z); }
    public static ITextComponent tp(String desc, BlockPos pos) { return tp(desc, pos.getX(), pos.getY(), pos.getZ()); }
    public static ITextComponent tp(String desc, double x, double y, double z) { return tp(desc, (float)x, (float)y, (float)z);}
    public static ITextComponent tp(String desc, float x, float y, float z)
    {
        return _getCoordsTextComponent(desc, x, y, z, false);
    }
    public static ITextComponent tp(String desc, int x, int y, int z)
    {
        return _getCoordsTextComponent(desc, (float)x, (float)y, (float)z, true);
    }

    /// to be continued
    public static ITextComponent dbl(String style, double double_value)
    {
        return c(String.format("%s %.1f",style,double_value),String.format("^w %f",double_value));
    }
    public static ITextComponent dbls(String style, double ... doubles)
    {
        StringBuilder str = new StringBuilder(style + " [ ");
        String prefix = "";
        for (double dbl : doubles)
        {
            str.append(String.format("%s%.1f", prefix, dbl));
            prefix = ", ";
        }
        str.append(" ]");
        return c(str.toString());
    }
    public static ITextComponent dblf(String style, double ... doubles)
    {
        StringBuilder str = new StringBuilder(style + " [ ");
        String prefix = "";
        for (double dbl : doubles)
        {
            str.append(String.format("%s%f", prefix, dbl));
            prefix = ", ";
        }
        str.append(" ]");
        return c(str.toString());
    }
    public static ITextComponent dblt(String style, double ... doubles)
    {
        List<Object> components = new ArrayList<>();
        components.add(style+" [ ");
        String prefix = "";
        for (double dbl:doubles)
        {

            components.add(String.format("%s %s%.1f",style, prefix, dbl));
            components.add("?"+dbl);
            components.add("^w "+dbl);
            prefix = ", ";
        }
        //components.remove(components.size()-1);
        components.add(style+"  ]");
        return c(components.toArray(new Object[0]));
    }

    private static ITextComponent _getCoordsTextComponent(String style, float x, float y, float z, boolean isInt)
    {
        String text;
        String command;
        if (isInt)
        {
            text = String.format("%s [ %d, %d, %d ]",style, (int)x,(int)y, (int)z );
            command = String.format("!/tp %d %d %d",(int)x,(int)y, (int)z);
        }
        else
        {
            text = String.format("%s [ %.1f, %.1f, %.1f]",style, x, y, z);
            command = String.format("!/tp %.3f %.3f %.3f",x, y, z);
        }
        return c(text, command);
    }

    //message source
    public static void m(CommandSource source, Object ... fields)
    {
        if (source != null)
            source.sendFeedback(Messenger.c(fields),source.getServer() != null && source.getServer().getWorld(DimensionType.OVERWORLD) != null);
    }
    public static void m(EntityPlayer player, Object ... fields)
    {
        player.sendMessage(Messenger.c(fields));
    }

    /*
    composes single line, multicomponent message, and returns as one chat messagge
     */
    public static ITextComponent c(Object ... fields)
    {
        ITextComponent message = new TextComponentString("");
        ITextComponent previous_component = null;
        for (Object o: fields)
        {
            if (o instanceof ITextComponent)
            {
                message.appendSibling((ITextComponent)o);
                previous_component = (ITextComponent)o;
                continue;
            }
            String txt = o.toString();
            ITextComponent comp = _getChatComponentFromDesc(txt,previous_component);
            if (comp != previous_component) message.appendSibling(comp);
            previous_component = comp;
        }
        return message;
    }

    //simple text

    public static ITextComponent s(String text)
    {
        return s(text,"");
    }
    public static ITextComponent s(String text, String style)
    {
        ITextComponent message = new TextComponentString(text);
        message.setStyle(parseStyle(style));
        return message;
    }

    public static void send(EntityPlayer player, Collection<ITextComponent> lines)
    {
        lines.forEach(player::sendMessage);
    }
    public static void send(CommandSource source, Collection<ITextComponent> lines)
    {
        lines.stream().forEachOrdered((s) -> source.sendFeedback(s, false));
    }

    public static void print_server_message(MinecraftServer server, String message)
    {
        if (server == null)
            LOG.error("Message not delivered: "+message);
        server.sendMessage(new TextComponentString(message));
        ITextComponent txt = c("gi "+message);
        for (EntityPlayer entityplayer : server.getPlayerList().getPlayers())
        {
            entityplayer.sendMessage(txt);
        }
    }
    public static void print_server_message(MinecraftServer server, ITextComponent message)
    {
        if (server == null)
            LOG.error("Message not delivered: "+message.getString());
        server.sendMessage(message);
        for (EntityPlayer entityplayer : server.getPlayerList().getPlayers())
        {
            entityplayer.sendMessage(message);
        }
    }
}