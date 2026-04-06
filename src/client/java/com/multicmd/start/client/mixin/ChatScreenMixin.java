// Файл: src/client/java/com/multicmd/start/mixin/ChatScreenMixin.java
package com.multicmd.start.mixin;

import com.multicmd.start.client.gui.MultiCmdScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Внедрение кнопки быстрого доступа к MultiCmd прямо в ванильный экран чата.
 */
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addMultiCmdButton(CallbackInfo ci) {
        int buttonWidth = 20;
        int buttonHeight = 20;

        int x = this.width - buttonWidth - 2;
        int y = this.height - 40;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§eM"), button -> {
                    if (this.client != null) {
                        this.client.setScreen(new MultiCmdScreen(this));
                    }
                }).dimensions(x, y, buttonWidth, buttonHeight)
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("key.multicmd.open_gui")))
                .build());
    }
}