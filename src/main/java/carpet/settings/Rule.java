package carpet.settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule
{
    /**
     * The rule name, by default the same as the field name
     */
    String name() default ""; // default same as field name

    /**
     * A description of the rule
     */
    String desc();

    /**
     * Extra information about the rule
     */
    String[] extra() default {};

    /**
     * A list of categories the rule is in
     */
    String[] category();

    /**
     * Options to select in menu.
     * Inferred for booleans and enums. Otherwise, must be present.
     */
    String[] options() default {};

    /**
     * if a rule is not strict - can take any value, otherwise it needs to match
     * any of the options
     * For enums, its always strict, same for booleans - no need to set that for them.
     */
    boolean strict() default true;

    /**
     * The class of the validator checked when the rule is changed.
     */
    Class<? extends Validator>[] validate() default {};
}
