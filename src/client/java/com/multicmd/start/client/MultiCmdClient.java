// Файл: src/client/java/com/multicmd/start/client/MultiCmdClient.java
package com.multicmd.start.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.multicmd.start.client.config.ConfigManager;
import com.multicmd.start.client.executor.BatchExecutor;
import com.multicmd.start.client.gui.MultiCmdScreen;
import com.multicmd.start.client.gui.ToastNotification;
import com.multicmd.start.client.lua.LuaEngine;
import com.multicmd.start.client.lua.LuaEventManager;
import com.multicmd.start.client.parser.CommandParser;
import com.multicmd.start.client.util.KeybindManager;
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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инициализатор мода с полнофункциональным движком ботов, биндов и макросов.
 */
public class MultiCmdClient implements ClientModInitializer {

	private static MultiCmdClient instance;
	private final CommandParser parser;
	private KeyBinding guiKeyBind;

	public MultiCmdClient() {
		this.parser = new CommandParser();
	}

	@Override
	public void onInitializeClient() {
		instance = this;
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
	}

	public static MultiCmdClient getInstance() {
		return instance;
	}

	private void printInteractiveHelp() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.player == null) return;
		var p = client.player;

		p.sendMessage(Text.translatable("multicmd.help.header"), false);
		p.sendMessage(Text.translatable("multicmd.help.syntax.title"), false);
		p.sendMessage(Text.translatable("multicmd.help.syntax.range")
				.styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Пример:\n/batch /say [1-3]\nВыведет:\n1\n2\n3").formatted(Formatting.GREEN)))), false);
		p.sendMessage(Text.translatable("multicmd.help.syntax.list")
				.styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Пример:\n/batch /heal {Steve,Alex}\nВыведет:\n/heal Steve\n/heal Alex").formatted(Formatting.GREEN)))), false);
		p.sendMessage(Text.translatable("multicmd.help.syntax.random")
				.styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Пример:\n/batch /give ?{A,B} apple\nВыдаст яблоко ТОЛЬКО одному!").formatted(Formatting.GREEN)))), false);
		p.sendMessage(Text.translatable("multicmd.help.syntax.env"), false);

		p.sendMessage(Text.translatable("multicmd.help.features.title"), false);
		p.sendMessage(Text.translatable("multicmd.help.features.group")
				.styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/group "))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Управление группами").formatted(Formatting.YELLOW)))), false);
		p.sendMessage(Text.translatable("multicmd.help.features.macro")
				.styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/macro "))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Управление макросами и биндами").formatted(Formatting.YELLOW)))), false);
		p.sendMessage(Text.translatable("multicmd.help.features.cancel")
				.styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/multicmd cancel"))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Мгновенный стоп!").formatted(Formatting.RED)))), false);

		p.sendMessage(Text.translatable("multicmd.help.gui", ConfigManager.guiAlias), false);

		p.sendMessage(Text.translatable("multicmd.help.lua.title"), false);
		p.sendMessage(Text.translatable("multicmd.help.lua.run")
				.styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lua run "))), false);
		p.sendMessage(Text.translatable("multicmd.help.lua.api"), false);
		p.sendMessage(Text.literal("§7• Бинды: §f/macro bind <Клавиша> <Команда>")
				.styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/macro bind "))), false);

		p.sendMessage(Text.translatable("multicmd.help.footer")
				.styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/TAOSHOI/MultiCmd"))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Открыть документацию").formatted(Formatting.AQUA)))), false);
	}

	public void executeBatchSafe(String rawCmd) { executeBatchSafe(rawCmd, false); }

	public void executeBatchSafe(String rawCmd, boolean silent) {
		if (rawCmd.trim().equalsIgnoreCase("help")) {
			printInteractiveHelp();
			return;
		}

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

			BatchExecutor.getInstance().enqueueAndStart(compiledCommands, silent);

		} catch (IllegalArgumentException ex) {
			ToastNotification.show(Text.literal("Ошибка Макроса"), Text.literal(ex.getMessage()), ToastNotification.Type.ERROR);
		} catch (CommandParser.ParserSecurityException ex) {
			ToastNotification.show(Text.literal("Блокировка Безопасности"), Text.literal(ex.getMessage()), ToastNotification.Type.ERROR);
		} catch (Exception ex) {
			ToastNotification.show(Text.literal("Критический Сбой"), Text.literal("Ошибка компиляции синтаксиса."), ToastNotification.Type.ERROR);
		}
	}

	private void registerNetworkGuards() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (BatchExecutor.getInstance().isActive()) { BatchExecutor.getInstance().abort(); }
			LuaEventManager.stopAll(); // Останавливаем всех демонов при выходе
		});
	}

	private void registerTickHandler() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client == null || client.player == null) return;

			// Опрос клавиш для открытия GUI
			while (guiKeyBind.wasPressed()) { client.setScreen(new MultiCmdScreen(client.currentScreen)); }

			// Выполнение очереди
			BatchExecutor.getInstance().tick();

			// Опрос кастомных биндов (Макросов)
			KeybindManager.tick(client);

			// Триггер Lua-демонов (onTick)
			if (!LuaEventManager.DAEMONS.isEmpty()) {
				float hp = client.player.getHealth();
				for (LuaEventManager.LuaDaemon daemon : LuaEventManager.DAEMONS.values()) {
					daemon.triggerTick(hp);
				}
			}
		});
	}

	private void registerHud() {
		HudRenderCallback.EVENT.register((drawContext, tickCounterObj) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			BatchExecutor executor = BatchExecutor.getInstance();

			// Скрываем HUD, если включен Silent режим (api:executeSilent)
			if (!ConfigManager.showHud || !executor.isActive() || executor.isSilent() || client.textRenderer == null || client.getWindow() == null) {
				return;
			}

			try {
				int width = client.getWindow().getScaledWidth();
				int height = client.getWindow().getScaledHeight();

				int total = executor.getTotal();
				int done = total - executor.getRemaining();
				int secondsLeft = (executor.getRemaining() * ConfigManager.delayTicks) / 20;

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

			dispatcher.register(ClientCommandManager.literal("lua")
					.then(ClientCommandManager.literal("run")
							.then(ClientCommandManager.argument("script", StringArgumentType.word())
									.suggests((context, builder) -> {
										String remaining = builder.getRemaining().toLowerCase();
										for (String script : LuaEngine.getAvailableScripts()) {
											if (script.toLowerCase().startsWith(remaining)) { builder.suggest(script); }
										}
										return builder.buildFuture();
									})
									.executes(context -> {
										LuaEngine.runScriptAsync(StringArgumentType.getString(context, "script"));
										return 1;
									})
							)
					)
					.then(ClientCommandManager.literal("stopall").executes(context -> {
						LuaEventManager.stopAll();
						ToastNotification.show(Text.literal("Lua Демоны"), Text.literal("Все фоновые скрипты остановлены."), ToastNotification.Type.INFO);
						return 1;
					}))
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
					.then(ClientCommandManager.literal("remove").then(ClientCommandManager.argument("name", StringArgumentType.word())
							.suggests((context, builder) -> {
								String remaining = builder.getRemaining().toLowerCase();
								ConfigManager.groups.keySet().stream().filter(k -> k.toLowerCase().startsWith(remaining)).forEach(builder::suggest);
								return builder.buildFuture();
							})
							.executes(context -> {
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
					// Команда добавления бинда: /macro bind O /lua run stun
					.then(ClientCommandManager.literal("bind").then(ClientCommandManager.argument("key", StringArgumentType.word()).then(ClientCommandManager.argument("cmd", StringArgumentType.greedyString()).executes(context -> {
						String key = StringArgumentType.getString(context, "key").toUpperCase();
						String cmd = StringArgumentType.getString(context, "cmd");
						ConfigManager.keybinds.put(key, cmd);
						ConfigManager.saveKeybinds();
						ToastNotification.show(Text.literal("Бинд Создан"), Text.literal("Кнопка [" + key + "] ➔ " + cmd), ToastNotification.Type.SUCCESS);
						return 1;
					}))))
					// Команда удаления бинда: /macro unbind O
					.then(ClientCommandManager.literal("unbind").then(ClientCommandManager.argument("key", StringArgumentType.word())
							.suggests((context, builder) -> {
								ConfigManager.keybinds.keySet().forEach(builder::suggest);
								return builder.buildFuture();
							})
							.executes(context -> {
								String key = StringArgumentType.getString(context, "key").toUpperCase();
								if (ConfigManager.keybinds.remove(key) != null) {
									ConfigManager.saveKeybinds();
									ToastNotification.show(Text.literal("Бинд Удален"), Text.literal("Кнопка [" + key + "] свободна"), ToastNotification.Type.INFO);
								} else {
									context.getSource().sendFeedback(Text.literal("§cБинд для клавиши " + key + " не найден."));
								}
								return 1;
							})))
					.then(ClientCommandManager.literal("add").then(ClientCommandManager.argument("name", StringArgumentType.word()).then(ClientCommandManager.argument("cmd", StringArgumentType.greedyString()).executes(context -> {
						String name = StringArgumentType.getString(context, "name");
						ConfigManager.macros.put(name, StringArgumentType.getString(context, "cmd")); ConfigManager.saveMacros();
						ToastNotification.show(Text.literal("Макрос добавлен"), Text.literal("#" + name), ToastNotification.Type.SUCCESS); return 1;
					}))))
					.then(ClientCommandManager.literal("remove").then(ClientCommandManager.argument("name", StringArgumentType.word())
							.suggests((context, builder) -> {
								String remaining = builder.getRemaining().toLowerCase();
								ConfigManager.macros.keySet().stream().filter(k -> k.toLowerCase().startsWith(remaining)).forEach(builder::suggest);
								return builder.buildFuture();
							})
							.executes(context -> {
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