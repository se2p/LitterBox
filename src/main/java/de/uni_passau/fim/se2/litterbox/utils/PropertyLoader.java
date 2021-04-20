package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {

    private PropertyLoader() {}

    /**
     * If a property is already set as a system property via command line, use that value.
     * Otherwise set all properties in the system as defined in the loaded property file.
     */
    public static void setDefaultSystemProperties() {
        InputStream input = Main.class.getClassLoader().getResourceAsStream("nsga-ii.properties");

        Properties nsgaIIProperties = new Properties();
        try {
            nsgaIIProperties.load(input);
        } catch (IOException e) {
            System.err.println("Could not load NSGA-II properties");
        }

        nsgaIIProperties.forEach((key, value) -> {
            if (System.getProperty((String) key) == null) {
                System.setProperty((String) key, (String) value);
            }
        });
    }
}
