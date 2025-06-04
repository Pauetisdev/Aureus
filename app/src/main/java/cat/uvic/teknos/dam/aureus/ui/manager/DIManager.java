package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ui.exceptions.DIException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;


public class DIManager {
    private final Properties properties;

    public DIManager() {
        properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/di.properties"));
        } catch (IOException e) {
            throw new DIException(e);
        }
    }
    public <T> T get(String className) {
        try {
            var clazz = Class.forName(properties.getProperty(className));
            return (T)clazz.getDeclaredConstructors()[0].newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new DIException(e);
        }
    }
}

