package carpet.logging;

import java.lang.reflect.Field;

public class HUDLogger extends Logger{
    public HUDLogger(Field acceleratorField, String logName, String def, String[] options) {
        super(acceleratorField, logName, def, options);
    }

    static Logger stardardHUDLogger(String logName, String def, String [] options) {
        // should convert to factory method if more than 2 classes are here
        try {
            return new HUDLogger(LoggerRegistry.class.getField("__"+logName), logName, def, options);
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to create logger "+logName);
        }
    }
}
