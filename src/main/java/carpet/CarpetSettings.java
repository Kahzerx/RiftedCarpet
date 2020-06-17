package carpet;

import carpet.settings.ParsedRule;
import carpet.settings.Rule;
import carpet.settings.Validator;
import carpet.utils.Messenger;
import carpet.utils.Translations;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static carpet.settings.RuleCategory.*;

public class CarpetSettings {
    public static final String carpetVersion = "1.0.0+v00001";
    public static final Logger LOG = LogManager.getLogger();
    public static boolean impendingFillSkipUpdates = false;
    public static AxisAlignedBB currentTelepotingEntityBox = null;
    public static Vec3d fixedPosition = null;

    private static class LanguageValidator extends Validator<String>{
        @Override public String validate(CommandSource source, ParsedRule<String> currentRule, String newValue, String string) {
            if (currentRule.get().equals(newValue) || source == null){
                return newValue;
            }
            if (!Translations.isValidLanguage(newValue)){
                Messenger.m(source, "r "+newValue+" is not a valid language");
                return null;
            }
            CarpetSettings.language = newValue;
            Translations.updateLanguage(source);
            return newValue;
        }
    }
    @Rule(
            desc = "sets the language for carpet",
            category = FEATURE,
            options = {"none", "zh_cn", "zh_tw"},
            strict = false,
            validate = LanguageValidator.class
    )
    public static String language = "none";

    @Rule(
            desc = "Nether portals correctly place entities going through",
            extra = "Entities shouldn't suffocate in obsidian",
            category = BUGFIX
    )
    public static boolean portalSuffocationFix = false;

    @Rule(
            desc = "Amount of delay ticks to use a nether portal in creative",
            options = {"1", "40", "80", "72000"},
            category = CREATIVE,
            strict = false,
            validate = OneHourMaxDelayLimit.class
    )
    public static int portalCreativeDelay = 1;

    @Rule(
            desc = "Amount of delay ticks to use a nether portal in survival",
            options = {"1", "40", "80", "72000"},
            category = SURVIVAL,
            strict = false,
            validate = OneHourMaxDelayLimit.class
    )
    public static int portalSurvivalDelay = 80;

    private static class OneHourMaxDelayLimit extends Validator<Integer> {
        @Override public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string) {
            return (newValue > 0 && newValue <= 72000) ? newValue : null;
        }
        @Override
        public String description() { return "You must choose a value from 1 to 72000";}
    }

    @Rule(desc = "Dropping entire stacks works also from on the crafting UI result slot", category = {BUGFIX, SURVIVAL})
    public static boolean ctrlQCraftingFix = false;

    @Rule(desc = "Parrots don't get of your shoulders until you receive proper damage", category = {SURVIVAL, FEATURE})
    public static boolean persistentParrots = false;

    @Rule( desc = "Players absorb XP instantly, without delay", category = CREATIVE )
    public static boolean xpNoCooldown = false;

    @Rule( desc = "XP orbs combine with other into bigger orbs", category = FEATURE )
    public static boolean combineXPOrbs = false;

    @Rule(
            desc = "Empty shulker boxes can stack to 64 when dropped on the ground",
            extra = "To move them around between inventories, use shift click to move entire stacks",
            category = {SURVIVAL, FEATURE}
    )
    public static boolean stackableShulkerBoxes = false;

    @Rule( desc = "Explosions won't destroy blocks", category = {CREATIVE, TNT} )
    public static boolean explosionNoBlockDamage = false;

    @Rule( desc = "Removes random TNT momentum when primed", category = {CREATIVE, TNT} )
    public static boolean tntPrimerMomentumRemoved = false;

    @Rule( desc = "Sets the horizontal random angle on TNT for debugging of TNT contraptions", category = TNT, options = "-1", strict = false,
            validate = TNTAngleValidator.class, extra = "Set to -1 for default behavior")
    public static double hardcodeTNTangle = -1.0D;

    private static class TNTAngleValidator extends Validator<Double> {
        @Override
        public Double validate(CommandSource source, ParsedRule<Double> currentRule, Double newValue, String string) {
            return (newValue >= 0 && newValue < Math.PI * 2) || newValue == -1 ? newValue : null;
        }

        @Override
        public String description() {
            return "Must be between 0 and 2pi, or -1";
        }
    }

    @Rule( desc = "Shulkers will respawn in end cities", category = FEATURE )
    public static boolean shulkerSpawningInEndCities = false;

    @Rule( desc = "TNT doesn't update when placed against a power source", category = {CREATIVE, TNT} )
    public static boolean tntDoNotUpdate = false;

    @Rule(desc = "Pistons, droppers and dispensers react if block above them is powered", category = CREATIVE)
    public static boolean quasiConnectivity = true;

    @Rule(
            desc = "Players can flip and rotate blocks when holding cactus",
            extra = {
                    "Applies to pistons, observers, droppers, repeaters, stairs, glazed terracotta etc..."
            },
            category = {CREATIVE, SURVIVAL, FEATURE}
    )
    public static boolean flippinCactus = false;

    @Rule(
            desc = "hoppers pointing to wool will count items passing through them",
            extra = {
                    "Enables /counter command, and actions while placing red and green carpets on wool blocks",
                    "Use /counter <color?> reset to reset the counter, and /counter <color?> to query",
                    "In survival, place green carpet on same color wool to query, red to reset the counters",
                    "Counters are global and shared between players, 16 channels available",
                    "Items counted are destroyed, count up to one stack per tick per hopper",
                    "You may need to setDefault true/false, exit and join the world for /counter to appear or not in command list"
            },
            category = {COMMAND, CREATIVE, FEATURE}
    )
    public static boolean hopperCounters = false;

    @Rule( desc = "Saplings turn into dead shrubs in hot climates and no water access", category = FEATURE )
    public static boolean desertShrubs = false;

    @Rule(
            desc = "Enables /c and /s commands to quickly switch between camera and survival modes",
            extra = {
                    "/c and /s commands are available to all players regardless of their permission levels",
                    "You may need to setDefault true/false, exit and join the world for /c - /s to appear or not in command list"
            },
            category = COMMAND
    )
    public static String commandCameramode = "true";
}
