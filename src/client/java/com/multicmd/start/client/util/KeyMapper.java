// Файл: src/client/java/com/multicmd/start/client/util/KeyMapper.java
package com.multicmd.start.client.util;

import net.fabricmc.fabric.mixin.client.keybinding.KeyBindingAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * Потокобезопасная утилита для симуляции физического ввода игрока.
 */
public class KeyMapper {

    public static void setKeyState(String keyName, boolean pressed) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            KeyBinding binding = getBinding(client, keyName.toLowerCase());
            if (binding != null) {
                binding.setPressed(pressed);
            }
        });
    }

    public static void click(String keyName) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            KeyBinding binding = getBinding(client, keyName.toLowerCase());
            if (binding != null) {
                InputUtil.Key boundKey = ((KeyBindingAccessor) binding).fabric_getBoundKey();
                KeyBinding.onKeyPressed(boundKey);
            }
        });
    }

    private static KeyBinding getBinding(MinecraftClient client, String name) {
        return switch (name) {
            case "w", "forward" -> client.options.forwardKey;
            case "s", "back" -> client.options.backKey;
            case "a", "left" -> client.options.leftKey;
            case "d", "right" -> client.options.rightKey;
            case "space", "jump" -> client.options.jumpKey;
            case "shift", "sneak" -> client.options.sneakKey;
            case "m1", "attack" -> client.options.attackKey;
            case "m2", "use" -> client.options.useKey;
            default -> null;
        };
    }
}