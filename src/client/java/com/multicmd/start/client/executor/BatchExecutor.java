// Файл: src/client/java/com/multicmd/start/client/executor/BatchExecutor.java
package com.multicmd.start.client.executor;

import com.multicmd.start.client.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchExecutor {

    private static BatchExecutor instance;

    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger totalCommands = new AtomicInteger(0);

    private int currentTickDelay = 0;
    private int internalWaitCounter = 0;
    private boolean isSilent = false;

    private BatchExecutor() {}

    public static synchronized BatchExecutor getInstance() {
        if (instance == null) instance = new BatchExecutor();
        return instance;
    }

    public void enqueueAndStart(List<String> commands, boolean silent) {
        if (commands == null || commands.isEmpty()) return;
        this.isSilent = silent;
        queue.addAll(commands);
        totalCommands.set(queue.size());
        currentTickDelay = 0;

        if (!silent) {
            MinecraftClient.getInstance().player.sendMessage(
                    Text.translatable("multicmd.batch.start", totalCommands.get()).formatted(Formatting.YELLOW), false
            );
        }
    }

    public void enqueueRaw(String command) {
        queue.add(command);
        totalCommands.updateAndGet(current -> Math.max(current, queue.size()));
    }

    public void tick() {
        if (queue.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            abort();
            return;
        }

        String peekCmd = queue.peek();
        if (peekCmd != null && peekCmd.startsWith("[INTERNAL_DELAY:")) {
            int delayTicks = Integer.parseInt(peekCmd.substring(16, peekCmd.length() - 1));
            if (internalWaitCounter < delayTicks) {
                internalWaitCounter++;
                return;
            } else {
                queue.poll();
                internalWaitCounter = 0;
                currentTickDelay = 0;
                return;
            }
        }

        currentTickDelay++;
        int requiredDelay = Math.max(1, ConfigManager.delayTicks);

        if (currentTickDelay >= requiredDelay) {
            currentTickDelay = 0;
            String cmd = queue.poll();

            dispatchCommand(client, cmd);

            if (queue.isEmpty()) {
                if (!isSilent) {
                    client.player.sendMessage(Text.translatable("multicmd.batch.done").formatted(Formatting.GREEN), false);
                }
                totalCommands.set(0);
                isSilent = false;
            }
        }
    }

    private void dispatchCommand(MinecraftClient client, String cmd) {
        if (client.getNetworkHandler() == null || cmd == null) return;
        try {
            if (cmd.startsWith("/")) client.getNetworkHandler().sendChatCommand(cmd.substring(1));
            else client.getNetworkHandler().sendChatMessage(cmd);
        } catch (Exception e) {}
    }

    public void abort() {
        queue.clear();
        totalCommands.set(0);
        currentTickDelay = 0;
        internalWaitCounter = 0;
        isSilent = false;
    }

    public boolean isActive() { return !queue.isEmpty(); }
    public boolean isSilent() { return isSilent; }
    public int getRemaining() { return queue.size(); }
    public int getTotal() { return totalCommands.get(); }
}