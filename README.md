<div align="center">
  <img src="src/main/resources/assets/multicmd/icon.png" alt="MultiCmd Logo" width="128" height="128">
  <h1>🚀 MultiCmd (Fabric 1.21.1+) v1.4.0</h1>
  <p><strong>The Ultimate Batch Command, Macro & Lua Bot-Framework | Инструмент инженерии команд и ботов</strong></p>
  
  [![Fabric](https://img.shields.io/badge/Fabric-1.21.1-blue.svg)](https://fabricmc.net/)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  
  [🇷🇺 Русский](#-русский) • [🇬🇧 English](#-english)
</div>

---

## 🇬🇧 English

**MultiCmd** is a powerful client-side utility mod designed to eliminate repetitive command typing and automate complex routine tasks. It has evolved from a simple batch executor into a full-fledged **Lua Bot-Framework**. Need to add 10 friends to 20 WorldGuard regions? Write a fully autonomous AFK farming script? Bind a 180-degree camera flick to a key? Do it all effortlessly!

### ✨ Key Features
* **🧠 Smart Parser:** Supports numeric/letter ranges `[1-10]`, lists `{Steve,Alex}`, and randomizers `?{A,B}`.
* **📜 Lua Scripting & Daemons:** Write powerful `.lua` algorithms. Supports background execution (Daemons) with `onTick` and `onHealthDrop` event listeners.
* **⌨️ Dynamic Keybinds:** Bind macros, commands, or Lua scripts to any keyboard key on the fly without restarting the game.
* **🤖 Player Simulation:** The Lua API can simulate mouse clicks (`M1`/`M2`), key presses (`W`, `Space`), and precisely control the camera (`setYaw`/`setPitch`).
* **👁️ World Interaction:** Scripts can raycast blocks and scan for nearby entities with exact coordinates and HP.
* **🛡️ SmartGuard Anti-Spam:** Commands are queued and dispatched with a configurable tick delay to prevent kicks.
* **🖥️ Smart Chat Alias (GUI):** Just type `m` in chat (configurable) to open an elegant control panel without sending the message to the server!

### 📸 Media

| Control Panel (GUI) | Toast Notifications & HUD |
|:---:|:---:|
| <img src="docs/gui_screenshot.png" width="400" alt="GUI"> | <img src="docs/toast_preview.png" width="400" alt="HUD"> |

---

### 📖 Lua API Reference & Examples

MultiCmd exposes a global `api` object to your `.lua` scripts located in `config/multicmd/scripts/`.

#### Full API Methods
| Category | Methods |
| :--- | :--- |
| **Execution** | `api:execute(cmd)`, `api:executeSilent(cmd)`, `api:executeInstant(cmd)`, `api:queue(cmd)`, `api:wait(ticks)`, `api:print(msg)` |
| **Player Info** | `api:getX()`, `api:getY()`, `api:getZ()`, `api:getYaw()`, `api:getPitch()`, `api:getHealth()`, `api:getFood()`, `api:getAttackCooldown()`, `api:getName()` |
| **Camera & Input**| `api:setYaw(yaw)`, `api:setPitch(pitch)`, `api:click("m1"\|"m2")`, `api:setKey("w"\|"space", true\|false)` |
| **Inventory** | `api:getSlot()`, `api:setSlot(1-9)`, `api:getItemCount("minecraft:apple")` |
| **World (Sync)** | `api:raycast()` (returns `type`, `x`, `y`, `z`, `name`), `api:getEntitiesInRadius(radius)` (returns table of entities) |

#### Event Listeners (Background Daemons)
If your script contains these functions, it will run continuously in the background until you type `/lua stopall`.
*   `function onTick()` — Fires every client tick (20 times per second).
*   `function onHealthDrop(hp)` — Fires instantly when the player takes damage.
*   `function onChat(msg)` — Fires when a chat message is received.

#### Example 1: Tactical 180° Turn & Hit (`stun.lua`)
```lua
local originalYaw = api:getYaw()
api:setYaw(originalYaw + 180.0) -- Turn around
api:wait(5)                     -- Wait 0.25s
api:setYaw(originalYaw)         -- Turn back
api:click("m1")                 -- Strike!
```
*Bind it to a key:* `/macro bind O /lua run stun`

#### Example 2: Auto-Killer Daemon (`autokill.lua`)
```lua
function onTick()
    if api:getAttackCooldown() >= 1.0 then -- If weapon is fully charged
        local targets = api:getEntitiesInRadius(4.0)
        if targets[1] ~= nil and targets[1].type ~= "MISS" then
            api:click("m1") -- Attack nearest entity
        end
    end
end

function onHealthDrop(hp)
    if hp < 6.0 then
        api:setSlot(9) -- Switch to slot 9 (e.g., golden apple)
        api:setKey("m2", true) -- Hold right-click to eat
        api:wait(40) -- Wait 2 seconds
        api:setKey("m2", false) -- Release
    end
end
```

---

### ⚙️ Installation
1. Install [Fabric Loader](https://fabricmc.net/) 0.16.5+.
2. Download **Fabric API** and **ModMenu**.
3. Drop the `MultiCmd` `.jar` into your `mods` folder.
*Note: This is a **Client-Side** mod. Do NOT install it on the server!*

### 📜 Commands
* `/batch <command|help>` — Execute a batch or view the in-game interactive guide.
* `/lua run <script>` — Execute a Lua script.
* `/lua stopall` — Terminate all background Daemons.
* `/macro bind <key> <command>` — Bind any macro/script to a keyboard key.
* `/group [add|remove|list]` — Manage player groups.
* `/multicmd cancel` — Emergency stop the command queue.

---

## 🇷🇺 Русский

**MultiCmd** — это мощный клиентский мод, который эволюционировал из простого распаковщика команд в **полноценный фреймворк для написания ботов**. Нужно заприватить 20 регионов? Написать автономный скрипт для АФК-фермы с поиском мобов? Привязать сложный разворот камеры к одной кнопке? MultiCmd сделает это легко!

### ✨ Главные возможности
* **🧠 Умный Парсер:** Поддерживает диапазоны `[1-10]`, `[a-z]`, списки `{Стив,Алекс}` и случайный выбор `?{А,Б}`.
* **📜 Lua Скриптинг и Демоны:** Пишите мощные алгоритмы. Скрипты могут работать в фоне (режим Демона), реагируя на тики игры (`onTick`) и урон (`onHealthDrop`).
* **⌨️ Динамические Бинды:** Привязывайте скрипты или макросы к любым клавишам прямо во время игры.
* **🤖 Симуляция Игрока:** Lua API умеет нажимать кнопки мыши (`M1/M2`), зажимать клавиши клавиатуры (`W`, `Space`) и вращать камеру с точностью до градуса.
* **👁️ Взаимодействие с миром:** Скрипты могут сканировать блоки, на которые вы смотрите (Raycast), и получать точные координаты и здоровье сущностей вокруг.
* **🛡️ SmartGuard:** Защита от кика за спам благодаря умной настраиваемой очереди.
* **🖥️ Умный Алиас (GUI):** Просто напишите `m` в чате, чтобы открыть панель управления!

---

### 📖 Lua API и Примеры

MultiCmd предоставляет глобальный объект `api` для скриптов в `config/multicmd/scripts/`.

#### Методы API
| Категория | Методы |
| :--- | :--- |
| **Выполнение** | `api:execute(cmd)`, `api:executeSilent(cmd)`, `api:executeInstant(cmd)`, `api:queue(cmd)`, `api:wait(ticks)`, `api:print(msg)` |
| **Инфо игрока** | `api:getX()`, `api:getY()`, `api:getZ()`, `api:getYaw()`, `api:getPitch()`, `api:getHealth()`, `api:getFood()`, `api:getAttackCooldown()`, `api:getName()` |
| **Камера и Ввод**| `api:setYaw(yaw)`, `api:setPitch(pitch)`, `api:click("m1"\|"m2")`, `api:setKey("w"\|"space", true\|false)` |
| **Инвентарь** | `api:getSlot()`, `api:setSlot(1-9)`, `api:getItemCount("minecraft:apple")` |
| **Мир (Синхронно)** | `api:raycast()` (возвращает `type`, `x`, `y`, `z`, `name`), `api:getEntitiesInRadius(radius)` (список сущностей) |

#### Фоновые События (Демоны)
Если в скрипте есть эти функции, он становится "Демоном" и работает вечно, пока вы не напишете `/lua stopall`.
*   `function onTick()` — Срабатывает каждый тик игры (20 раз в сек).
*   `function onHealthDrop(hp)` — Срабатывает моментально при получении урона.
*   `function onChat(msg)` — Чтение входящего чата.

#### Пример 1: Тактический разворот и удар (`stun.lua`)
```lua
local originalYaw = api:getYaw()
api:setYaw(originalYaw + 180.0) -- Разворот
api:wait(5)                     -- Ждем 0.25 сек
api:setYaw(originalYaw)         -- Возвращаем камеру
api:click("m1")                 -- Удар!
```
*Забиндить в игре:* `/macro bind O /lua run stun`

#### Пример 2: Авто-Фармилка / Киллаура (`autokill.lua`)
```lua
function onTick()
    if api:getAttackCooldown() >= 1.0 then -- Оружие заряжено
        local targets = api:getEntitiesInRadius(4.0)
        if targets[1] ~= nil and targets[1].type ~= "MISS" then
            api:click("m1") -- Бьем ближайшую цель
        end
    end
end

function onHealthDrop(hp)
    if hp < 6.0 then
        api:setSlot(9) -- Выбираем слот 9 (например, яблоко)
        api:setKey("m2", true) -- Зажимаем правую кнопку мыши
        api:wait(40) -- Ждем 2 секунды
        api:setKey("m2", false) -- Отпускаем кнопку
    end
end
```

---

### ⚙️ Установка
1. Установите[Fabric Loader](https://fabricmc.net/) 0.16.5+.
2. Скачайте **Fabric API** и **ModMenu**.
3. Переместите `.jar` файл мода `MultiCmd` в папку `mods`.
*Примечание: Это **Клиентский** мод. Его НЕ нужно устанавливать на сервер!*

### 📜 Список команд
* `/batch <команда|help>` — Выполнить пакет или открыть внутриигровой гайд.
* `/lua run <скрипт>` — Запустить Lua скрипт.
* `/lua stopall` — Остановить всех фоновых ботов (демонов).
* `/macro bind <клавиша> <команда>` — Привязать скрипт/макрос к кнопке клавиатуры.
* `/group [add|remove|list]` — Управление группами игроков.

---
<div align="center">
  <i>Developed with ❤️ by TAOSHOI</i>
</div>
