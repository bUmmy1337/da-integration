package net.bummy1337.daintegrate.ui;

import java.util.HashMap;
import java.util.Map;

/**
 * Menu strings. Russian is used for ru_* / uk_* / be_* game languages, English otherwise.
 */
public final class I18n {
    private static final Map<String, String> ru = new HashMap<>();
    private static final Map<String, String> en = new HashMap<>();
    private static Map<String, String> current = en;

    private I18n() {
    }

    public static void setLanguage(String gameLanguage) {
        var code = gameLanguage == null ? "" : gameLanguage.toLowerCase();
        current = code.startsWith("ru") || code.startsWith("uk") || code.startsWith("be") ? ru : en;
    }

    public static boolean isRussian() {
        return current == ru;
    }

    public static String t(String key) {
        var value = current.get(key);
        if (value == null)
            value = en.get(key);
        return value == null ? key : value;
    }

    public static String t(String key, Object... args) {
        return String.format(t(key), args);
    }

    private static void add(String key, String english, String russian) {
        en.put(key, english);
        ru.put(key, russian);
    }

    static {
        add("logo.subtitle", "for Minecraft", "для Minecraft");
        add("nav.overview", "Overview", "Обзор");
        add("nav.connection", "Connection", "Подключение");
        add("nav.triggers", "Triggers", "Триггеры");
        add("nav.settings", "Settings", "Настройки");
        add("nav.help", "Help", "Справка");

        add("state.Disconnected", "Disconnected", "Не подключено");
        add("state.Connecting", "Connecting…", "Подключение…");
        add("state.Connected", "Connected", "Подключено");
        add("state.Reconnecting", "Reconnecting…", "Переподключение…");
        add("state.Error", "Connection error", "Ошибка подключения");

        add("overview.title", "Overview", "Обзор");
        add("overview.subtitle", "Everything that happens on your stream, at a glance", "Всё, что происходит на стриме, на одном экране");
        add("overview.status", "Status", "Статус");
        add("overview.triggers", "Active triggers", "Активные триггеры");
        add("overview.events", "Events this session", "Событий за сессию");
        add("overview.last", "Last event", "Последнее событие");
        add("overview.recent", "Recent events", "Последние события");
        add("overview.empty", "No events yet", "Событий пока нет");
        add("overview.empty.hint", "Send a test alert from the Donation Alerts dashboard to check that everything works.", "Отправьте тестовое оповещение из панели Donation Alerts, чтобы проверить, что всё работает.");
        add("overview.connect.cta", "Connect Donation Alerts", "Подключить Donation Alerts");
        add("overview.connect.hint", "You are not connected yet. It takes less than a minute.", "Вы ещё не подключены. Это займёт меньше минуты.");
        add("col.time", "Time", "Время");
        add("col.type", "Type", "Тип");
        add("col.from", "From", "От кого");
        add("col.amount", "Amount", "Сумма");
        add("col.message", "Message", "Сообщение");
        add("never", "—", "—");
        add("test", "test", "тест");

        add("conn.title", "Connect Donation Alerts", "Подключение Donation Alerts");
        add("conn.subtitle", "Three short steps. The token stays on your computer.", "Три коротких шага. Токен хранится только на вашем компьютере.");
        add("conn.step1", "Open Donation Alerts", "Откройте Donation Alerts");
        add("conn.step1.desc", "Sign in and copy the secret token from the account settings, or copy the alert widget link for OBS.", "Войдите и скопируйте секретный токен в основных настройках или ссылку виджета оповещений для OBS.");
        add("conn.step1.button", "Open token page", "Открыть страницу токена");
        add("conn.step1.widget", "Open alert widget", "Открыть виджет оповещений");
        add("conn.step2", "Paste the token", "Вставьте токен");
        add("conn.step2.desc", "A bare token and a widget link both work, we will find the token ourselves.", "Подойдёт и сам токен, и ссылка виджета: токен мы найдём сами.");
        add("conn.step2.paste", "Paste from clipboard", "Вставить из буфера");
        add("conn.step2.hint", "Token or link", "Токен или ссылка");
        add("conn.step2.save", "Save", "Сохранить");
        add("conn.step2.saved", "Token saved: %s", "Токен сохранён: %s");
        add("conn.step2.invalid", "This does not look like a token or a Donation Alerts link.", "Это не похоже на токен или ссылку Donation Alerts.");
        add("conn.step2.clipboard.empty", "The clipboard is empty.", "Буфер обмена пуст.");
        add("conn.step2.forget", "Forget token", "Удалить токен");
        add("conn.step2.change", "Change", "Изменить");
        add("conn.step3", "Connect", "Подключитесь");
        add("conn.step3.desc", "After connecting, send a test alert from Donation Alerts. It will appear in the overview.", "После подключения отправьте тестовое оповещение из Donation Alerts, оно появится в обзоре.");
        add("conn.connect", "Connect", "Подключиться");
        add("conn.disconnect", "Disconnect", "Отключиться");
        add("conn.retry", "Retry", "Повторить");
        add("conn.since", "Connected for %s", "Подключено %s");
        add("conn.lastEvent", "Last event: %s ago", "Последнее событие: %s назад");
        add("conn.waiting", "Waiting for the first event…", "Ждём первое событие…");
        add("conn.error", "Could not connect: %s", "Не удалось подключиться: %s");
        add("conn.auto", "Connect automatically when the game starts", "Подключаться автоматически при запуске игры");
        add("conn.needToken", "Paste the token first", "Сначала вставьте токен");

        add("trg.title", "Triggers", "Триггеры");
        add("trg.subtitle", "What happens in the game when someone supports you", "Что происходит в игре, когда вас поддерживают");
        add("trg.search", "Search…", "Поиск…");
        add("trg.add", "New trigger", "Новый триггер");
        add("trg.empty", "No triggers yet", "Триггеров пока нет");
        add("trg.empty.hint", "Create the first one: for example, say thanks in chat for every donation.", "Создайте первый: например, благодарность в чат за каждый донат.");
        add("trg.select", "Select a trigger on the left or create a new one", "Выберите триггер слева или создайте новый");
        add("trg.name", "Name", "Название");
        add("trg.enabled", "Enabled", "Включён");
        add("trg.when", "When", "Когда");
        add("trg.event", "Event", "Событие");
        add("trg.from", "From", "От");
        add("trg.to", "To", "До");
        add("trg.currency", "Currency", "Валюта");
        add("trg.currency.any", "Any", "Любая");
        add("trg.sub.type", "Subscription", "Подписка");
        add("trg.then", "Then", "Тогда");
        add("trg.action.message", "Chat message", "Сообщение в чат");
        add("trg.action.command", "Command", "Команда");
        add("trg.action.delay", "Delay before the action, in seconds", "Задержка перед действием, в секундах");
        add("trg.action.seconds", "s", "с");
        add("trg.action.hint", "{username}, thanks for {amount} {currency}!", "{username}, спасибо за {amount} {currency}!");
        add("trg.to.hint", "empty = no limit", "пусто = без ограничения");
        add("trg.action.add.message", "+ Message", "+ Сообщение");
        add("trg.action.add.command", "+ Command", "+ Команда");
        add("trg.action.none", "No actions yet. Add a chat message or a command.", "Действий пока нет. Добавьте сообщение в чат или команду.");
        add("trg.placeholders", "Placeholders, click to insert:", "Подстановки, нажмите, чтобы вставить:");
        add("trg.test", "Test", "Проверить");
        add("trg.test.tooltip", "Runs this trigger's actions in the game with a sample event", "Выполнит действия триггера в игре на примере события");
        add("trg.test.sent", "Test event sent to the game", "Тестовое событие отправлено в игру");
        add("trg.duplicate", "Duplicate", "Дублировать");
        add("trg.delete", "Delete", "Удалить");
        add("trg.delete.confirm", "Delete trigger \"%s\"?", "Удалить триггер «%s»?");
        add("trg.unsaved", "Unsaved changes", "Есть несохранённые изменения");
        add("trg.save", "Save", "Сохранить");
        add("trg.revert", "Revert", "Отменить");
        add("trg.saved", "Saved", "Сохранено");
        add("trg.saveError", "Could not save: %s", "Не удалось сохранить: %s");
        add("trg.newName", "New trigger", "Новый триггер");
        add("trg.copySuffix", " (copy)", " (копия)");

        add("kind.Any", "Any event", "Любое событие");
        add("kind.Donate", "Donation", "Донат");
        add("kind.Subscribe", "Subscription", "Подписка");
        add("kind.TwitchBits", "Twitch Bits", "Twitch Bits");
        add("kind.TwitchPoints", "Twitch channel points", "Баллы канала Twitch");

        add("sub.YouTubeSubscription", "YouTube subscription", "Подписка на YouTube");
        add("sub.TwitchSubscription", "Twitch subscription", "Подписка на Twitch");
        add("sub.TwitchPrimeSubscription", "Twitch Prime subscription", "Подписка Twitch Prime");
        add("sub.TwitchGiftSubscription", "Twitch gift subscription", "Подарочная подписка Twitch");
        add("sub.TwitchFreeFollow", "Twitch follow", "Фоллоу на Twitch");

        add("set.title", "Settings", "Настройки");
        add("set.subtitle", "Behaviour of the mod", "Поведение мода");
        add("set.skipTest", "Ignore test alerts", "Игнорировать тестовые оповещения");
        add("set.skipTest.desc", "Test alerts from Donation Alerts will not run triggers.", "Тестовые оповещения из Donation Alerts не будут запускать триггеры.");
        add("set.welcome", "Show tips in chat when joining a world", "Показывать подсказки в чате при входе в мир");
        add("set.uiScale", "Menu scale", "Масштаб меню");
        add("set.files", "Files", "Файлы");
        add("set.openConfig", "Open settings.yaml", "Открыть settings.yaml");
        add("set.openFolder", "Open folder", "Открыть папку");
        add("set.reload", "Reload from file", "Перечитать файл");

        add("help.title", "Help", "Справка");
        add("help.placeholders", "Placeholders", "Подстановки");
        add("help.ph.username", "donor's name", "имя отправителя");
        add("help.ph.amount", "amount", "сумма");
        add("help.ph.currency", "currency", "валюта");
        add("help.ph.message", "donation message", "сообщение доната");
        add("help.ph.playername", "your nickname in Minecraft", "ваш ник в Minecraft");
        add("help.keys", "Keys", "Клавиши");
        add("help.keys.open", "Open this menu: %s (change in Controls)", "Открыть это меню: %s (меняется в «Управлении»)");
        add("help.keys.save", "Save changes: Ctrl+S", "Сохранить изменения: Ctrl+S");
        add("help.commands", "Chat commands", "Команды чата");
        add("help.cmd.connect", "/da connect, connect", "/da connect, подключиться");
        add("help.cmd.disconnect", "/da disconnect, disconnect", "/da disconnect, отключиться");
        add("help.cmd.status", "/da status, connection status", "/da status, статус подключения");
        add("help.cmd.set", "/da set, save the token or link from the clipboard and connect", "/da set, взять токен или ссылку из буфера и подключиться");
        add("help.guide", "Open the guide on GitHub", "Открыть руководство на GitHub");

        add("close", "Close", "Закрыть");
        add("cancel", "Cancel", "Отмена");
        add("ago.s", "%d s", "%d с");
        add("ago.m", "%d min", "%d мин");
        add("ago.h", "%d h", "%d ч");
    }
}
