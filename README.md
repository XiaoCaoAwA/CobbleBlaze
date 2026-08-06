# CobbleBlaze

Use a Cobblemon fire-type Pokémon as the occupant of a dedicated Pokémon Blaze Burner —
rendered with Cobblemon's own model, with no asset replacement and none of the flicker/reversion
problems of the old `createslugma` approach.

## How it works

- **Dedicated empty chamber.** `cobbleblaze:pokemon_blaze_burner` is registered as a completely
  separate block and appears in the Functional Blocks creative tab. Regular Create blaze burners
  are no longer used for Pokémon.
- **Portable occupants.** Breaking an occupied Pokémon Blaze Burner drops the same burner with the
  complete Pokémon stored in its block-entity-data component. Placing it again restores that exact
  Pokémon and immediately renders its model.
- **Infinite, configurable normal heat.** A qualifying occupant holds configurable normal heat
  (default `kindled`). `seething` still requires timed special fuel such as a Blaze Cake or ethanol.
- **No blaze head.** The dedicated block uses only Create's empty cage model. Its own block-entity
  type is not registered with Create's blaze renderer, while compatibility Mixins suppress the
  blaze on CCA and moving-contraption render paths.
- **Cobblemon drawn by us.** A world-render event draws the occupant model via Cobblemon's
  `VaryingModelRepository` (same path as the restoration tank). One draw path, never fights Create's
  transforms, so conductor-hat / fluid modes can't "revert" it.

## Controls

- **Right-click the burner with an empty hand** → opens Cobblemon's party-select screen. Only
  fire-type (and config-allowed) Pokémon are selectable. The chosen Pokémon is deposited and removed
  from your party (full data preserved).
- **Sneak + right-click with an empty hand** → returns the Pokémon to your party.

## Config (`config/cobbleblaze.json`)

| Key | Default | Meaning |
|---|---|---|
| `defaultHeatLevel` | `kindled` | Normal heat level for stat-based infinite burning. `seething` is reserved for timed special fuel. |
| `speciesHeatLevels` | `{}` | Per-species overrides, keyed by id (`cobblemon:slugma`) or path (`slugma`). |
| `blacklistedSpecies` | `[]` | Species that can never be deposited. |
| `allowAnyFireType` | `true` | If false, only `whitelistedSpecies` may be deposited. |
| `whitelistedSpecies` | `[]` | Used when `allowAnyFireType` is false. |
| `infiniteBurningStatThreshold` | `1000` | Six battle stats must be strictly greater than this value for infinite burning; at or below it, normal fuel is required. |
| `modelScale` | `0.5` | Render-size multiplier (× the species' baseScale). **Tune visually.** |
| `modelYOffset` | `0.55` | Vertical offset of the model inside the burner. **Tune visually.** |
| `modelRotation` | `0.0` | Y rotation of the model (degrees). |

## Building

```
./gradlew :fabric:remapJar :neoforge:remapJar
```

Outputs: `fabric/build/libs/cobbleblaze-fabric-1.0.jar`, `neoforge/build/libs/cobbleblaze-neoforge-1.0.jar`.

Compile dependencies are bundled locally under `common/libs/` (Create, Flywheel, Ponder extracted
from Create's JarInJar). At runtime you need Create + Cobblemon (+ Flywheel/Ponder, which ship with
Create). The provided Create jar is the **NeoForge** build — use the NeoForge jar for testing; for
Fabric testing you need the Fabric builds of Create/Flywheel/Ponder/Cobblemon.

## Create Crafts & Additions (CCA) compat

CCA's "straw" converts a blaze burner into a parallel `LiquidBlazeBurnerBlock` (its own
BE/Visual/Renderer, not extending Create's). CobbleBlaze now supports that variant too:

- An occupied Pokémon Blaze Burner accepts CCA's straw and becomes CCA's fluid-capable
  `LiquidBlazeBurnerBlock`, preserving the Pokémon and rendering its model instead of a blaze.
- A normal CCA liquid blaze burner cannot accept Pokémon directly. Its occupant can only come from
  the occupied Pokémon-burner straw conversion above.
- **Occupant survives the straw conversion** — CCA rebuilds the block entity without copying NBT, so
  CobbleBlaze hands the occupant off (position-keyed) and the new liquid burner picks it up on its
  first tick.

## Contraptions (trains)

Burners riding a contraption are rendered through Create's `renderInContraption`. CobbleBlaze reads
the occupant from the contraption's stored block-entity data and draws the Cobblemon there (same for
CCA's liquid burner on contraptions), instead of letting the blaze render. This is best-effort — the
on-train model placement may need a small config tweak.

## v1 scope / known limitations

- **Party selection only** (≤6). Selecting from the PC is not in v1.
- **Model placement** uses sensible defaults but the exact fit (`modelScale`/`modelYOffset`) and the
  player-facing/head-tracking sign usually need a small tweak in the config.
- **CCA is NeoForge-only** for 1.21.1; the CCA compat mixins apply there. On Fabric they are skipped.
