package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyLoader {

    private PropertyLoader() {}

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
