// Файл: src/client/java/com/multicmd/start/client/MultiCmdClient.java
package com.multicmd.start.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.multicmd.start.client.config.ConfigManager;
import com.multicmd.start.client.executor.BatchExecutor;
import com.multicmd.start.client.gui.MultiCmdScreen;
import com.multicmd.start.client.gui.ToastNotification;
import com.multicmd.start.client.parser.CommandParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Точка входа в клиентский мод. Регистрация событий и команд.
 */
public class MultiCmdClient implements ClientModInitializer {

	private static MultiCmdClient instance;
	private KeyBinding guiKeyBind;
	private final CommandParser parser;

	public MultiCmdClient() {
		this.parser = new CommandParser();
	}

	@Override
	public void onInitializeClient() {
		instance = this;
		ConfigManager.LOGGER.info("[MultiCmd] Bootstraping Client Mod...");

		ConfigManager.load();

		guiKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.multicmd.open_gui",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_U,
				"category.multicmd.keys"
		));

		registerCommands();
		registerTickHandler();
		registerHud();
		registerNetworkGuards();

		ConfigManager.LOGGER.info("[MultiCmd] Bootstrap Complete.");
	}

	public static MultiCmdClient getInstance() {
		return instance;
	}

	public void executeBatchSafe(String rawCmd) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.player == null) return;

		try {
			Map<String, String> envVars = new HashMap<>();
			envVars.put("%me%", client.player.getName().getString());
			envVars.put("%x%", String.valueOf((int) client.player.getX()));
			envVars.put("%y%", String.valueOf((int) client.player.getY()));
			envVars.put("%z%", String.valueOf((int) client.player.getZ()));

			List<String> compiledCommands = parser.parse(
					rawCmd, envVars, ConfigManager.groups, ConfigManager.macros
			);

			BatchExecutor.getInstance().enqueueAndStart(compiledCommands);

		} catch (IllegalArgumentException ex) {
			ToastNotification.show(Text.literal("Ошибка Макроса"), Text.literal(ex.getMessage()), ToastNotification.Type.ERROR);
		} catch (CommandParser.ParserSecurityException ex) {
			ToastNotification.show(Text.literal("Блокировка Безопасности"), Text.literal(ex.getMessage()), ToastNotification.Type.ERROR);
		} catch (Exception ex) {
			ToastNotification.show(Text.literal("Критический Сбой"), Text.literal("Ошибка компиляции синтаксиса."), ToastNotification.Type.ERROR);
			ConfigManager.LOGGER.error("Необработанное исключение парсера", ex);
		}
	}

	private void registerNetworkGuards() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (BatchExecutor.getInstance().isActive()) {
				BatchExecutor.getInstance().abort();
			}
		});
	}

	private void registerTickHandler() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client == null || client.player == null) return;

			while (guiKeyBind.wasPressed()) {
				client.setScreen(new MultiCmdScreen(client.currentScreen));
			}

			BatchExecutor.getInstance().tick();
		});
	}

	private void registerHud() {
		HudRenderCallback.EVENT.register((drawContext, tickCounterObj) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			BatchExecutor executor = BatchExecutor.getInstance();

			if (!ConfigManager.showHud || !executor.isActive() || client.textRenderer == null || client.getWindow() == null) {
				return;
			}

			try {
				int width = client.getWindow().getScaledWidth();
				int height = client.getWindow().getScaledHeight();

				int total = executor.getTotal();
				int done = total - executor.getRemaining();
				int safeDelay = Math.max(1, ConfigManager.delayTicks);
				int secondsLeft = (executor.getRemaining() * safeDelay) / 20;

				String text = Text.translatable("multicmd.hud.progress", done, total, secondsLeft).getString();
				int textWidth = client.textRenderer.getWidth(text);
				int x = (width - textWidth) / 2;
				int y = height - 70;

				drawContext.fill(x - 5, y - 5, x + textWidth + 5, y + 15, 0x80000000);
				drawContext.drawText(client.textRenderer, text, x, y, 0xFFFF55, true);

				float progress = total > 0 ? (float) done / total : 0;
				int barWidth = (int) (textWidth * progress);
				drawContext.fill(x, y + 12, x + barWidth, y + 14, 0xFF55FF55);
			} catch (Exception e) {}
		});
	}

	private void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

			dispatcher.register(ClientCommandManager.literal("batch")
					.then(ClientCommandManager.argument("command", StringArgumentType.greedyString())
							.executes(context -> {
								executeBatchSafe(StringArgumentType.getString(context, "command"));
								return 1;
							})
					)
			);

			dispatcher.register(ClientCommandManager.literal("multicmd")
					.then(ClientCommandManager.literal("cancel").executes(context -> {
						BatchExecutor.getInstance().abort();
						ToastNotification.show(Text.literal("Пакет остановлен"), Text.translatable("multicmd.command.cancel"), ToastNotification.Type.WARNING);
						return 1;
					}))
			);

			dispatcher.register(ClientCommandManager.literal("group")
					.then(ClientCommandManager.literal("add").then(ClientCommandManager.argument("name", StringArgumentType.word()).then(ClientCommandManager.argument("members", StringArgumentType.greedyString()).executes(context -> {
						String name = StringArgumentType.getString(context, "name");
						ConfigManager.groups.put(name, StringArgumentType.getString(context, "members").replace(" ", "")); ConfigManager.saveGroups();
						ToastNotification.show(Text.literal("Группа добавлена"), Text.literal("@" + name), ToastNotification.Type.SUCCESS); return 1;
					}))))
					.then(ClientCommandManager.literal("remove").then(ClientCommandManager.argument("name", StringArgumentType.word()).executes(context -> {
						String name = StringArgumentType.getString(context, "name");
						if (ConfigManager.groups.remove(name) != null) { ConfigManager.saveGroups(); ToastNotification.show(Text.literal("Группа удалена"), Text.literal("@" + name), ToastNotification.Type.INFO); }
						else context.getSource().sendFeedback(Text.translatable("multicmd.group.not_found", name).formatted(Formatting.RED)); return 1;
					})))
					.then(ClientCommandManager.literal("list").executes(context -> {
						if (ConfigManager.groups.isEmpty()) context.getSource().sendFeedback(Text.translatable("multicmd.group.list.empty").formatted(Formatting.YELLOW));
						else { context.getSource().sendFeedback(Text.translatable("multicmd.group.list.header").formatted(Formatting.GOLD)); ConfigManager.groups.forEach((n, m) -> context.getSource().sendFeedback(Text.literal("- @" + n + ": " + m).formatted(Formatting.GRAY))); } return 1;
					}))
			);

			dispatcher.register(ClientCommandManager.literal("macro")
					.then(ClientCommandManager.literal("add").then(ClientCommandManager.argument("name", StringArgumentType.word()).then(ClientCommandManager.argument("cmd", StringArgumentType.greedyString()).executes(context -> {
						String name = StringArgumentType.getString(context, "name");
						ConfigManager.macros.put(name, StringArgumentType.getString(context, "cmd")); ConfigManager.saveMacros();
						ToastNotification.show(Text.literal("Макрос добавлен"), Text.literal("#" + name), ToastNotification.Type.SUCCESS); return 1;
					}))))
					.then(ClientCommandManager.literal("remove").then(ClientCommandManager.argument("name", StringArgumentType.word()).executes(context -> {
						String name = StringArgumentType.getString(context, "name");
						if (ConfigManager.macros.remove(name) != null) { ConfigManager.saveMacros(); ToastNotification.show(Text.literal("Макрос удален"), Text.literal("#" + name), ToastNotification.Type.INFO); }
						else context.getSource().sendFeedback(Text.translatable("multicmd.macro.not_found", name).formatted(Formatting.RED)); return 1;
					})))
					.then(ClientCommandManager.literal("list").executes(context -> {
						if (ConfigManager.macros.isEmpty()) context.getSource().sendFeedback(Text.translatable("multicmd.macro.list.empty").formatted(Formatting.YELLOW));
						else { context.getSource().sendFeedback(Text.translatable("multicmd.macro.list.header").formatted(Formatting.GOLD)); ConfigManager.macros.forEach((n, c) -> context.getSource().sendFeedback(Text.literal("- #" + n + " -> " + c).formatted(Formatting.GRAY))); } return 1;
					}))
			);
		});
	}
}