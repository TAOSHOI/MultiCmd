// Файл: src/client/java/com/multicmd/start/client/lua/LuaEventManager.java
package com.multicmd.start.client.lua;

import com.multicmd.start.client.config.ConfigManager;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер фоновых демонов. Управляет подписками Lua-скриптов на события игры.
 */
public class LuaEventManager {

    public static final Map<String, LuaDaemon> DAEMONS = new ConcurrentHashMap<>();

    public static class LuaDaemon {
        public final String name;
        public final LuaFunction onTick;
        public final LuaFunction onChat;
        public final LuaFunction onHealthDrop;

        private volatile boolean isExecutingTick = false;
        private float lastHealth = -1;

        public LuaDaemon(String name, LuaFunction onTick, LuaFunction onChat, LuaFunction onHealthDrop) {
            this.name = name;
            this.onTick = onTick;
            this.onChat = onChat;
            this.onHealthDrop = onHealthDrop;
        }

        public void triggerTick(float currentHealth) {
            // Обработка падения здоровья
            if (onHealthDrop != null && lastHealth != -1 && currentHealth < lastHealth) {
                CompletableFuture.runAsync(() -> {
                    try { onHealthDrop.call(LuaValue.valueOf(currentHealth)); } catch (Exception e) {}
                });
            }
            lastHealth = currentHealth;

            // Обработка тиков (с защитой от наслаивания потоков)
            if (onTick != null && !isExecutingTick) {
                isExecutingTick = true;
                CompletableFuture.runAsync(() -> {
                    try { onTick.call(); }
                    catch (Exception e) { ConfigManager.LOGGER.error("Daemon Tick Error", e); }
                    finally { isExecutingTick = false; }
                });
            }
        }

        public void triggerChat(String message) {
            if (onChat != null) {
                CompletableFuture.runAsync(() -> {
                    try { onChat.call(LuaValue.valueOf(message)); } catch (Exception e) {}
                });
            }
        }
    }

    public static void registerDaemon(String scriptName, LuaFunction onTick, LuaFunction onChat, LuaFunction onHealthDrop) {
        DAEMONS.put(scriptName, new LuaDaemon(scriptName, onTick, onChat, onHealthDrop));
    }

    public static void stopDaemon(String scriptName) {
        DAEMONS.remove(scriptName);
    }

    public static void stopAll() {
        DAEMONS.clear();
    }
}