// Файл: src/client/java/com/multicmd/start/client/lua/LuaApi.java
package com.multicmd.start.client.lua;

import com.multicmd.start.client.MultiCmdClient;
import com.multicmd.start.client.executor.BatchExecutor;
import com.multicmd.start.client.util.KeyMapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.luaj.vm2.LuaTable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Изолированный, потокобезопасный интерфейс (API) для виртуальной машины Lua.
 * Предоставляет полный контроль над клиентом Minecraft.
 */
public class LuaApi {

    // ==========================================
    // БАЗОВЫЕ УТИЛИТЫ И СИНХРОНИЗАТОР ПОТОКОВ
    // ==========================================

    /**
     * Важнейший метод архитектуры. Выполняет лямбду строго в главном потоке Minecraft
     * и блокирует Lua-поток до получения результата. Гарантирует отсутствие ConcurrentModificationException.
     */
    private <T> T sync(Supplier<T> task) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            return task.get();
        } else {
            return CompletableFuture.supplyAsync(task, client).join();
        }
    }

    public void print(String message) {
        MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("[Lua] ").formatted(Formatting.AQUA).append(Text.literal(message).formatted(Formatting.WHITE)), false
                );
            }
        });
    }

    public void execute(String cmd) { MultiCmdClient.getInstance().executeBatchSafe(cmd, false); }
    public void executeSilent(String cmd) { MultiCmdClient.getInstance().executeBatchSafe(cmd, true); }
    public void queue(String cmd) { BatchExecutor.getInstance().enqueueRaw(cmd); }
    public void wait(int ticks) { BatchExecutor.getInstance().enqueueRaw("[INTERNAL_DELAY:" + ticks + "]"); }

    // ==========================================
    // ВВОД И УПРАВЛЕНИЕ (КЛИАТУРА / МЫШЬ)
    // ==========================================

    public void click(String button) { KeyMapper.click(button); }
    public void setKey(String button, boolean state) { KeyMapper.setKeyState(button, state); }

    // ==========================================
    // ИНФОРМАЦИЯ ОБ ИГРОКЕ
    // ==========================================

    public double getX() { return sync(() -> MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getX() : 0.0); }
    public double getY() { return sync(() -> MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getY() : 0.0); }
    public double getZ() { return sync(() -> MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getZ() : 0.0); }
    public float getYaw() { return sync(() -> MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getYaw() : 0.0f); }
    public float getPitch() { return sync(() -> MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getPitch() : 0.0f); }
    public float getHealth() { return sync(() -> MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getHealth() : 0.0f); }
    public int getFood() { return sync(() -> MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getHungerManager().getFoodLevel() : 0); }
    public float getAttackCooldown() { return sync(() -> MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getAttackCooldownProgress(0.0F) : 0.0f); }

    /**
     * Проверяет, использует ли игрок предмет в данный момент (ест еду, натягивает лук).
     */
    public boolean isUsingItem() {
        return sync(() -> MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.isUsingItem());
    }

    // ==========================================
    // НАВЕДЕНИЕ И КАМЕРА (АИМБОТ)
    // ==========================================

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

    /**
     * Автоматически высчитывает углы и поворачивает голову игрока на указанные координаты.
     */
    public void lookAt(double x, double y, double z) {
        MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().player != null) {
                double dx = x - MinecraftClient.getInstance().player.getX();
                double dy = y - MinecraftClient.getInstance().player.getEyeY();
                double dz = z - MinecraftClient.getInstance().player.getZ();

                double distance = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                float pitch = (float) (-(MathHelper.atan2(dy, distance) * (180.0 / Math.PI)));

                MinecraftClient.getInstance().player.setYaw(yaw);
                MinecraftClient.getInstance().player.setPitch(pitch);
            }
        });
    }

    // ==========================================
    // ИНВЕНТАРЬ (ПОИСК ЕДЫ И ОРУЖИЯ)
    // ==========================================

    public int getSlot() {
        return sync(() -> MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getInventory().selectedSlot + 1 : 1);
    }

    public void setSlot(int slot) {
        if (slot >= 1 && slot <= 9) {
            MinecraftClient.getInstance().execute(() -> {
                if (MinecraftClient.getInstance().player != null) MinecraftClient.getInstance().player.getInventory().selectedSlot = slot - 1;
            });
        }
    }

    /**
     * Ищет предмет в хотбаре (слоты 1-9) по части названия (например, "apple").
     * Возвращает номер слота (1-9) или -1, если не найдено.
     */
    public int findItemInHotbar(String keyword) {
        return sync(() -> {
            if (MinecraftClient.getInstance().player == null) return -1;
            String lowerKeyword = keyword.toLowerCase();
            for (int i = 0; i < 9; i++) {
                ItemStack stack = MinecraftClient.getInstance().player.getInventory().main.get(i);
                if (!stack.isEmpty()) {
                    String itemName = Registries.ITEM.getId(stack.getItem()).getPath().toLowerCase();
                    if (itemName.contains(lowerKeyword)) return i + 1;
                }
            }
            return -1;
        });
    }

    // ==========================================
    // ВЗАИМОДЕЙСТВИЕ С МИРОМ (РАЙКАСТИНГ И РАДАР)
    // ==========================================

    /**
     * Для Pathfinder'а: возвращает имя блока по координатам (например "minecraft:stone" или "minecraft:air").
     */
    public String getBlock(int x, int y, int z) {
        return sync(() -> {
            if (MinecraftClient.getInstance().world == null) return "unknown";
            return Registries.BLOCK.getId(MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, y, z)).getBlock()).toString();
        });
    }

    /**
     * Радар (Киллаура): Ищет ближайшую живую сущность в заданном радиусе (исключая самого игрока).
     * Возвращает таблицу Lua с параметрами цели или таблицу с {type="MISS"}.
     */
    public LuaTable getClosestEntity(double radius) {
        return sync(() -> {
            LuaTable result = new LuaTable();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) {
                result.set("type", "MISS");
                return result;
            }

            LivingEntity closest = null;
            double minDistance = radius * radius; // Оптимизация (сравниваем квадраты дистанций)

            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof LivingEntity target && entity != client.player && !target.isSpectator() && target.isAlive()) {
                    double distSq = client.player.squaredDistanceTo(target);
                    if (distSq <= minDistance) {
                        minDistance = distSq;
                        closest = target;
                    }
                }
            }

            if (closest != null) {
                result.set("type", "ENTITY");
                result.set("name", closest.getName().getString());
                result.set("x", closest.getX());
                result.set("y", closest.getY());
                result.set("z", closest.getZ());
                result.set("health", closest.getHealth());
                result.set("distance", Math.sqrt(minDistance));
            } else {
                result.set("type", "MISS");
            }
            return result;
        });
    }
}