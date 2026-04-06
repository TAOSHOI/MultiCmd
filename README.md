<div align="center">
  <img src="src/main/resources/assets/multicmd/icon.png" alt="MultiCmd Logo" width="128" height="128">
  <h1>🚀 MultiCmd (Fabric 1.21.1+)</h1>
  <p><strong>The Ultimate Batch Command & Macro Engineering Tool | Инструмент инженерии пакетных команд</strong></p>
  
  [![Fabric](https://img.shields.io/badge/Fabric-1.21.1-blue.svg)](https://fabricmc.net/)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  
  [🇷🇺 Русский](#-русский) • [🇬🇧 English](#-english)
</div>

---

## 🇬🇧 English

**MultiCmd** is a powerful client-side utility mod designed to eliminate repetitive command typing. Need to add 10 friends to 20 WorldGuard regions? Distribute items randomly? Execute complex macros? Do it all in one single chat message!

### ✨ Key Features
* **🧠 Smart Parser:** Supports numeric ranges `[1-10]`, lists `{Steve,Alex}`, and randomizers `?{A,B}`.
* **🛡️ SmartGuard Anti-Spam:** Commands are queued and dispatched with a configurable tick delay. Prevents getting kicked for spamming.
* **🖥️ Vanilla-Style GUI:** Press `U` (or the `M` button in chat) to open an elegant control panel.
* **🔔 Toast Notifications:** Clean, unobtrusive on-screen popups instead of chat spam.
* **👥 Groups & Macros:** Save player teams (`@Builders`) and command chains (`#claim`) locally.
* **🌍 Environment Variables:** Inject `%me%`, `%x%`, `%y%`, `%z%` into your commands dynamically.

### 📸 Media
*(Upload your screenshots to a `docs/` folder in your repo to make these links work)*

| Control Panel (GUI) | Interactive Help |
|:---:|:---:|
| <img src="docs/gui_screenshot.png" width="400" alt="GUI"> | <img src="docs/help_preview.png" width="400" alt="Help"> |

### 📖 Syntax Examples

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

**3. Using Groups & Coordinates**
```text
/group add MyTeam Steve,Alex
/batch /msg @MyTeam I am here: %x% %y% %z%
```

### ⚙️ Installation
1. Install [Fabric Loader](https://fabricmc.net/) 0.16.5+.
2. Download **Fabric API** and **ModMenu**.
3. Drop the `MultiCmd` `.jar` into your `mods` folder.
*Note: This is a **Client-Side** mod. You do NOT need to install it on the server!*

### 📜 Commands
* `/batch <command|help>` — Execute a batch or view interactive help.
* `/multicmd cancel` — Emergency stop the command queue.
* `/group[add|remove|list]` — Manage player groups.
* `/macro [add|remove|list]` — Manage your macros.

---

## 🇷🇺 Русский

**MultiCmd** — это мощный клиентский мод, созданный для избавления от рутины при вводе команд. Нужно добавить 10 друзей в 20 приватов WorldGuard? Случайно раздать ресурсы? Запустить сложный макрос? Сделайте это всё одним сообщением в чат!

### ✨ Главные возможности
* **🧠 Умный Парсер:** Поддерживает диапазоны `[1-10]`, списки `{Стив,Алекс}` и случайный выбор `?{А,Б}`.
* **🛡️ SmartGuard (Анти-спам):** Команды помещаются в очередь и отправляются с настраиваемой задержкой. Сервер вас не кикнет!
* **🖥️ Удобный GUI:** Нажмите `U` (или кнопку `M` прямо в чате), чтобы открыть элегантную панель управления.
* **🔔 Всплывающие уведомления:** Чистые ванильные "тосты" вместо засорения истории чата.
* **👥 Группы и Макросы:** Локальное сохранение команд игроков (`@Builders`) и алиасов (`#claim`).
* **🌍 Переменные среды:** Используйте `%me%`, `%x%`, `%y%`, `%z%` в своих скриптах.

### 📸 Скриншоты

| Всплывающие Уведомления | Работа с прогресс-баром |
|:---:|:---:|
| <img src="docs/toast_preview.png" width="400" alt="Toasts"> | <img src="docs/gui_screenshot.png" width="400" alt="HUD Progress"> |

### 📖 Примеры синтаксиса

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

**3. Использование Групп и Координат**
```text
/group add MyTeam Steve,Alex
/batch /msg @MyTeam Я на базе, координаты: %x% %y% %z%
```

### ⚙️ Установка
1. Установите [Fabric Loader](https://fabricmc.net/) 0.16.5+.
2. Скачайте **Fabric API** и **ModMenu**.
3. Переместите `.jar` файл мода `MultiCmd` в папку `mods`.
*Примечание: Это **Клиентский** мод. Его НЕ нужно устанавливать на сервер!*

### 📜 Список команд
* `/batch <команда|help>` — Выполнить пакет или открыть интерактивную помощь.
* `/multicmd cancel` — Экстренная остановка очереди выполнения.
* `/group[add|remove|list]` — Управление группами игроков.
* `/macro [add|remove|list]` — Управление вашими макросами.

---
<div align="center">
  <i>Developed with ❤️ by TAOSHOI</i>
</div>
