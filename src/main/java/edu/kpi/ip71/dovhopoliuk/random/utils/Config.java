package edu.kpi.ip71.dovhopoliuk.random.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (FileReader fileReader = new FileReader("resources/project.properties")) {
            properties.load(fileReader);
        } catch (IOException e) {
            throw new IllegalStateException("Can not read property file", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getPropertyOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
