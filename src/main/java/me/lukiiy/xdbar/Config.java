package me.lukiiy.xdbar;

import org.slf4j.Logger;

import java.io.IOException;
import java.util.Properties;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    private final Properties properties = new Properties();
    public final Path filePath;
    private final String modName;
    private final Logger logger;

    public Config(Path configDir, Logger logger, String fileName, String modName) {
        this.modName = modName;
        this.filePath = configDir.resolve(fileName + ".properties");
        this.logger = logger;
        load();
    }

    public void load() {
        if (Files.notExists(filePath)) {
            save();
            return;
        }

        try (var reader = Files.newBufferedReader(filePath)) {
            properties.load(reader);
        } catch (IOException e) {
            logger.error("Failed to load config: {}", e.getMessage(), e);
        }
    }

    public void save() {
        try (var writer = Files.newBufferedWriter(filePath)) {
            properties.store(writer, modName + " Config");
        } catch (IOException e) {
            logger.error("Failed to save config: {}", e.getMessage(), e);
        }
    }

    public boolean has(String key) {
        return properties.containsKey(key);
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
        save();
    }

    public void setIfAbsent(String key, String value) {
        if (!has(key)) set(key, value);
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public Boolean getBoolean(String key) {
        return get(key).equalsIgnoreCase("true");
    }
}