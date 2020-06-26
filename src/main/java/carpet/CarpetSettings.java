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

@SuppressWarnings("CanBeFinal")
public class CarpetSettings {
    public static final String carpetVersion = "0.9.2+v240620";
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
            desc = "Hoppers pointing to wool will count items passing through them",
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

    @Rule( desc = "Guardians turn into Elder Guardian when struck by lightning", category = FEATURE )
    public static boolean renewableSponges = false;

    @Rule( desc = "Saplings turn into dead shrubs in hot climates and no water access", category = FEATURE )
    public static boolean desertShrubs = false;

    @Rule( desc = "Silverfish drop a gravel item when breaking out of a block", category = FEATURE )
    public static boolean silverFishDropGravel = false;

    @Rule( desc = "Summoning a lightning bolt has all the side effects of natural lightning", category = CREATIVE )
    public static boolean summonNaturalLightning = false;

    @Rule(desc = "Enables /spawn command for spawn tracking",
            extra = "You may need to setDefault true/false, exit and join the world for /counter to appear or not in command list",
            category = COMMAND)
    public static String commandSpawn = "false";

    @Rule(desc = "Enables /tick command to control game clocks", category = COMMAND)
    public static String commandTick = "true";

    @Rule(desc = "Enables /log command to monitor events via chat and overlays", category = COMMAND)
    public static String commandLog = "true";

    @Rule(
            desc = "sets these loggers in their default configurations for all new players",
            extra = "use csv, like 'tps,mobcaps' for multiple loggers, none for nothing",
            category = {CREATIVE, SURVIVAL},
            options = {"none", "tps", "mobcaps,tps"},
            strict = false
    )
    public static String defaultLoggers = "none";

    @Rule(
            desc = "Enables /distance command to measure in game distance between points",
            extra = "Also enables brown carpet placement action if 'carpets' rule is turned on as well",
            category = COMMAND
    )
    public static String commandDistance = "true";

    @Rule(
            desc = "Enables /info command for blocks",
            extra = {
                    "Also enables gray carpet placement action",
                    "if 'carpets' rule is turned on as well"
            },
            category = COMMAND
    )
    public static String commandInfo = "true";

    @Rule(
            desc = "Enables /c and /s commands to quickly switch between camera and survival modes",
            extra = {
                    "/c and /s commands are available to all players regardless of their permission levels",
                    "You may need to setDefault true/false, exit and join the world for /c - /s to appear or not in command list"
            },
            category = COMMAND
    )
    public static String commandCameramode = "true";

    @Rule(
            desc = "Enables /perimeterinfo command",
            extra = "... that scans the area around the block for potential spawnable spots",
            category = COMMAND
    )
    public static String commandPerimeterInfo = "true";

    @Rule(desc = "Enables /draw commands", extra = {"... allows for drawing simple shapes or","other shapes which are sorta difficult to do normally"}, category = COMMAND)
    public static String commandDraw = "true";

    @Rule(desc = "Enables /script command", extra = "WIP... An in-game scripting API for Scarpet programming language", category = COMMAND)
    public static boolean commandScript = true;

    @Rule(desc = "Enables /player command to control/spawn players", category = COMMAND)
    public static String commandPlayer = "true";

    @Rule(desc = "Placing carpets may issue carpet commands for non-op players", category = SURVIVAL)
    public static boolean carpets = false;

    @Rule(desc = "Pistons, Glass and Sponge can be broken faster with their appropriate tools", category = SURVIVAL)
    public static boolean missingTools = false;

    @Rule(desc = "fill/clone/setblock and structure blocks cause block updates", category = CREATIVE)
    public static boolean fillUpdates = true;

    private static class PushLimitLimits extends Validator<Integer> {
        @Override public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string) {
            return (newValue>0 && newValue <= 1024) ? newValue : null;
        }
        @Override
        public String description() { return "You must choose a value from 1 to 1024";}
    }
    @Rule(
            desc = "Customizable piston push limit",
            options = {"10", "12", "14", "100"},
            category = CREATIVE,
            strict = false,
            validate = PushLimitLimits.class
    )
    public static int pushLimit = 12;

    @Rule(
            desc = "Customizable powered rail power range",
            options = {"9", "15", "30"},
            category = CREATIVE,
            strict = false,
            validate = PushLimitLimits.class
    )
    public static int railPowerLimit = 9;

    private static class FillLimitLimits extends Validator<Integer> {
        @Override public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string) {
            return (newValue>0 && newValue < 20000000) ? newValue : null;
        }
        @Override
        public String description() { return "You must choose a value from 1 to 20M";}
    }
    @Rule(
            desc = "Customizable fill/clone volume limit",
            options = {"32768", "250000", "1000000"},
            category = CREATIVE,
            strict = false,
            validate = FillLimitLimits.class
    )
    public static int fillLimit = 32768;

    @Rule(
            desc = "Customizable forceload chunk limit",
            options = {"256"},
            category = CREATIVE,
            strict = false,
            validate = FillLimitLimits.class
    )
    public static int forceloadLimit = 256;

    @Rule(desc = "One player is required on the server to cause night to pass", category = SURVIVAL)
    public static boolean onePlayerSleeping = false;

    @Rule(
            desc = "Cactus in dispensers rotates blocks.",
            extra = "Rotates block anti-clockwise if possible",
            category = {FEATURE, DISPENSER}
    )
    public static boolean rotatorBlock = false;

    @Rule(desc = "Coral structures will grow with bonemeal from coral plants", category = FEATURE)
    public static boolean renewableCoral = false;

    @Rule(
            desc = "Removes fog from client in the nether and the end",
            extra = "Improves visibility, but looks weird",
            category = CLIENT
    )
    public static boolean fogOff = false;

    @Rule(
            desc = "Creative No Clip",
            extra = {
                    "On servers it needs to be set on both ",
                    "client and server to function properly.",
                    "Has no effect when set on the server only",
                    "Can allow to phase through walls",
                    "if only set on the carpet client side",
                    "but requires some trapdoor magic to",
                    "allow the player to enter blocks"
            },
            category = {CREATIVE, CLIENT}
    )
    public static boolean creativeNoClip = false;
}
