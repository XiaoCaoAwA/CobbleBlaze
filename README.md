# CobbleBlaze

Use a Cobblemon fire-type Pokémon as the "combustion chamber" inside Create's Blaze Burner —
rendered with Cobblemon's own model, with no asset replacement and none of the flicker/reversion
problems of the old `createslugma` approach.

## How it works

- **No block swap.** The burner stays Create's `BlazeBurnerBlock` the whole time. The occupant
  Pokémon is stored as extra NBT on `BlazeBurnerBlockEntity` (a Mixin field) and rides Create's own
  client-sync, so there is **no placement flicker**.
- **Infinite, configurable heat.** While occupied, the burner holds a configurable `HeatLevel`
  (default `seething`) — that's the "power generation". Removing the Pokémon extinguishes it.
- **Blaze fully suppressed.** Two Mixins hide Create's blaze on every render path:
  - `BlazeBurnerVisual` (Flywheel — the default path) → `setVisible(false)` on every blaze instance.
  - `BlazeBurnerRenderer.renderSafe` (Flywheel-off / fallback) → cancel.
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
| `defaultHeatLevel` | `seething` | Heat level while occupied (`none`/`smouldering`/`fading`/`kindled`/`seething`). |
| `speciesHeatLevels` | `{}` | Per-species overrides, keyed by id (`cobblemon:slugma`) or path (`slugma`). |
| `blacklistedSpecies` | `[]` | Species that can never be deposited. |
| `allowAnyFireType` | `true` | If false, only `whitelistedSpecies` may be deposited. |
| `whitelistedSpecies` | `[]` | Used when `allowAnyFireType` is false. |
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

- The occupant mixin, render suppression, and right-click handling are applied to CCA's
  `LiquidBlazeBurnerBlockEntity`/`Visual`/`Renderer` as well (these mixins are skipped automatically
  when CCA isn't installed).
- **Occupant survives the straw conversion** — CCA rebuilds the block entity without copying NBT, so
  CobbleBlaze hands the occupant off (position-keyed) and the new liquid burner picks it up on its
  first tick. So using the straw no longer "reverts" the blaze or loses the Pokémon.

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
