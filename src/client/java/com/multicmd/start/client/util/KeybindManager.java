// Файл: src/client/java/com/multicmd/start/client/util/KeybindManager.java
package com.multicmd.start.client.util;

import com.multicmd.start.client.MultiCmdClient;
import com.multicmd.start.client.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Динамический обработчик пользовательских биндов (Нажатий клавиш).
 * Проверяет состояние клавиатуры каждый тик, предотвращая спам команд (защита от зажатия).
 */
public class KeybindManager {

    // Хранит состояние клавиши в предыдущем тике (чтобы команда срабатывала 1 раз за нажатие)
    private static final Map<Integer, Boolean> keyStates = new HashMap<>();

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.currentScreen != null) return; // Не реагируем, если открыт чат или инвентарь

        long windowHandle = client.getWindow().getHandle();

        for (Map.Entry<String, String> entry : ConfigManager.keybinds.entrySet()) {
            String keyName = entry.getKey();
            String commandToExecute = entry.getValue();

            try {
                // Преобразуем букву (например, "O") в LWJGL KeyCode
                InputUtil.Key key = InputUtil.fromTranslationKey("key.keyboard." + keyName.toLowerCase());
                int keyCode = key.getCode();

                if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) continue;

                boolean isPressed = InputUtil.isKeyPressed(windowHandle, keyCode);
                boolean wasPressed = keyStates.getOrDefault(keyCode, false);

                // Если клавиша была только что нажата (Transition from False to True)
                if (isPressed && !wasPressed) {
                    if (commandToExecute.startsWith("/lua run ")) {
                        // Спец-обработка для Lua, чтобы не спамить в чат
                        String script = commandToExecute.replace("/lua run ", "").trim();
                        com.multicmd.start.client.lua.LuaEngine.runScriptAsync(script);
                    } else if (commandToExecute.startsWith("/batch ")) {
                        MultiCmdClient.getInstance().executeBatchSafe(commandToExecute.replace("/batch ", ""));
                    } else {
                        // Если это просто команда (например /say 1)
                        MultiCmdClient.getInstance().executeBatchSafe(commandToExecute);
                    }
                }

                keyStates.put(keyCode, isPressed);
            } catch (Exception e) {
                // Игнорируем невалидные клавиши
            }
        }
    }
}