// Файл: src/client/java/com/multicmd/start/client/gui/MultiCmdScreen.java
package com.multicmd.start.client.gui;

import com.multicmd.start.client.MultiCmdClient;
import com.multicmd.start.client.config.ConfigManager;
import com.multicmd.start.client.executor.BatchExecutor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class MultiCmdScreen extends Screen {

    private final Screen parent;
    private int currentTab = 0;

    private TextFieldWidget batchInput;
    private TextFieldWidget delayInput;

    public MultiCmdScreen(Screen parent) {
        super(Text.translatable("multicmd.gui.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.clearChildren();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("multicmd.gui.tab.run"), button -> switchTab(0))
                .dimensions(centerX - 105, 10, 100, 20)
                .tooltip(Tooltip.of(Text.literal("Открыть терминал исполнителя")))
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("multicmd.gui.tab.settings"), button -> switchTab(1))
                .dimensions(centerX + 5, 10, 100, 20)
                .tooltip(Tooltip.of(Text.literal("Конфигурация параметров мода")))
                .build());

        if (currentTab == 0) {
            initRunTab(centerX, centerY);
        } else {
            initSettingsTab(centerX, centerY);
        }

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
                .dimensions(centerX - 100, this.height - 30, 200, 20).build());
    }

    private void switchTab(int tabIndex) {
        this.currentTab = tabIndex;
        if (this.client != null) {
            this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        this.init();
    }

    private void initRunTab(int centerX, int centerY) {
        this.batchInput = new TextFieldWidget(this.textRenderer, centerX - 150, centerY - 20, 300, 20, Text.literal(""));
        this.batchInput.setMaxLength(1024);
        this.batchInput.setTooltip(Tooltip.of(Text.literal("Пример: /rg addmember Base_[1-10] @Builders")));
        this.addDrawableChild(this.batchInput);
        this.setInitialFocus(this.batchInput);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("multicmd.gui.btn.execute"), button -> {
            String cmd = this.batchInput.getText();
            if (cmd != null && !cmd.trim().isEmpty()) {
                this.close();
                MultiCmdClient.getInstance().executeBatchSafe(cmd);
            }
        }).dimensions(centerX - 100, centerY + 10, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("multicmd.gui.btn.stop"), button -> {
                    if (BatchExecutor.getInstance().isActive()) {
                        BatchExecutor.getInstance().abort();
                        ToastNotification.show(Text.literal("Пул остановлен"), Text.literal("Очередь была экстренно очищена."), ToastNotification.Type.WARNING);
                    }
                }).dimensions(centerX - 100, centerY + 35, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Сбросить текущий активный пул команд")))
                .build());
    }

    private void initSettingsTab(int centerX, int centerY) {
        this.delayInput = new TextFieldWidget(this.textRenderer, centerX + 10, centerY - 20, 50, 20, Text.literal(""));
        this.delayInput.setMaxLength(4);
        this.delayInput.setText(String.valueOf(ConfigManager.delayTicks));
        this.delayInput.setTooltip(Tooltip.of(Text.literal("Задержка между пакетами. Рекомендуется: 10 тиков.")));
        this.addDrawableChild(this.delayInput);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("multicmd.gui.btn.save_delay"), button -> {
            try {
                int delay = Integer.parseInt(this.delayInput.getText().trim());
                ConfigManager.delayTicks = Math.max(1, delay);
                ConfigManager.saveConfig();
                ToastNotification.show(Text.literal("Конфиг сохранен"), Text.literal("Задержка: " + ConfigManager.delayTicks + " тиков"), ToastNotification.Type.SUCCESS);
            } catch (NumberFormatException e) {
                this.delayInput.setText(String.valueOf(ConfigManager.delayTicks));
                ToastNotification.show(Text.literal("Ошибка Валидации"), Text.literal("Требуется целочисленное значение."), ToastNotification.Type.ERROR);
            }
        }).dimensions(centerX + 65, centerY - 20, 80, 20).build());

        Text hudText = Text.translatable("multicmd.gui.hud", ConfigManager.showHud ? "§aВКЛ" : "§cВЫКЛ");
        this.addDrawableChild(ButtonWidget.builder(hudText, button -> {
                    ConfigManager.showHud = !ConfigManager.showHud;
                    ConfigManager.saveConfig();
                    this.switchTab(1);
                }).dimensions(centerX - 100, centerY + 10, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Отображение прогресс-бара поверх экрана")))
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // ФИКС БАГА 1: Используем яркий желтый цвет для заголовка
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 35, 0xFFFF55);

        if (currentTab == 0) {
            // ФИКС БАГА 1: Подняли текст на 5 пикселей выше и сделали белым (0xFFFFFF), чтобы он не сливался с полем
            context.drawTextWithShadow(this.textRenderer, Text.translatable("multicmd.gui.label.enter_cmd"), centerX - 150, centerY - 32, 0xFFFFFF);

            if (BatchExecutor.getInstance().isActive()) {
                String activeText = "Задач в пуле: " + BatchExecutor.getInstance().getRemaining();
                context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(activeText), centerX, centerY + 65, 0xFF5555);
            }
        } else if (currentTab == 1) {
            // ФИКС БАГА 1: Белый текст для настроек
            context.drawTextWithShadow(this.textRenderer, Text.translatable("multicmd.gui.label.delay"), centerX - 150, centerY - 14, 0xFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (currentTab == 0 && this.batchInput.isFocused()) {
                String cmd = this.batchInput.getText();
                if (!cmd.trim().isEmpty()) {
                    this.close();
                    MultiCmdClient.getInstance().executeBatchSafe(cmd);
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}