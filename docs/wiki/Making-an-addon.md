# Создание аддона

Здесь по шагам собирается аддон с одним действием: «Сердечки вокруг меня». Готовый результат лежит в
[`Modifications/ExampleAddon`](../../Modifications/ExampleAddon).

Нужно: JDK 25, Gradle (или gradlew из шаблона Fabric), Minecraft 26.3 с Fabric Loader 0.19.5+.

## 1. Проект Fabric-мода

Возьмите шаблон на [fabricmc.net/develop/template](https://fabricmc.net/develop/template/)
(Minecraft 26.3, только клиент) или скопируйте `ExampleAddon`. Получится обычная структура:

```
my-addon/
├─ build.gradle
├─ gradle.properties
├─ libs/                       ← сюда положим DA Integrate
└─ src/main/
   ├─ java/com/example/myaddon/
   └─ resources/fabric.mod.json
```

## 2. Подключить DA Integrate

Скачайте `daintegrate-<версия>.jar` со страницы [Releases](https://github.com/bummy1337/da-integration/releases)
и положите в `libs/`. В `build.gradle`:

```groovy
dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    implementation "net.fabricmc:fabric-loader:${project.loader_version}"

    // API для компиляции. В игре его даёт сам DA Integrate, внутрь аддона он не попадает
    compileOnly files("libs/daintegrate-2.0.3.jar")

    // Чтобы gradle runClient запускал игру сразу с DA Integrate (ему нужен Fabric API)
    modLocalRuntime files("libs/daintegrate-2.0.3.jar")
    modLocalRuntime "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
}
```

> API не зависит от Minecraft, поэтому хватает `compileOnly`, и ремаппинг не нужен.

## 3. fabric.mod.json

Две важные вещи: entrypoint `daintegrate` и зависимость от мода.

```json
{
  "schemaVersion": 1,
  "id": "myaddon",
  "version": "${version}",
  "name": "My DA addon",
  "environment": "client",
  "entrypoints": {
    "daintegrate": [ "com.example.myaddon.MyAddon" ]
  },
  "depends": {
    "fabricloader": ">=0.19.5",
    "minecraft": ">=26.3",
    "daintegrate": ">=2.0.1"
  },
  "contact": { "homepage": "https://example.com/myaddon" }
}
```

- `id` мода становится частью id действия: `myaddon/handler/hearts`. Не меняйте его после выпуска, иначе старые триггеры потеряют действие.
- `contact.homepage` необязателен, мод сохраняет его как ссылку на аддон.
- Аддон клиентский, как и сам DA Integrate: `"environment": "client"`.

## 4. Класс настроек

Настройки действия — обычный класс с **публичными** полями. Каждое поле становится полем ввода в меню,
а значение по умолчанию берётся из инициализатора.

```java
package com.example.myaddon;

public class HeartsSettings {
    public int count = 20;       // число → поле для цифр
    public float radius = 1.5f;  // дробное число
}
```

Поддерживаемые типы и как их показывает меню:

| Тип поля | В меню |
| --- | --- |
| `String` | текстовое поле, в него можно вставлять подстановки `{username}` и др. |
| `int`, `long`, `float`, `double` | поле для чисел, запятая тоже принимается |
| `boolean` | переключатель |

Нужен публичный конструктор без аргументов (если вы не пишете конструкторов, он есть). Статические и
непубличные поля меню и файл настроек пропускают. Если настроек нет вовсе, используйте `None.class`.

## 5. Регистрация действия

```java
package com.example.myaddon;

import net.bummy1337.daintegrate.IModuleConfiguration;
import net.bummy1337.daintegrate.api.DaIntegratePlugin;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;

public class MyAddon implements DaIntegratePlugin {
    @Override
    public void register(IModuleConfiguration configuration) {
        configuration.registerAction("hearts", "Сердечки вокруг меня", HeartsSettings.class, MyAddon::hearts);
    }

    private static void hearts(ReadOnlyDonationAlertsEvent event, HeartsSettings settings) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || minecraft.level == null)
            return; // донат пришёл, пока игрок в меню
        for (int i = 0; i < settings.count; i++) {
            double angle = Math.PI * 2 * i / settings.count;
            minecraft.level.addParticle(ParticleTypes.HEART,
                    player.getX() + Math.cos(angle) * settings.radius,
                    player.getY() + 1,
                    player.getZ() + Math.sin(angle) * settings.radius,
                    0, 0.05, 0);
        }
    }
}
```

`registerAction(id, название, класс настроек, код)`:
- `id` — латиница без пробелов, уникален в пределах аддона;
- название показывается в меню (можно по-русски);
- код вызывается **в игровом потоке**, поэтому из него можно сразу работать с `Minecraft.getInstance()`.

## 6. Сборка и проверка

```
gradlew build       → build/libs/myaddon-1.0.0.jar
gradlew runClient   → игра с DA Integrate и вашим аддоном
```

В игре:
1. Нажмите `=` (или `/da`) → «Триггеры» → выберите триггер.
2. В блоке «Тогда» нажмите «+ Ещё…» → «Сердечки вокруг меня».
3. Поменяйте настройки, нажмите «Сохранить», потом «Проверить»: действие сработает на тестовом донате.

В `settings.yaml` (папка игры → `donation-alerts-integrate/`) действие выглядит так:

```yaml
handlers:
- delay: 0
  properties:
    type: myaddon/handler/hearts
    value:
      count: 30
      radius: 2.0
```

## 7. Публикация

Выкладывайте jar аддона как обычный мод. Игроку нужны Fabric API, DA Integrate и ваш аддон в папке `mods`.
Если игрок удалит аддон, его триггеры не сломаются: действие пропускается и остаётся в настройках, а при
возвращении аддона снова заработает.

Дальше: [Справочник API](Addon-API-reference.md).
