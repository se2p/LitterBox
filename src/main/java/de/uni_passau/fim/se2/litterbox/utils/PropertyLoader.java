package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
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
     * If a property is already set as a system property via command line, use that value.
     * Otherwise set all properties in the system as defined property file with the given name.
     *
     * @param propertyFileName The name of the properties file in the classpath.
     */
    public static void setDefaultSystemProperties(String propertyFileName) {
        InputStream input = Main.class.getClassLoader().getResourceAsStream(propertyFileName);

        Properties propertiesInFile = new Properties();
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
}
