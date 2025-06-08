package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ui.exceptions.DIException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Dependency Injection Manager for the AUREUS system.
 * Handles the creation and management of dependencies based on configuration in di.properties.
 *
 * @author Pau Vilardell
 * @version 1.0
 * @since 2025-06-05
 */
public class DIManager {
    private final Properties properties;

    /**
     * Constructs a new DIManager and loads the dependency configuration from di.properties.
     *
     * @throws DIException if the properties file cannot be loaded
     */
    public DIManager() {
        properties = new Properties();
        try (InputStream in = this.getClass().getResourceAsStream("/di.properties")) {
            if (in == null) {
                throw new DIException("No se ha encontrado el archivo di.properties en el classpath.");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new DIException(e);
        }
    }

    /**
     * Creates and returns an instance of the requested class based on the configuration.
     *
     * @param className the name of the class to instantiate as defined in di.properties
     * @param <T> the type of object to return
     * @return an instance of the requested class
     * @throws DIException if the class cannot be found or instantiated
     */
    public <T> T get(String className) {
        try {
            var clazz = Class.forName(properties.getProperty(className));
            return (T)clazz.getDeclaredConstructors()[0].newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new DIException(e);
        }
    }
}