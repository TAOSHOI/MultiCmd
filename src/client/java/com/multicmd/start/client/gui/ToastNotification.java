// Файл: src/client/java/com/multicmd/start/client/gui/ToastNotification.java
package com.multicmd.start.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Кастомная реализация всплывающих уведомлений для улучшения User Experience (UX).
 */
public class ToastNotification implements Toast {

    private final Text title;
    private final Text description;
    private final Type type;
    private long startTime;
    private boolean justUpdated;

    public enum Type {
        SUCCESS(0x55FF55),
        INFO(0x55FFFF),
        WARNING(0xFFFF55),
        ERROR(0xFF5555);

        public final int color;
        Type(int color) { this.color = color; }
    }

    public ToastNotification(Text title, Text description, Type type) {
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public static void show(Text title, Text description, Type type) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getToastManager() != null) {
            client.getToastManager().add(new ToastNotification(title, description, type));
        }
    }

    @Override
    public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }

        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, 0, 0, this.getWidth(), this.getHeight(), 256, 256);

        TextRenderer textRenderer = manager.getClient().textRenderer;
        context.drawText(textRenderer, this.title, 30, 7, this.type.color, false);

        List<OrderedText> lines = textRenderer.wrapLines(this.description, 125);
        if (lines.size() == 1) {
            context.drawText(textRenderer, lines.get(0), 30, 18, 0xFFFFFF, false);
        } else {
            for (int i = 0; i < Math.min(2, lines.size()); i++) {
                context.drawText(textRenderer, lines.get(i), 30, 18 + i * 10, 0xFFFFFF, false);
            }
        }

        return startTime - this.startTime >= 3500L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}