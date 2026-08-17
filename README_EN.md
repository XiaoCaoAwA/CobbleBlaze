# CobbleBlaze

> Turn Fire-type Pokémon into Create's combustion power and bring a Pokémon-powered factory to life.

[中文 README](README.md)

## Overview

CobbleBlaze is an addon mod built for **Cobblemon** and **Create**. It adds a dedicated Pokémon Blaze Burner that lets players select a Fire-type Pokémon from their Cobblemon party and place it inside the burner to provide heat for Create machinery and boilers.

The deposited Pokémon keeps its complete data, including its species, form, individual data, moves, and other Cobblemon information. Its original Cobblemon model is rendered inside the burner. When an occupied burner is broken, the Pokémon data is stored in the dropped burner item and restored when the burner is placed again. Sneak-right-clicking an occupied burner returns the Pokémon to the player's party.

This mod was developed as one of the addons for the **Sky Pokémon Factory** modpack. Everyone is welcome to try it. Thanks to the modpack author for open-sourcing and publishing the mod, and special thanks to **Horrrs** for the support.

## Features

- **Dedicated Pokémon Blaze Burner**: a separate block that does not replace or alter Create's normal Blaze Burner behavior.
- **Party selection screen**: right-click an empty-handed burner to open Cobblemon's party selection screen and choose from the current party.
- **Fire-type filtering**: only Pokémon with the Fire type are accepted by default; blacklists and whitelists can further restrict the selection.
- **Complete data preservation**: the full Pokémon NBT is stored while the Pokémon is inside the burner and restored when retrieved.
- **Cobblemon model rendering**: the Pokémon is rendered using Cobblemon's own model path, without replacing assets or showing Create's Blaze head.
- **Configurable continuous heat**: Pokémon above the configured stat threshold can provide a normal heat level without consuming ordinary fuel.
- **Stat-based boiler bonus**: the burner heat multiplier can scale with the Pokémon's six displayed battle stats.
- **Portable burners**: breaking, carrying, and placing an occupied burner preserves its Pokémon.
- **Contraption support**: occupied burners can travel with Create contraptions and trains, with a dedicated model render path.
- **Mechanical Arm support**: Create Mechanical Arms can target the Pokémon burner and insert fuel.
- **Create Crafts & Additions compatibility**: on NeoForge with CCA installed, an occupied burner can be converted with a straw into a liquid-capable Pokémon burner.

## How to use

### Deposit a Pokémon

1. Place a Pokémon Blaze Burner.
2. Keep the target Pokémon in the player's current party and hold an empty hand.
3. Right-click the burner to open Cobblemon's party selection screen.
4. Select a Pokémon that passes the Fire-type and configuration checks.

The selected Pokémon is removed from the party and displayed inside the burner. Each burner can hold one Pokémon.

### Retrieve a Pokémon

Hold an empty hand, sneak, and right-click an occupied burner. The player's party must have an available slot. If the party is full, the Pokémon remains in the burner.

### Add fuel

Right-clicking with a non-empty hand keeps Create's normal burner fuel interaction available. Create-supported ordinary and special fuels can still be inserted. A Pokémon that meets the infinite-burning threshold does not consume ordinary fuel, but special fuel can still be used to reach the `seething` heat level.

### Carry and move the burner

An occupied burner can be mined and carried as an item. Its tooltip shows the stored Pokémon. When installed in a Create contraption or train, the Pokémon model follows the moving structure. Large or unusual models may require a small position adjustment in the config.

## Heat behavior

The Pokémon Blaze Burner uses Create's heat levels. By default, a Fire-type Pokémon whose six-stat total exceeds the configured threshold provides continuous `kindled` heat. If the configured default is `seething`, the infinite-burning path automatically caps it at `kindled`, because `seething` remains a special-fuel heat tier.

The infinite-burning check uses the sum of six displayed battle stats: max HP, Attack, Defence, Special Attack, Special Defence, and Speed. The total must be **strictly greater than** `infiniteBurningStatThreshold`. Pokémon below or equal to the threshold can still use the burner normally, but ordinary fuel is required.

Boiler heat also receives a stat-based multiplier:

```text
heat multiplier = 1 + total stats × boilerHeatBonusPercentPerStat ÷ 100
```

## Create Crafts & Additions compatibility

With **Create Crafts & Additions** installed on **NeoForge 1.21.1**:

- Use a CCA straw on an occupied Pokémon Blaze Burner to convert it into a straw-equipped, liquid-capable Pokémon Blaze Burner.
- The conversion preserves the complete Pokémon data, stat total, and rendered model information.
- The converted burner keeps CCA's liquid tank and can continue to act as a Create boiler heater and fan-processing catalyst.
- A normal CCA Liquid Blaze Burner cannot accept a Pokémon directly; it can only receive one through conversion from an occupied CobbleBlaze burner.
- Retrieving the Pokémon from the straw-equipped version changes the block back into the normal Pokémon Blaze Burner.

CCA support is optional. The normal Pokémon Blaze Burner works without CCA, and the Fabric build skips the CCA-only block and mixins.

## Configuration

The configuration file is generated after the first launch at:

```text
config/cobbleblaze.json
```

Restart the game or server after editing the file. Species can be written as a full ID such as `cobblemon:slugma` or as a path such as `slugma`.

| Option | Default | Description |
| --- | --- | --- |
| `defaultHeatLevel` | `kindled` | Default heat level while a Pokémon occupies the burner. Accepts `none`, `smouldering`, `kindled`, `seething`, or a heat-level index. |
| `speciesHeatLevels` | `{}` | Per-species heat overrides, for example `{ "cobblemon:slugma": "kindled" }`. |
| `blacklistedSpecies` | `[]` | Species that can never be deposited. This takes priority over other allow rules. |
| `allowAnyFireType` | `true` | When `true`, any Fire-type Pokémon is allowed. When `false`, only `whitelistedSpecies` is allowed. |
| `whitelistedSpecies` | `[]` | The species list used when `allowAnyFireType` is `false`. |
| `infiniteBurningStatThreshold` | `1000` | The six-stat total must be strictly greater than this value for ordinary-fuel-free burning. |
| `boilerHeatBonusPercentPerStat` | `0.2` | Additional boiler heat percentage per point of total stats. At the default value, 1000 total stats gives a `3.0x` multiplier. |
| `modelScale` | `0.5` | Global model scale, multiplied by the species' base scale. |
| `modelYOffset` | `0.55` | Vertical model offset inside the burner, in blocks. |
| `modelRotation` | `0.0` | Horizontal model rotation in degrees. |

Example configuration that only allows Slugma and Magcargo:

```json
{
  "defaultHeatLevel": "kindled",
  "speciesHeatLevels": {
    "cobblemon:slugma": "kindled"
  },
  "blacklistedSpecies": [],
  "allowAnyFireType": false,
  "whitelistedSpecies": [
    "cobblemon:slugma",
    "cobblemon:magcargo"
  ],
  "infiniteBurningStatThreshold": 1000,
  "boilerHeatBonusPercentPerStat": 0.2,
  "modelScale": 0.5,
  "modelYOffset": 0.55,
  "modelRotation": 0.0
}
```

## Block and item IDs

| Content | ID | Notes |
| --- | --- | --- |
| Pokémon Blaze Burner | `cobbleblaze:pokemon_blaze_burner` | Available on all supported platforms. |
| Straw-equipped Pokémon Blaze Burner | `cobbleblaze:pokemon_liquid_blaze_burner` | Registered only on NeoForge when CCA is installed. |

The base burner is added to Minecraft's Functional Blocks creative tab. The current version does not include a custom crafting recipe; survival acquisition is controlled by the modpack or server configuration.

## Requirements

- Minecraft **1.21.1**
- Java **21** or newer
- Cobblemon **1.7.1** or newer
- Create **6.0.10** or newer
- Flywheel (provided with the matching Create version)
- Architectury API **13.0.8** or newer
- Fabric: Fabric Loader **0.18.1** or newer and Fabric API
- NeoForge: the **21.1** series
- Optional: Create Crafts & Additions on NeoForge for straw and liquid-burner compatibility

Use platform-specific Cobblemon, Create, and dependency builds for the selected loader. All mods must target the same Minecraft version.

## Installation

1. Install a Fabric or NeoForge instance for Minecraft 1.21.1.
2. Put Cobblemon, Create, Architectury API, the platform dependencies, and CobbleBlaze into the `mods` folder.
3. On NeoForge, install Create Crafts & Additions if liquid-burner compatibility is required.
4. Start the game and verify the burner in the Functional Blocks creative tab or with its item ID.

## Building from source

CobbleBlaze uses Architectury and provides Fabric and NeoForge build targets. On Windows, run:

```powershell
.\gradlew.bat :fabric:remapJar :neoforge:remapJar
```

On Linux, macOS, or Git Bash, run:

```bash
./gradlew :fabric:remapJar :neoforge:remapJar
```

Output files:

```text
fabric/build/libs/cobbleblaze-fabric-1.0.jar
neoforge/build/libs/cobbleblaze-neoforge-1.0.jar
```

Compile-only Create, Flywheel, and Ponder files are kept under `common/libs/`. The game instance still needs Cobblemon, Create, and the appropriate runtime dependencies installed separately.

## Known limitations

- Pokémon can currently be selected only from the player's active party of up to six Pokémon; PC storage selection is not implemented.
- Only Fire-type Pokémon are accepted, subject to the blacklist, whitelist, and `allowAnyFireType` settings.
- The `seething` tier still requires Create special fuel such as Blaze Cakes or ethanol; infinite burning does not permanently maintain it.
- Contraption and train rendering uses a separate model path. Some large or unusual species may need `modelScale` and `modelYOffset` adjustments.
- CCA compatibility is NeoForge-only for Minecraft 1.21.1; Fabric skips those integration mixins and content.
- The current version has no PC selection screen and no custom crafting recipe.

## Source and licensing

CobbleBlaze is provided free of charge with its source publicly available and was created to support the Sky Pokémon Factory modpack. Please download it from reputable sources. If a platform sells this mod as paid content, treat the listing with caution.

The exact permissions for use, modification, and redistribution are defined by the repository's [LICENSE.txt](LICENSE.txt).

## Credits

- Thanks to the developers and maintainers of Cobblemon, Create, Architectury, Flywheel, Ponder, and Create Crafts & Additions.
- Thanks to the Sky Pokémon Factory modpack author for publishing and supporting this addon.
- Special thanks to **Horrrs** for the support.

