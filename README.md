<div align="center">
  <img src="src/main/resources/assets/multicmd/icon.png" alt="MultiCmd Logo" width="128" height="128">
  <h1>🚀 MultiCmd (Fabric 1.21.1+) v1.2.0</h1>
  <p><strong>The Ultimate Batch Command, Macro & Lua Engineering Tool | Инструмент инженерии пакетных команд</strong></p>
  
  [![Fabric](https://img.shields.io/badge/Fabric-1.21.1-blue.svg)](https://fabricmc.net/)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  
  [🇷🇺 Русский](#-русский) • [🇬🇧 English](#-english)
</div>

---

## 🇬🇧 English

**MultiCmd** is a powerful client-side utility mod designed to eliminate repetitive command typing and automate routine tasks. Need to add 10 friends to 20 WorldGuard regions? Write an AFK farming script? Execute complex macros? Do it all effortlessly!

### ✨ Key Features
* **🧠 Smart Parser:** Supports numeric ranges `[1-10]`, lists `{Steve,Alex}`, and randomizers `?{A,B}`.
* **📜 Lua Scripting Engine:** Write powerful `.lua` algorithms using the built-in `api` object.
* **🛡️ SmartGuard Anti-Spam:** Commands are queued and dispatched with a configurable tick delay. Prevents getting kicked for spamming.
* **🖥️ Smart Chat Alias (GUI):** Just type `m` in chat (configurable) to open an elegant control panel without sending the message to the server!
* **🔔 Toast Notifications:** Clean, unobtrusive on-screen popups instead of chat spam.
* **👥 Groups & Macros:** Save player teams (`@Builders`) and command chains (`#claim`) locally.
* **🌍 Environment Variables:** Inject `%me%`, `%x%`, `%y%`, `%z%` into your commands dynamically.

### 📸 Media

| Control Panel (GUI) | Interactive Help |
|:---:|:---:|
| <img src="docs/gui_screenshot.png" width="400" alt="GUI"> | <img src="docs/help_preview.png" width="400" alt="Help"> |

### 📖 Syntax & Lua Examples

**1. Mass Region Claim (WorldGuard)**
```text
/batch /rg addmember Base_[1-5] {Steve, Notch}
```
*Generates 10 commands smoothly sent to the server.*

**2. Randomizer (Giveaway)**
```text
/batch /give ?{Player1, Player2, Player3} diamond 1
```
*Picks exactly ONE random player from the brackets.*

**3. Lua Scripting (`config/multicmd/scripts/farm.lua`)**
```lua
-- Simple planting algorithm with delay
for i = 1, 5 do
    api:queue("/useitem")
    api:wait(20) -- Waits 1 second (20 ticks)
end
api:execute("/batch /say Finished at X: " .. api:getX())
```
*Run it in-game using:* `/lua run farm`

### ⚙️ Installation
1. Install [Fabric Loader](https://fabricmc.net/) 0.16.5+.
2. Download **Fabric API** and **ModMenu**.
3. Drop the `MultiCmd` `.jar` into your `mods` folder.
*Note: This is a **Client-Side** mod. You do NOT need to install it on the server!*

### 📜 Commands
* `/batch <command|help>` — Execute a batch or view interactive help & Lua guide.
* `/lua run <script>` — Execute a Lua script (supports Tab-completion).
* `/multicmd cancel` — Emergency stop the command queue.
* `/group[add|remove|list]` — Manage player groups.
* `/macro [add|remove|list]` — Manage your macros.

---

## 🇷🇺 Русский

**MultiCmd** — это мощный клиентский мод, созданный для автоматизации рутины и сложных задач. Нужно добавить 10 друзей в 20 приватов WorldGuard? Написать скрипт для АФК-фермы? Сделайте это всё в пару кликов!

### ✨ Главные возможности
* **🧠 Умный Парсер:** Поддерживает диапазоны `[1-10]`, списки `{Стив,Алекс}` и случайный выбор `?{А,Б}`.
* **📜 Lua Скриптинг:** Пишите мощные алгоритмы в `.lua` файлах, используя встроенный объект `api`.
* **🛡️ SmartGuard (Анти-спам):** Команды помещаются в очередь и отправляются с настраиваемой задержкой. Сервер вас не кикнет!
* **🖥️ Умный Алиас (GUI):** Просто напишите `m` в чате (настраивается), чтобы открыть панель управления! Сообщение не отправится на сервер.
* **🔔 Всплывающие уведомления:** Чистые ванильные "тосты" вместо засорения истории чата.
* **👥 Группы и Макросы:** Локальное сохранение команд игроков (`@Builders`) и алиасов (`#claim`).
* **🌍 Переменные среды:** Используйте `%me%`, `%x%`, `%y%`, `%z%` в своих скриптах.

### 📸 Скриншоты

| Всплывающие Уведомления | Работа с прогресс-баром |
|:---:|:---:|
| <img src="docs/toast_preview.png" width="400" alt="Toasts"> | <img src="docs/gui_screenshot.png" width="400" alt="HUD Progress"> |

### 📖 Примеры синтаксиса и Lua

**1. Масштабный приват (WorldGuard)**
```text
/batch /rg addmember Base_[1-5] {Steve, Notch}
```
*Сгенерирует 10 команд и плавно отправит их на сервер.*

**2. Рулетка / Розыгрыш**
```text
/batch /give ?{Игрок1, Игрок2, Игрок3} diamond 1
```
*Выберет ровно ОДНОГО случайного игрока из скобок.*

**3. Lua Скрипт (`config/multicmd/scripts/farm.lua`)**
```lua
-- Простой алгоритм посадки с задержкой
for i = 1, 5 do
    api:queue("/useitem")
    api:wait(20) -- Ждем 1 секунду (20 тиков)
end
api:execute("/batch /say Я закончил работу на X: " .. api:getX())
```
*Запуск в игре:* `/lua run farm`

### ⚙️ Установка
1. Установите [Fabric Loader](https://fabricmc.net/) 0.16.5+.
2. Скачайте **Fabric API** и **ModMenu**.
3. Переместите `.jar` файл мода `MultiCmd` в папку `mods`.
*Примечание: Это **Клиентский** мод. Его НЕ нужно устанавливать на сервер!*

### 📜 Список команд
* `/batch <команда|help>` — Выполнить пакет или открыть интерактивный гайд (вкл. Lua).
* `/lua run <скрипт>` — Запустить Lua скрипт (поддерживает Tab-автодополнение).
* `/multicmd cancel` — Экстренная остановка очереди выполнения.
* `/group[add|remove|list]` — Управление группами игроков.
* `/macro [add|remove|list]` — Управление вашими макросами.

---
<div align="center">
  <i>Developed with ❤️ by TAOSHOI</i>
</div>
