package carpet;

import carpet.settings.ParsedRule;
import carpet.settings.Rule;
import carpet.settings.Validator;
import carpet.utils.Messenger;
import carpet.utils.Translations;
import net.minecraft.command.CommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static carpet.settings.RuleCategory.FEATURE;

public class CarpetSettings {
    public static final String carpetVersion = "1.0.0+v00001";
    public static final Logger LOG = LogManager.getLogger();

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
}
