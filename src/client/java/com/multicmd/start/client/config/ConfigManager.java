// Файл: src/client/java/com/multicmd/start/client/config/ConfigManager.java
package com.multicmd.start.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер конфигураций. Добавлено постоянное хранилище для пользовательских биндов клавиатуры.
 */
public class ConfigManager {
    public static final Logger LOGGER = LoggerFactory.getLogger("MultiCmd");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("multicmd");
    public static final Path SCRIPTS_DIR = CONFIG_DIR.resolve("scripts");

    private static final File CONFIG_FILE = CONFIG_DIR.resolve("config.json").toFile();
    private static final File GROUPS_FILE = CONFIG_DIR.resolve("groups.json").toFile();
    private static final File MACROS_FILE = CONFIG_DIR.resolve("macros.json").toFile();
    private static final File KEYBINDS_FILE = CONFIG_DIR.resolve("keybinds.json").toFile();

    public static volatile int delayTicks = 10;
    public static volatile boolean showHud = true;
    public static volatile String guiAlias = "m";

    public static Map<String, String> groups = new ConcurrentHashMap<>();
    public static Map<String, String> macros = new ConcurrentHashMap<>();
    public static Map<String, String> keybinds = new ConcurrentHashMap<>(); // Клавиша -> Команда

    public static void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            if (!Files.exists(SCRIPTS_DIR)) Files.createDirectories(SCRIPTS_DIR);

            loadConfigFile();
            loadMapFile(GROUPS_FILE, groups);
            loadMapFile(MACROS_FILE, macros);
            loadMapFile(KEYBINDS_FILE, keybinds);
        } catch (IOException e) {
            LOGGER.error("Критическая ошибка доступа к файловой системе", e);
        }
    }

    private static void loadConfigFile() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json != null) {
                    if (json.has("delayTicks")) delayTicks = Math.max(1, json.get("delayTicks").getAsInt());
                    if (json.has("showHud")) showHud = json.get("showHud").getAsBoolean();
                    if (json.has("guiAlias")) guiAlias = json.get("guiAlias").getAsString();
                }
            } catch (Exception e) { saveConfig(); }
        } else saveConfig();
    }

    private static void loadMapFile(File file, Map<String, String> map) {
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<ConcurrentHashMap<String, String>>(){}.getType();
                ConcurrentHashMap<String, String> loaded = GSON.fromJson(reader, type);
                if (loaded != null) map.putAll(loaded);
            } catch (Exception e) { saveMapFile(file, map); }
        } else saveMapFile(file, map);
    }

    public static synchronized void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            JsonObject json = new JsonObject();
            json.addProperty("delayTicks", delayTicks);
            json.addProperty("showHud", showHud);
            json.addProperty("guiAlias", guiAlias);
            GSON.toJson(json, writer);
        } catch (IOException e) {}
    }

    private static synchronized void saveMapFile(File file, Map<String, String> map) {
        try (FileWriter writer = new FileWriter(file)) { GSON.toJson(map, writer); } catch (IOException e) {}
    }

    public static void saveGroups() { saveMapFile(GROUPS_FILE, groups); }
    public static void saveMacros() { saveMapFile(MACROS_FILE, macros); }
    public static void saveKeybinds() { saveMapFile(KEYBINDS_FILE, keybinds); }
}