# DonationAlerts Integration
## Minecraft version: 26.3
A modification that allows you to receive events from **DonationAlerts** and interact with your chat and Minecraft server.
Forked from [Folleach repo](https://github.com/Folleach/DonationAlerts-Minecraft) - thanks!

Supports:
- Donation
- Twitch Bits
- Twitch Points
- Subscriptions to YouTube and Twitch

Allows to:
- Send message to the chat
- Execute any Minecraft commands (include mods)

You can download this from: [Releases](https://github.com/bummy1337/da-integration/releases)

## Setup

Press **`=`** in game to open the menu (the key can be changed in *Controls*), or type `/da`.

1. Go to **Connection** and click **Open token page**. Donation Alerts opens in your browser.
2. Copy the **secret token** (*General settings → Secret token*, click *Show*), or the link of the alert widget
   for OBS (`https://www.donationalerts.com/widget/alerts?...&token=...`): the mod finds the token in it.
   Then click **Paste from clipboard**. This is not an API / OAuth token, no app registration is needed.
3. That's it: the mod connects right away and reconnects by itself if the connection drops.
   Send a test alert from Donation Alerts and it shows up on the **Overview** page.

With *Connect automatically* on (the default) the mod connects every time the game starts.
The token is stored only on your computer, in `~/.donation-alerts-token`.

Triggers are created in the **Triggers** page: pick the event (donation, subscription, Bits, points),
the amount range and the currency, then add chat messages or commands. **Test** runs the trigger
without a real donation. Changes are saved with **Save** or `Ctrl+S`.

### Chat commands

| Command | What it does |
|---|---|
| `/da` | open the menu |
| `/da set` | save the token or widget link from the clipboard and connect |
| `/da connect` | connect with the saved token |
| `/da disconnect` | disconnect |
| `/da status` | show the connection status |

### Working on the menu

The menu is an ImGui interface in the `DaUi` module, shared by the mod. To see it without
starting Minecraft, run a preview window with sample data:

```
cd Modifications
./gradlew :DaUi:preview --args="--lang ru --page Triggers"
```

## Configuration

The settings file is in `.minecraft/donation-alerts-integrate/settings.yaml`  
The file format is yaml, example:

```yaml
disableSettingsUpdateMessage: false
disableWelcomeMessage: false
triggers:
- name: default
  description: example trigger
  isActive: true
  sensitives:
  - properties:
      type: daintegrate/sensitive/donate
      value:
        from: 5
        to: 10
        currency: USD
  handlers:
  - delay: 0 # Any comment you can type here!
    properties:
      type: daintegrate/handler/message
      value:
        message: Hello, {username}! This is an example message for all events from Donation Alerts
  - delay: 10
    properties:
      type: daintegrate/handler/command
      value:
        command: time set day # The sun is rising
- name: Subscribtion
  isActive: true
  sensitives:
  - properties:
      type: daintegrate/sensitive/subscribe
      value:
        type: YouTubeSubscription
  handlers:
  - properties:
      type: daintegrate/handler/message
      value:
        message: {username} just subscribe to YouTube!
```

`triggers` is a list of events that can trigger by each message from donation alerts

| Property    | Description            |
| ----------- | ---------------------- |
| name        | Just a name of trigger |
| description | Description of trigger |
| isActive    | Enables or disables the trigger.<br/> If `isActive = false`, the trigger will not be activated |
| sensitive   | Conditions for trigger activation<br/>see #Sensitive for learn more |
| handlers    | Handlers inside the trigger, execute any registered commands.<br/>You can create your own mod that will add a custom handler<br/>See #Handlers for lean more |

### Sensitive

Conditions for trigger activation.  
The DonationAlertsIntegrate provides multiple sensitive

| Type                                | Description                     |
| ----------------------------------- | ------------------------------- |
| `daintegrate/sensitive/always`      | Activates for any event         |
| `daintegrate/sensitive/donate`      | Activates for specific donation |
| `daintegrate/sensitive/subscribe`   | Activates for subscribe         |
| `daintegrate/sensitive/twitch/bits` | Activates for specific bits     |

#### Always

###### Parameters
Has no parameters

###### Example
```yaml
sensitives:
- properties:
    type: daintegrate/sensitive/always
```

#### Donate

Activates if the amount falls within the specified range (`from` <= amount <= `to`).  
And the currency matches.

###### Parameters

| Name       | Type     | Description |
| ---------- | -------- | ----------- |
| `from`     | number   | Left side number  |
| `to`       | number   | Right side number |
| `currency` | text     | Currency in the [ISO 4217](https://en.wikipedia.org/wiki/ISO_4217) code |

###### Example

```yaml
sensitives:
- properties:
    type: daintegrate/sensitive/donate
    value:
      from: 5
      to: 10
      currency: USD
```

#### Subscribe

Activates if the `type` is matches

###### Parameters

| Name       | Type     | Description |
| ---------- | -------- | ----------- |
| `type`     | enum     | Type to match, see below for available types |

###### Available types

- YouTubeSubscription
- TwitchSubscription
- TwitchFreeFollow
- TwitchGiftSubscription
- TwitchPrimeSubscription

###### Example

```yaml
sensitives:
- properties:
    type: daintegrate/sensitive/subscribe
    value:
      type: YouTubeSubscription
```

#### Twitch points

Activates if the amount of Twitch points falls within the specified range (`from` <= amount <= `to`).  

###### Parameters

| Name       | Type     | Description |
| ---------- | -------- | ----------- |
| `from`     | number   | Left side of the Twitch bits amount  |
| `to`       | number   | Right side of the Twitch bits amount |

###### Example

```yaml
sensitives:
- properties:
    type: daintegrate/sensitive/twitch/points
    value:
      from: 0
      to: 1000
```

#### Twitch bits

Activates if the amount of Twitch bits falls within the specified range (`from` <= amount <= `to`).  

###### Parameters

| Name       | Type     | Description |
| ---------- | -------- | ----------- |
| `from`     | number   | Left side of the Twitch bits amount  |
| `to`       | number   | Right side of the Twitch bits amount |

###### Example

```yaml
sensitives:
- properties:
    type: daintegrate/sensitive/twitch/bits
    value:
      from: 0
      to: 2000
```

### Handlers

Handlers for the trigger.  
Perform actions when the trigger is activated.  

| Type                          | Description                                  |
| ----------------------------- | -------------------------------------------- |
| `daintegrate/handler/message` | Adds a message to your chat                  |
| `daintegrate/handler/command` | Sends a command to the server on your behalf |
| `daintegrate/handler/title`   | Shows a title and subtitle on the screen     |
| `daintegrate/handler/actionbar` | Shows text above the hotbar                |
| `daintegrate/handler/sound`   | Plays a sound only you hear                  |
| `daintegrate/handler/chat`    | Says something in chat as you                |

Other mods can add their own handlers, see [Adding your own actions](#adding-your-own-actions).
A trigger whose handler comes from a mod that is not installed still loads: that handler is skipped and kept in the file.

###### Delay

Each handler, in addition to the properties object, has a `delay` property, it indicates after how many ticks the command will be executed.  
By default, 1 second in minecraft is 20 ticks

###### Replace patterns

In commands and messages, you can specify patterns that will be replaced with the corresponding values.

```
{message}
{amount}
{currency}
{username}
{playername}
```

#### Message

Adds a message to your chat

###### Parameters

| Name       | Type     | Description |
| ---------- | -------- | ----------- |
| `message`  | text     | A message which will be added to the chat |

###### Example

```yaml
handlers:
- delay: 10
  properties:
    type: daintegrate/handler/message
    value:
      message: Hello! It's day time!
```

#### Command

Sends a command to the server on your behalf

###### Parameters

| Name       | Type     | Description |
| ---------- | -------- | ----------- |
| `command`  | text     | A command which will be send to the server (**without** slash `/`) |

###### Example

```yaml
handlers:
- delay: 10
  properties:
    type: daintegrate/handler/command
    value:
      command: time set day
```

#### Title

Shows a title and subtitle in the middle of the screen

| Name       | Type     | Description |
| ---------- | -------- | ----------- |
| `title`    | text     | Big line |
| `subtitle` | text     | Small line under it |
| `seconds`  | number   | How long it stays, 3 by default |

```yaml
handlers:
- delay: 0
  properties:
    type: daintegrate/handler/title
    value:
      title: '{username}'
      subtitle: 'spawns a creeper!'
      seconds: 3
```

#### Action bar

Shows `text` above the hotbar for a couple of seconds

#### Sound

Plays a sound only you hear. `sound` is a sound id such as `minecraft:entity.player.levelup`
(the same ids `/playsound` takes), `volume` and `pitch` are 1 by default.

```yaml
handlers:
- delay: 0
  properties:
    type: daintegrate/handler/sound
    value:
      sound: minecraft:entity.generic.explode
      volume: 1
      pitch: 0.8
```

#### Chat

Sends `message` to the chat as you, so everyone on the server sees it

## Adding your own actions

Any Fabric mod can add actions that triggers run, and they show up in the menu next to messages and commands.

1. Depend on this mod and add an entrypoint to your `fabric.mod.json`:

```json
"entrypoints": {
  "daintegrate": [ "com.example.donations.MyActions" ]
}
```

2. Describe the settings of your action as a class with public fields (`String`, `int`, `float`, `double`, `long` or `boolean`).
   Field initialisers are the defaults; the menu shows one input per field.

```java
public class StrikeSettings {
    public int count = 1;
    public boolean damage = false;
}
```

3. Register the action:

```java
public class MyActions implements DaIntegratePlugin {
    @Override
    public void register(IModuleConfiguration configuration) {
        configuration.registerAction("strike", "Lightning strike", StrikeSettings.class, (event, settings) -> {
            // Runs on the game thread. event has the donation: getUserName(), getAmount(), getMessage()...
            var text = ReplaceHelper.replace("{username} sent {amount} {currency}", event, "");
        });
    }
}
```

The action is saved in the settings file as `<your mod id>/handler/strike`. Use `None.class` for an action without settings.
`configuration.registerEventListener(...)` also gets every donation, subscription and Twitch event as it arrives.

## Sub-modifications

// todo: add information abount sub modifications

## Contribute
If you want to contribute, you can:
- Translate or edit translation in the modification [here](Modifications/fabric_1.20/src/main/resources/assets/daintegratew/lang)
- See [here](https://github.com/bummy1337/da-integration/projects/1) what you can do

### TODO for adding new Minecraft version

1. Create a new module in `Modifications`, example: "fabric_1.20"
2. Add module to `Modifications/settings.gradle`
3. Create a integrate machine
   ```java
   var cfgSource = DonationAlertsIntegrateFactory.create(
        "donation-alerts-integrate",
        "settings.yaml",
        LOGGER::info);   
   ```
4. Create basic handlers
   - MessageHandler
   - CommandHandler
5. Register basic handlers
   ```java
   DonationAlertsIntegrate.configure(Constants.ModId, Constants.ModUrl)
        .registerHandler(new MessageHandler())
        .registerHandler(new CommandHandler());
   ```
6. Add information abount update settings file
   ```java
   cfgSource.addListener(settings -> { });
   ```
   **Consider the setting that allows to disable this message!**
7. Create client side commands
   - /da set
   - /da connect
   - /da status
   - /da disconnect
8. Create `DonationAlertsClient`
   ```java
   var eventProcessor = new EventProcessor();
   var listener = new DonationAlertsEventListener(eventProcessor);
   var client = new DonationAlertsClient(Constants.DonationAlertsEventServer, listener);
   ```
   And register the event tick:
   ```java
   ClientTickEvents.END_CLIENT_TICK.register(t -> {
       eventProcessor.evalute();
   });
   ```
9. Call `cfgSource.startListening()` as late as possible.  
   For start watcher on the config file
10. Create welcome message with short guide  
    Example: [fabric_1.20/ClientEntryPoint.java](Modifications/fabric_1.20/src/main/java/net/folleach/daintegrate/fabric/ClientEntryPoint.java#L183-L204)

## Required and used:
Minecraft Fabric: https://fabricmc.net/  
Socket.io: https://github.com/socketio/socket.io-client-java  
Gson: https://github.com/google/gson  
SnakeYAML: https://bitbucket.org/snakeyaml/snakeyaml
