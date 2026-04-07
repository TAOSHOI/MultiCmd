// Файл: src/client/java/com/multicmd/start/mixin/ChatMessageMixin.java
package com.multicmd.start.mixin;

import com.multicmd.start.client.config.ConfigManager;
import com.multicmd.start.client.gui.MultiCmdScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Перехватчик сообщений чата (Умный Алиас).
 * Заменяет засоряющую интерфейс кнопку M на красивое и бесшовное решение.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ChatMessageMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String content, CallbackInfo ci) {
        if (content.trim().equalsIgnoreCase(ConfigManager.guiAlias)) {
            ci.cancel();
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> client.setScreen(new MultiCmdScreen(null)));
        }
    }
}