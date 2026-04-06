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
 * Подсистема постоянного хранения данных (Persistence Layer).
 * Отвечает за I/O операции с локальными JSON файлами.
 */
public class ConfigManager {
    public static final Logger LOGGER = LoggerFactory.getLogger("MultiCmd-Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("multicmd");

    private static final File CONFIG_FILE = CONFIG_DIR.resolve("config.json").toFile();
    private static final File GROUPS_FILE = CONFIG_DIR.resolve("groups.json").toFile();
    private static final File MACROS_FILE = CONFIG_DIR.resolve("macros.json").toFile();

    public static int delayTicks = 10;
    public static boolean showHud = true;

    // Потокобезопасные коллекции для предотвращения ConcurrentModificationException
    public static Map<String, String> groups = new ConcurrentHashMap<>();
    public static Map<String, String> macros = new ConcurrentHashMap<>();

    public static void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            loadConfigFile();
            loadGroupsFile();
            loadMacrosFile();
        } catch (IOException e) {
            LOGGER.error("Критическая ошибка доступа к файловой системе при инициализации", e);
        }
    }

    private static void loadConfigFile() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json != null) {
                    if (json.has("delayTicks")) delayTicks = Math.max(1, json.get("delayTicks").getAsInt());
                    if (json.has("showHud")) showHud = json.get("showHud").getAsBoolean();
                }
            } catch (JsonSyntaxException | IllegalStateException | IOException e) {
                LOGGER.warn("Поврежден config.json. Сброс до заводских настроек.", e);
                saveConfig();
            }
        } else {
            saveConfig();
        }
    }

    private static void loadGroupsFile() {
        if (GROUPS_FILE.exists()) {
            try (FileReader reader = new FileReader(GROUPS_FILE)) {
                Type type = new TypeToken<ConcurrentHashMap<String, String>>(){}.getType();
                ConcurrentHashMap<String, String> loaded = GSON.fromJson(reader, type);
                if (loaded != null) groups = loaded;
            } catch (JsonSyntaxException | IOException e) {
                LOGGER.warn("Поврежден groups.json. Инициализация чистой базы.", e);
                groups = new ConcurrentHashMap<>();
                saveGroups();
            }
        } else {
            saveGroups();
        }
    }

    private static void loadMacrosFile() {
        if (MACROS_FILE.exists()) {
            try (FileReader reader = new FileReader(MACROS_FILE)) {
                Type type = new TypeToken<ConcurrentHashMap<String, String>>(){}.getType();
                ConcurrentHashMap<String, String> loaded = GSON.fromJson(reader, type);
                if (loaded != null) macros = loaded;
            } catch (JsonSyntaxException | IOException e) {
                LOGGER.warn("Поврежден macros.json. Инициализация чистой базы.", e);
                macros = new ConcurrentHashMap<>();
                saveMacros();
            }
        } else {
            saveMacros();
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            JsonObject json = new JsonObject();
            json.addProperty("delayTicks", delayTicks);
            json.addProperty("showHud", showHud);
            GSON.toJson(json, writer);
        } catch (IOException e) {
            LOGGER.error("Ошибка сохранения config.json", e);
        }
    }

    public static void saveGroups() {
        try (FileWriter writer = new FileWriter(GROUPS_FILE)) {
            GSON.toJson(groups, writer);
        } catch (IOException e) {
            LOGGER.error("Ошибка сохранения groups.json", e);
        }
    }

    public static void saveMacros() {
        try (FileWriter writer = new FileWriter(MACROS_FILE)) {
            GSON.toJson(macros, writer);
        } catch (IOException e) {
            LOGGER.error("Ошибка сохранения macros.json", e);
        }
    }
}