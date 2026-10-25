# Справочник API

Всё, что нужно аддону, лежит в пакетах `net.bummy1337.daintegrate` и `net.bummy1337.dontaionalerts`
(да, во втором пакете опечатка, но так он называется).

## DaIntegratePlugin

```java
package net.bummy1337.daintegrate.api;

public interface DaIntegratePlugin {
    void register(IModuleConfiguration configuration);
}
```

Точка входа аддона. Объявляется в `fabric.mod.json` как entrypoint `"daintegrate"`. DA Integrate вызывает
`register` один раз, при запуске игры, **до загрузки настроек**. Поэтому регистрируйте всё здесь, а не позже:
действие, зарегистрированное после загрузки, не подхватит уже загруженные триггеры.

Каждому аддону выдаётся свой `IModuleConfiguration`, привязанный к id его мода. Если `register` бросит
исключение, оно попадёт в лог (`failed to register actions of <id>`), а игра и другие аддоны продолжат работать.

## IModuleConfiguration

Все методы возвращают тот же объект, так что вызовы можно писать цепочкой.

### registerAction

```java
<T> IModuleConfiguration registerAction(String id, String title, Class<T> propertiesType, IAction<T> action);
```

| Параметр | Что это |
| --- | --- |
| `id` | Короткий id действия. Полный id в файле настроек: `<id мода>/handler/<id>` |
| `title` | Название в списке действий меню |
| `propertiesType` | Класс настроек (см. ниже) или `None.class` |
| `action` | Код действия, `(event, settings) -> { ... }` |

Повторная регистрация того же id заменяет действие.

### registerEventListener

```java
IModuleConfiguration registerEventListener(IListener<ReadOnlyDonationAlertsEvent> listener);
```

Получает **каждое** событие от Donation Alerts, независимо от триггеров: для статистики, своих оверлеев,
логов. Вызывается в потоке сокета, а не в игровом, так что для работы с игрой оборачивайте код
в `Minecraft.getInstance().execute(() -> ...)`. Тестовые оповещения (`event.isTest()`) приходят тоже.

### registerSensitive (продвинутое)

```java
<T> IModuleConfiguration registerSensitive(ISensitive<T> sensitive);
```

Своё **условие** срабатывания триггера: реализуйте `ISensitive<T>` с методами `getImplementationId()` и
`boolean isActive(event, settings)`. Тип настроек берётся из параметра интерфейса, поэтому нужен отдельный
класс, а не лямбда. Условия работают из `settings.yaml` (`type: <id мода>/sensitive/<id>`), но меню пока
умеет редактировать только встроенные условия, и триггер с вашим условием, пересохранённый через меню,
получит условие «любое событие». Используйте, если условие задаётся только в файле.

### registerHandler (старый способ)

`registerHandler(IHandler<T>)` — прежний интерфейс действия: класс с `handle(event, settings)` и
`getImplementationId()`, без названия для меню. Работает, но для новых аддонов удобнее `registerAction`.

## IAction

```java
@FunctionalInterface
public interface IAction<T> {
    void run(ReadOnlyDonationAlertsEvent event, T properties);
}
```

- Вызывается **в игровом потоке** (и для мгновенных действий, и для отложенных), так что `Minecraft`,
  `player`, `level` можно трогать напрямую.
- `properties` — новый объект ваших настроек, заполненный из файла. Не храните его: при изменении настроек придёт другой.
- Исключение из `run` ловится и пишется в лог, остальные действия триггера выполняются дальше.
- Не делайте долгую работу (сеть, файлы) прямо в `run`: игра зависнет. Уносите её в отдельный поток.
- `player` бывает `null`, например в главном меню, поэтому проверяйте.

## Класс настроек

- Публичный класс с публичным конструктором без аргументов.
- Настройки — **публичные нестатические** поля: `String`, `int`, `long`, `float`, `double`, `boolean`.
  Поля других типов загрузятся из файла, но в меню не отобразятся.
- Значения по умолчанию задаются инициализаторами полей: они попадают и в меню при добавлении действия,
  и в настройки, если поля нет в файле.
- Подписи в меню — имена полей, поэтому называйте их понятно (`count`, `radius`, `text`).
- Переименование поля — это новое поле: старое значение из файла пропадёт. Добавлять поля можно спокойно.

## ReadOnlyDonationAlertsEvent

| Метод | Что возвращает |
| --- | --- |
| `getUserName()` | Имя зрителя |
| `getAmount()` | Сумма (`float`). Для баллов и битсов — их число |
| `getCurrency()` | Валюта: `RUB`, `USD`, … (может быть `null` у подписок) |
| `getMessage()` | Сообщение зрителя (может быть `null`) |
| `getType()` | `AlertType`, см. ниже |
| `isTest()` | `true` для тестового оповещения из панели Donation Alerts |
| `getReceivedAt()` | Когда пришло, `System.currentTimeMillis()` |

`AlertType`: `Donate`, `TwitchSubscription`, `TwitchFreeFollow`, `YouTubeSubscription`, `TwitchBits`,
`TwitchGiftSubscription`, `TwitchPrimeSubscription`, `TwitchPoints`, `Undefined`.

## Подстановки: ReplaceHelper

```java
String text = ReplaceHelper.replace(settings.text, event, player.getName().getString());
```

Заменяет `{username}`, `{amount}`, `{currency}`, `{message}` и `{playername}`, так же как встроенные действия.
В меню под действиями есть кнопки-подстановки: они вставляют текст в поле, где стоит курсор, так что
в текстовых полях ваших настроек они работают сами.

## Задержка

Задержку задаёт пользователь в меню (секунды) или в файле (`delay`, в тиках, 20 тиков = 1 секунда).
Аддону ничего делать не нужно: DA Integrate сам вызовет действие через нужное время.

## Формат в settings.yaml

```yaml
triggers:
- name: Сердечки за 100+
  isActive: true
  sensitives:
  - properties:
      type: daintegrate/sensitive/donate
      value: { from: 100, to: 999999, currency: RUB }
  handlers:
  - delay: 0
    properties:
      type: daintegrate-example-addon/handler/hearts
      value:
        count: 30
        radius: 2.0
```

Если мода с таким `type` нет, триггер всё равно загружается: это действие пропускается (в логе
`Skip unknown action '...'`) и при сохранении из меню остаётся в файле как было.
