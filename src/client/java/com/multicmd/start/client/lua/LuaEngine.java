// Файл: src/client/java/com/multicmd/start/client/lua/LuaEngine.java
package com.multicmd.start.client.lua;

import com.multicmd.start.client.config.ConfigManager;
import com.multicmd.start.client.gui.ToastNotification;
import net.minecraft.text.Text;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Виртуальная машина Lua с поддержкой регистрации Демонов.
 */
public class LuaEngine {

    public static void runScriptAsync(String scriptName) {
        if (!scriptName.endsWith(".lua")) scriptName += ".lua";

        File scriptFile = ConfigManager.SCRIPTS_DIR.resolve(scriptName).toFile();
        if (!scriptFile.exists()) {
            ToastNotification.show(Text.literal("Сбой Lua"), Text.literal("Файл не найден: " + scriptFile.getName()), ToastNotification.Type.ERROR);
            return;
        }

        final String finalName = scriptName.replace(".lua", "");
        CompletableFuture.runAsync(() -> {
            try {
                Globals globals = JsePlatform.standardGlobals();
                globals.set("api", CoerceJavaToLua.coerce(new LuaApi()));

                String code = Files.readString(scriptFile.toPath());
                LuaValue chunk = globals.load(code);
                chunk.call(); // Выполнение основного тела

                // Проверка на наличие Event Listeners (Перевод в режим Демона)
                LuaValue onTick = globals.get("onTick");
                LuaValue onChat = globals.get("onChat");
                LuaValue onHealthDrop = globals.get("onHealthDrop");

                if (onTick.isfunction() || onChat.isfunction() || onHealthDrop.isfunction()) {
                    LuaEventManager.registerDaemon(
                            finalName,
                            onTick.isfunction() ? (LuaFunction) onTick : null,
                            onChat.isfunction() ? (LuaFunction) onChat : null,
                            onHealthDrop.isfunction() ? (LuaFunction) onHealthDrop : null
                    );
                    ToastNotification.show(Text.literal("Демон запущен"), Text.literal("Скрипт " + finalName + " работает в фоне."), ToastNotification.Type.INFO);
                } else {
                    ToastNotification.show(Text.literal("Скрипт Завершен"), Text.literal("Успешно: " + finalName), ToastNotification.Type.SUCCESS);
                }

            } catch (LuaError e) {
                ToastNotification.show(Text.literal("Ошибка Lua"), Text.literal(e.getMessage()), ToastNotification.Type.ERROR);
            } catch (Exception e) {}
        });
    }

    public static String[] getAvailableScripts() {
        try (Stream<Path> paths = Files.walk(ConfigManager.SCRIPTS_DIR, 1)) {
            return paths.filter(Files::isRegularFile).map(Path::getFileName)
                    .map(Path::toString).filter(name -> name.endsWith(".lua")).toArray(String[]::new);
        } catch (Exception e) { return new String[0]; }
    }
}