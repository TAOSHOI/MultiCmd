// Файл: src/client/java/com/multicmd/start/mixin/ChatScreenMixin.java
package com.multicmd.start.mixin;

import com.multicmd.start.client.gui.MultiCmdScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    /**
     * Статический внутренний класс кнопки, который специально "тупой"
     * для системы навигации (игнорирует фокус).
     */
    private static class MultiCmdButton extends ButtonWidget {
        public MultiCmdButton(int x, int y, int width, int height, Text message, PressAction onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        public boolean isNarratable() {
            return false;
        }

        @Override
        public void setFocused(boolean focused) {
            // Игнорируем попытки сфокусироваться через Tab или стрелки
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addMultiCmdButton(CallbackInfo ci) {
        int buttonWidth = 20;
        int buttonHeight = 20;

        int x = this.width - buttonWidth - 2;
        int y = this.height - 40;

        MultiCmdButton multiCmdButton = new MultiCmdButton(x, y, buttonWidth, buttonHeight, Text.literal("§eM"), button -> {
            if (this.client != null) {
                this.client.setScreen(new MultiCmdScreen(this));
            }
        });

        multiCmdButton.setTooltip(Tooltip.of(Text.translatable("key.multicmd.open_gui")));
        this.addDrawableChild(multiCmdButton);
    }
}