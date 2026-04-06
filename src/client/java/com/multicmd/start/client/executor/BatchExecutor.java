// Файл: src/client/java/com/multicmd/start/client/executor/BatchExecutor.java
package com.multicmd.start.client.executor;

import com.multicmd.start.client.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Исполнитель командной очереди. (Singleton)
 * Инкапсулирует тайминги, Anti-Spam задержки и взаимодействие с сетевым каналом игры.
 */
public class BatchExecutor {

    private static BatchExecutor instance;

    private final Queue<String> queue = new LinkedList<>();
    private int totalCommands = 0;
    private int currentTickDelay = 0;

    private BatchExecutor() {}

    public static synchronized BatchExecutor getInstance() {
        if (instance == null) {
            instance = new BatchExecutor();
        }
        return instance;
    }

    public void enqueueAndStart(List<String> commands) {
        if (commands == null || commands.isEmpty()) return;

        queue.addAll(commands);
        totalCommands = queue.size();
        currentTickDelay = 0;

        MinecraftClient.getInstance().player.sendMessage(
                Text.translatable("multicmd.batch.start", totalCommands).formatted(Formatting.YELLOW), false
        );
    }

    public void tick() {
        if (queue.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            abort(); // Защита от спама на другой сервер при дисконнекте
            return;
        }

        currentTickDelay++;
        int requiredDelay = Math.max(1, ConfigManager.delayTicks);

        if (currentTickDelay >= requiredDelay) {
            currentTickDelay = 0;
            String cmd = queue.poll();

            dispatchCommand(client, cmd);

            if (queue.isEmpty()) {
                client.player.sendMessage(Text.translatable("multicmd.batch.done").formatted(Formatting.GREEN), false);
                totalCommands = 0;
            }
        }
    }

    private void dispatchCommand(MinecraftClient client, String cmd) {
        if (client.getNetworkHandler() == null || cmd == null) return;

        try {
            if (cmd.startsWith("/")) {
                client.getNetworkHandler().sendChatCommand(cmd.substring(1));
            } else {
                client.getNetworkHandler().sendChatMessage(cmd);
            }
        } catch (Exception e) {
            ConfigManager.LOGGER.error("Сбой сети при диспетчеризации пакета чата: {}", cmd, e);
        }
    }

    public void abort() {
        queue.clear();
        totalCommands = 0;
        currentTickDelay = 0;
        ConfigManager.LOGGER.info("Внимание: Выполнение очереди экстренно остановлено.");
    }

    public boolean isActive() { return !queue.isEmpty(); }
    public int getRemaining() { return queue.size(); }
    public int getTotal() { return totalCommands; }
}