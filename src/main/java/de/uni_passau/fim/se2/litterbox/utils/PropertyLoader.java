package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class PropertyLoader {

    private PropertyLoader() {
    }

    private static final Logger log = Logger.getLogger(PropertyLoader.class.getName());

    /**
     * Load the string value of the given key from the system properties.
     * If the key has a value assigned, that is a parsable integer, return that integer.
     * Otherwise, write a warning to the logs and return 0.
     *
     * @param name The key to the system variable.
     * @return The value to the given key as integer or 0 otherwise.
     */
    public static int getSystemIntProperty(String name) {
        String stringValue = System.getProperty(name);
        if (stringValue == null) {
            throw new IllegalArgumentException("The value for key " + name + " is not set in the system properties!");
        } else {
            try {
                return Integer.parseInt(stringValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The value for key " + name + " is not an integer value!");
            }
        }
    }

    /**
     * Load the string value of the given key from the system properties.
     * If the key has a value assigned, that is a parsable floating number, return that value as a double.
     * Otherwise, write a warning to the logs and return 0.
     *
     * @param name The key to the system variable.
     * @return The value to the given key as double or 0 otherwise.
     */
    public static double getSystemDoubleProperty(String name) {
        String stringValue = System.getProperty(name);
        if (stringValue == null) {
            throw new IllegalArgumentException("The value for key " + name + " is not set in the system properties!");
        } else {
            try {
                return Double.parseDouble(stringValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The value for key " + name + " is not a floating number!");
            }
        }
    }

    /**
     * If a seed is specified and a valid long, returns the specified long wrapped in an optional.
     * If the seed is not specified, returns an empty optional.
     * Throws an exception if the seed is specified but not a parsable long.
     *
     * @return The seed, if specified, {@code Optional.empty()} otherwise.
     */
    public static Optional<Long> getSeed() {
        String stringValue = System.getProperty("nsga-ii.seed");
        if (stringValue == null) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(Long.parseLong(stringValue));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The specified seed is not a long.");
            }
        }
    }

    /**
     * If a property is already set as a system property via command line, use that value.
     * Otherwise set all properties in the system as defined property file with the given name.
     *
     * @param propertyFileName The name of the properties file in the classpath.
     */
    public static void setDefaultSystemProperties(String propertyFileName) {
        InputStream input = Main.class.getClassLoader().getResourceAsStream(propertyFileName);

        var propertiesInFile = new Properties();
        try {
            propertiesInFile.load(input);
        } catch (IOException e) {
            log.log(Level.WARNING, "The file {0} is not loaded in the classpath.", propertyFileName);
        }

        propertiesInFile.forEach((key, value) -> {
            if (System.getProperty((String) key) == null) {
                System.setProperty((String) key, (String) value);
            }
        });
    }

    /**
     * Set the global logging level of the java.util.logging root logger.
     * It is either set to the valid logging level name (e.g. "ALL", "INFO" or its integer values such "1") provided
     * as JVM argument for the key 'logLevel' or the default logging level {@code Level.INFO} is set.
     * <p>
     * For example, to set the global log level to "FINEST" call the JAR of the project with the JVM option {@code -DlogLevel=FINEST}.
     */
    public static void setGlobalLoggingLevelFromEnvironment() {
        var logLevel = System.getProperty("logLevel", null);
        Level level;
        try {
            level = Level.parse(logLevel);
        } catch (NullPointerException | IllegalArgumentException e) {
            level = Level.INFO;
        }
        setGlobalLoggingLevel(level);
    }

    private static void setGlobalLoggingLevel(Level level) {
        var rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(level);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(level);
        }
    }
}
