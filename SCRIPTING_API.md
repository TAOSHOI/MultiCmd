<div align="center">
  <h1>🤖 MultiCmd: Scripting & Bot API</h1>
  <p><strong>Comprehensive Guide to Lua Automation / Полное руководство по автоматизации на Lua</strong></p>
</div>

---

<details>
<summary><h2>🇬🇧 English Version (Click to expand)</h2></summary>

## 1. How Scripts Work
MultiCmd uses the **Luaj** engine, operating in a completely asynchronous, thread-safe environment. Even if your script contains an infinite `while true do` loop, **your game will never freeze or crash**.
All scripts must be placed in: `.minecraft/config/multicmd/scripts/`.
To run a script named `bot.lua`, type: `/lua run bot`.

## 2. Script Types: Linear vs Daemons
* **Linear Scripts:** Execute from top to bottom and terminate. Ideal for quick macros (e.g., "build a wall", "tactical 180° turn").
* **Daemons (Background Bots):** If your script declares at least one event listener (like `function onTick()`), the engine automatically converts it into a background process. It will run forever until you type `/lua stopall`.

---

## 3. The Global `api` Object
In all scripts, you have access to the global `api` object. The engine automatically synchronizes threads, so it's 100% safe.

### Execution & Queues
| Method | Description |
| :--- | :--- |
| `api:print("text")` | Prints a local message to your chat (server doesn't see it). |
| `api:execute("/cmd")` | Runs a command through the MultiCmd parser (supports ranges `[1-5]`). |
| `api:executeInstant("/cmd")`| Bypasses the queue and sends the packet to the server instantly. |
| `api:queue("/cmd")` | Adds a raw command to the anti-spam queue. |
| `api:wait(ticks)` | Pauses the script execution for `X` ticks (20 ticks = 1 sec). |

### Player Info
| Method | Returns | Description |
| :--- | :--- | :--- |
| `api:getX()`, `Y()`, `Z()` | `number` | Exact player coordinates. |
| `api:getYaw()`, `Pitch()` | `number` | Camera rotation angles. |
| `api:getHealth()` | `number` | Player HP (20.0 = full). |
| `api:getFood()` | `number` | Hunger level (20 = full). |
| `api:getAttackCooldown()` | `number` | Weapon readiness from 0.0 to 1.0 (1.0 = fully charged). |
| `api:isUsingItem()` | `boolean` | `true` if the player is eating, drinking, or drawing a bow. |

### Input & Camera (Aimbot)
| Method | Description |
| :--- | :--- |
| `api:click("button")` | Single click. Supports: `"m1"` (Attack), `"m2"` (Use). |
| `api:setKey("key", bool)`| Holds (`true`) or releases (`false`) a key. Supports: `"w", "a", "s", "d", "space", "shift", "m1", "m2"`. |
| `api:setYaw(val)` | Instantly rotates the camera horizontally. |
| `api:lookAt(x, y, z)` | **Smart Aimbot.** Automatically calculates trigonometry and aims exactly at the target coordinates. |

### Inventory & World
| Method | Returns | Description |
| :--- | :--- | :--- |
| `api:getSlot()` | `number` | Returns current hotbar slot (1-9). |
| `api:setSlot(slot)` | `void` | Instantly switches hotbar slot (1-9). |
| `api:findItemInHotbar("name")`| `number` | Searches for an item by partial ID (e.g. `"apple"`). Returns slot (1-9) or `-1` if not found. |
| `api:getBlock(x, y, z)` | `string` | Returns block ID (e.g., `"minecraft:diamond_ore"`). |
| `api:getClosestEntity(rad)`| `table` | Returns a table with data of the closest mob/player in `rad` radius. |

---

## 4. Event Listeners (Daemons)
Declare these functions in your script to make it a Daemon.

*   `function onTick()` — Fires 20 times per second. Reentrancy-safe (won't overlap if execution takes >50ms).
*   `function onHealthDrop(hp)` — Fires instantly when taking damage.
*   `function onChat(msg)` — Fires when receiving a chat message.

---

## 5. Bot Examples

### Example A: Killaura with Auto-Eat (`hunter.lua`)
```lua
local state = "COMBAT"

function onTick()
    local hp = api:getHealth()
    
    if state == "COMBAT" and hp < 12.0 then
        state = "EATING"
    elseif state == "EATING" and hp >= 18.0 then
        state = "COMBAT"
        api:setKey("m2", false) -- Release right-click
        api:setSlot(1)          -- Back to sword
    end
    
    if state == "COMBAT" then
        local target = api:getClosestEntity(4.0)
        if target.type ~= "MISS" then
            api:lookAt(target.x, target.y + 1.0, target.z) -- Aim at chest
            if api:getAttackCooldown() >= 1.0 then api:click("m1") end
        end
    elseif state == "EATING" then
        if not api:isUsingItem() then
            local foodSlot = api:findItemInHotbar("apple")
            if foodSlot ~= -1 then
                api:setSlot(foodSlot)
                api:setKey("m2", true) -- Hold right-click to eat
            else
                api:setKey("m2", false)
            end
        end
    end
end
```

</details>

<details>
<summary><h2>🇷🇺 Русская Версия (Нажмите, чтобы развернуть)</h2></summary>

## 1. Как работают скрипты?
MultiCmd использует движок **Luaj**, который работает в полностью асинхронном и потокобезопасном (Thread-Safe) режиме. Это означает, что даже если ваш скрипт содержит бесконечный цикл `while true do`, игра **никогда не зависнет и не вылетит**.
Все скрипты хранятся в папке: `.minecraft/config/multicmd/scripts/`.
Для запуска скрипта `bot.lua` напишите в чат: `/lua run bot`.

## 2. Типы скриптов: Обычные vs Демоны
* **Линейные скрипты:** Выполняются сверху вниз и завершаются. Идеально для макросов (например, "тактический разворот на 180°").
* **Демоны (Фоновые боты):** Если в вашем скрипте объявлена хотя бы одна функция-событие (например, `function onTick()`), движок автоматически переводит скрипт в фоновый режим. Он будет работать вечно, пока вы не напишете `/lua stopall`.

---

## 3. Глобальный объект `api`
Во всех скриптах вам доступен глобальный объект `api`. Все вызовы к ядру Minecraft происходят через него и автоматически синхронизируются.

### Выполнение и Очереди
| Метод | Описание |
| :--- | :--- |
| `api:print("текст")` | Выводит локальное сообщение в чат игрока (сервер этого не видит). |
| `api:execute("/cmd")` | Прогоняет команду через парсер MultiCmd (поддерживает диапазоны `[1-5]`). |
| `api:executeInstant("/cmd")`| Игнорирует очередь и мгновенно отправляет пакет на сервер (без задержек). |
| `api:queue("/cmd")` | Добавляет чистую команду в очередь отправки. |
| `api:wait(ticks)` | **Важно!** Приостанавливает выполнение скрипта на `X` тиков (20 тиков = 1 сек). |

### Информация об игроке
| Метод | Возвращает | Описание |
| :--- | :--- | :--- |
| `api:getX()`, `Y()`, `Z()` | `number` | Точные координаты игрока. |
| `api:getYaw()`, `Pitch()` | `number` | Углы поворота камеры. |
| `api:getHealth()` | `number` | Здоровье игрока (20.0 = полное). |
| `api:getFood()` | `number` | Уровень сытости (20 = полный). |
| `api:getAttackCooldown()` | `number` | Готовность оружия от 0.0 до 1.0 (где 1.0 — можно бить). |
| `api:isUsingItem()` | `boolean` | `true`, если игрок сейчас ест еду, натягивает лук или пьет зелье. |

### Ввод и Камера (Аимбот)
| Метод | Описание |
| :--- | :--- |
| `api:click("button")` | Одиночный клик. Поддерживает: `"m1"` (Атака), `"m2"` (Использование). |
| `api:setKey("key", bool)`| Зажимает (`true`) или отпускает (`false`) клавишу. Поддерживает: `"w", "a", "s", "d", "space", "shift", "m1", "m2"`. |
| `api:setYaw(val)` | Моментально поворачивает камеру по горизонтали. |
| `api:lookAt(x, y, z)` | **Умное наведение.** Автоматически высчитывает тригонометрию и наводит прицел точно на указанную точку. |

### Инвентарь и Мир
| Метод | Возвращает | Описание |
| :--- | :--- | :--- |
| `api:getSlot()` | `number` | Возвращает текущий слот хотбара (1-9). |
| `api:setSlot(slot)` | `void` | Мгновенно переключает активный слот хотбара (1-9). |
| `api:findItemInHotbar("имя")`| `number` | Ищет предмет по части ID (например, `"apple"`). Возвращает слот (1-9) или `-1`, если не найдено. |
| `api:getBlock(x, y, z)` | `string` | Возвращает ID блока по координатам (например, `"minecraft:diamond_ore"`). |
| `api:getClosestEntity(rad)`| `table` | Возвращает таблицу с данными ближайшего моба/игрока в радиусе `rad`. Содержит: `type`, `name`, `x`, `y`, `z`, `health`, `distance`. |

---

## 4. События (Event Listeners)
Чтобы превратить ваш скрипт в Демона, просто объявите в нем одну из этих функций:

*   `function onTick()` — Вызывается 20 раз в секунду. Защищено от "наслаивания" (Reentrancy-safe).
*   `function onHealthDrop(hp)` — Срабатывает моментально при получении урона. Идеально для Авто-Тотема.
*   `function onChat(msg)` — Чтение входящего чата.

---

## 5. Примеры ботов

### Пример А: Умная Киллаура с Авто-Поеданием (`hunter.lua`)
Бот стоит на месте, убивает всё живое в радиусе 4 блоков. Если здоровье падает ниже 6 сердец (12 HP), он берет яблоко, кушает, и возвращается в бой.

```lua
local state = "COMBAT"

function onTick()
    local hp = api:getHealth()
    
    -- Переключатель состояний
    if state == "COMBAT" and hp < 12.0 then
        state = "EATING"
    elseif state == "EATING" and hp >= 18.0 then
        state = "COMBAT"
        api:setKey("m2", false) -- Отпускаем ПКМ
        api:setSlot(1)          -- Возвращаем меч (1 слот)
    end
    
    -- Логика Боя
    if state == "COMBAT" then
        local target = api:getClosestEntity(4.0)
        if target.type ~= "MISS" then
            api:lookAt(target.x, target.y + 1.0, target.z) -- Целимся в тело
            
            -- Бьем только при 100% заряде оружия
            if api:getAttackCooldown() >= 1.0 then
                api:click("m1")
            end
        end
        
    -- Логика Еды
    elseif state == "EATING" then
        if not api:isUsingItem() then
            local foodSlot = api:findItemInHotbar("apple")
            if foodSlot ~= -1 then
                api:setSlot(foodSlot)
                api:setKey("m2", true) -- Зажимаем кнопку кушать
            else
                api:setKey("m2", false)
            end
        end
    end
end
```

</details>
