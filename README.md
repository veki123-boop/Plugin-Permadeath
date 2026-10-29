# Permadeath

A fully configurable Permadeath plugin for Paper/Spigot 1.20+.
Players have a persistent number of lives, and eliminated players can be
revived by paying a configurable diamond cost.

## Features

- **100% configurable** gameplay values, GUI text, lore, colors, messages,
  elimination behavior, ban behavior, and kick behavior.
- Persistent player lives stored by UUID in `data.yml`.
- Configurable maximum lives (default: 3).
- Configurable revival cost (default: 20 diamonds).
- 54-slot chest GUI showing eliminated players, including offline players.
- Safe revival transaction that checks and removes the exact diamond cost.
- Automatic ban pardon when a player is revived.
- Traditional `&` color-code support in every configurable message and GUI text.
- Protection against taking menu items through clicks or inventory dragging.

## Commands and permissions

| Command | Permission | Description |
| --- | --- | --- |
| `/permadeath revive` | `permadeath` | Opens the revival GUI. |
| `/permadeath reload` | `permadeath.admin` | Reloads `config.yml` without restarting the server. |

The command also has the `/pd` alias.

## Configuration

All player-facing text and gameplay options are located in `config.yml`.
The following placeholders are available:

- `{player}`: target player's name.
- `{lives}`: target player's current lives.
- `{cost}`: configured diamond revival cost.
- `{reviver}`: name of the player performing the revival.

When a player dies, one life is removed. When the counter reaches zero, the
plugin sends the configured elimination message and optionally bans and/or
kicks the player. A player with zero lives is listed in the GUI until another
player revives them.

## Building

Requirements:

- Java 17 or newer.
- Maven 3.9 or newer.

Build the plugin with:

```text
mvn package
```

The compiled plugin is generated at `target/Permadeath.jar`. Copy it to the
server's `plugins` directory and restart the server safely.

## Data storage

The plugin creates `plugins/Permadeath/data.yml` automatically. Player lives
are keyed by UUID, so they remain associated with the correct player even if a
player changes their Minecraft name.
