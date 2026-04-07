// Файл: src/client/java/com/multicmd/start/client/lua/LuaApi.java
package com.multicmd.start.client.lua;

import com.multicmd.start.client.MultiCmdClient;
import com.multicmd.start.client.config.ConfigManager;
import com.multicmd.start.client.executor.BatchExecutor;
import com.multicmd.start.client.util.KeyMapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.luaj.vm2.LuaTable;

import java.util.concurrent.CompletableFuture;

/**
 * Глобальный объект API для Lua.
 */
public class LuaApi {

    public void print(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.execute(() -> client.player.sendMessage(
                    Text.literal("[Lua] ").formatted(Formatting.AQUA).append(Text.literal(message).formatted(Formatting.WHITE)), false
            ));
        }
    }

    public void execute(String cmd) {
        MultiCmdClient.getInstance().executeBatchSafe(cmd, false);
    }

    public void executeSilent(String cmd) {
        MultiCmdClient.getInstance().executeBatchSafe(cmd, true);
    }

    /**
     * Мгновенное выполнение команды на сервере. Обходит очередь (очередь не останавливается).
     * Идеально для мгновенных системных команд или разворотов.
     */
    public void executeInstant(String cmd) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.getNetworkHandler() != null && cmd != null) {
                if (cmd.startsWith("/")) client.getNetworkHandler().sendChatCommand(cmd.substring(1));
                else client.getNetworkHandler().sendChatMessage(cmd);
            }
        });
    }

    public void queue(String cmd) { BatchExecutor.getInstance().enqueueRaw(cmd); }
    public void wait(int ticks) { BatchExecutor.getInstance().enqueueRaw("[INTERNAL_DELAY:" + ticks + "]"); }

    public void click(String button) { KeyMapper.click(button); }
    public void setKey(String button, boolean state) { KeyMapper.setKeyState(button, state); }

    public double getX() { return safeGet(c -> c.player.getX(), 0.0); }
    public double getY() { return safeGet(c -> c.player.getY(), 0.0); }
    public double getZ() { return safeGet(c -> c.player.getZ(), 0.0); }
    public float getYaw() { return safeGet(c -> c.player.getYaw(), 0.0f); }
    public float getPitch() { return safeGet(c -> c.player.getPitch(), 0.0f); }
    public float getHealth() { return safeGet(c -> c.player.getHealth(), 0.0f); }
    public int getFood() { return safeGet(c -> c.player.getHungerManager().getFoodLevel(), 0); }
    public float getAttackCooldown() { return safeGet(c -> c.player.getAttackCooldownProgress(0.0F), 0.0f); }

    public void setYaw(float yaw) {
        MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().player != null) MinecraftClient.getInstance().player.setYaw(yaw);
        });
    }

    public void setPitch(float pitch) {
        MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().player != null) MinecraftClient.getInstance().player.setPitch(pitch);
        });
    }

    public int getSlot() { return safeGet(c -> c.player.getInventory().selectedSlot + 1, 1); }

    public void setSlot(int slot) {
        if (slot >= 1 && slot <= 9) {
            MinecraftClient.getInstance().execute(() -> {
                if (MinecraftClient.getInstance().player != null) MinecraftClient.getInstance().player.getInventory().selectedSlot = slot - 1;
            });
        }
    }

    public int getItemCount(String itemId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        MinecraftClient.getInstance().execute(() -> {
            int count = 0;
            if (MinecraftClient.getInstance().player != null) {
                Item targetItem = Registries.ITEM.get(Identifier.of(itemId));
                for (ItemStack stack : MinecraftClient.getInstance().player.getInventory().main) {
                    if (stack.isOf(targetItem)) count += stack.getCount();
                }
            }
            future.complete(count);
        });
        return future.join();
    }

    public LuaTable raycast() {
        CompletableFuture<LuaTable> future = new CompletableFuture<>();
        MinecraftClient.getInstance().execute(() -> {
            LuaTable table = new LuaTable();
            HitResult hit = MinecraftClient.getInstance().crosshairTarget;
            if (hit != null) {
                table.set("type", hit.getType().name());
                if (hit instanceof BlockHitResult bh) {
                    table.set("x", bh.getBlockPos().getX());
                    table.set("y", bh.getBlockPos().getY());
                    table.set("z", bh.getBlockPos().getZ());
                } else if (hit instanceof EntityHitResult eh) {
                    table.set("name", eh.getEntity().getName().getString());
                    if (eh.getEntity() instanceof LivingEntity le) {
                        table.set("health", le.getHealth());
                    }
                }
            } else {
                table.set("type", "MISS");
            }
            future.complete(table);
        });
        return future.join();
    }

    public LuaTable getEntitiesInRadius(double radius) {
        CompletableFuture<LuaTable> future = new CompletableFuture<>();
        MinecraftClient.getInstance().execute(() -> {
            LuaTable resultTable = new LuaTable();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.world != null) {
                int index = 1;
                for (Entity entity : client.world.getEntities()) {
                    if (entity != client.player && client.player.distanceTo(entity) <= radius) {
                        LuaTable entTable = new LuaTable();
                        entTable.set("name", entity.getName().getString());
                        entTable.set("x", entity.getX());
                        entTable.set("y", entity.getY());
                        entTable.set("z", entity.getZ());
                        if (entity instanceof LivingEntity le) {
                            entTable.set("health", le.getHealth());
                        }
                        resultTable.set(index++, entTable);
                    }
                }
            }
            future.complete(resultTable);
        });
        return future.join();
    }

    private <T> T safeGet(java.util.function.Function<MinecraftClient, T> getter, T fallback) {
        CompletableFuture<T> future = new CompletableFuture<>();
        MinecraftClient.getInstance().execute(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) future.complete(getter.apply(client));
            else future.complete(fallback);
        });
        return future.join();
    }
}