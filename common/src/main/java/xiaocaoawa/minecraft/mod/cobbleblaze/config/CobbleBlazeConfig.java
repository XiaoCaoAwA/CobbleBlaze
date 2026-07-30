package xiaocaoawa.minecraft.mod.cobbleblaze.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/**
 * Tunable behaviour for CobbleBlaze. Stored as JSON under {@code config/cobbleblaze.json}
 * so server operators (and the player) can edit it without recompiling.
 *
 * <p>The headline behaviour: a deposited fire-type Cobblemon keeps the burner at a configurable
 * {@link BlazeBurnerBlock.HeatLevel HeatLevel} indefinitely (infinite "power generation"), at a
 * level that can be customised globally or per-species.</p>
 */
public final class CobbleBlazeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "cobbleblaze.json";

    /** Heat level a burner holds while occupied, unless overridden per-species. */
    public String defaultHeatLevel = "seething";

    /** Per-species overrides, keyed by either full id ("cobblemon:slugma") or path ("slugma"). */
    public Map<String, String> speciesHeatLevels = new HashMap<>();

    /** Species that may never be deposited, even if fire-type. */
    public List<String> blacklistedSpecies = new ArrayList<>();

    /** If true, any fire-type Cobblemon may be deposited. If false, only whitelisted ones. */
    public boolean allowAnyFireType = true;

    /** Explicitly allowed species (used when allowAnyFireType is false). */
    public List<String> whitelistedSpecies = new ArrayList<>();

    /** Global multiplier on the rendered model size (multiplied by the species' baseScale). */
    public float modelScale = 0.5F;

    /** Vertical offset (blocks) of the rendered model inside the burner. Tune visually. */
    public float modelYOffset = 0.55F;

    /** Y-rotation (degrees) of the rendered model. */
    public float modelRotation = 0.0F;

    public static CobbleBlazeConfig defaults() {
        return new CobbleBlazeConfig();
    }

    public static CobbleBlazeConfig load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        CobbleBlazeConfig cfg;
        if (Files.exists(file)) {
            try {
                String json = Files.readString(file);
                cfg = GSON.fromJson(json, CobbleBlazeConfig.class);
                if (cfg == null) {
                    cfg = defaults();
                }
            } catch (Exception e) {
                System.err.println("[CobbleBlaze] Failed to read config, using defaults: " + e);
                cfg = defaults();
            }
        } else {
            cfg = defaults();
        }
        cfg.save(configDir);
        return cfg;
    }

    public void save(Path configDir) {
        try {
            Path dir = configDir.resolve(FILE_NAME).getParent();
            if (dir != null) Files.createDirectories(dir);
            Files.writeString(configDir.resolve(FILE_NAME), GSON.toJson(this));
        } catch (Exception e) {
            System.err.println("[CobbleBlaze] Failed to write config: " + e);
        }
    }

    /** Resolves the heat level a burner should hold while occupied by the given species. */
    public BlazeBurnerBlock.HeatLevel heatLevelFor(ResourceLocation species) {
        String key = species.toString();
        String path = species.getPath();
        String name = null;
        if (speciesHeatLevels != null) {
            if (speciesHeatLevels.containsKey(key)) name = speciesHeatLevels.get(key);
            else if (speciesHeatLevels.containsKey(path)) name = speciesHeatLevels.get(path);
        }
        if (name == null) name = defaultHeatLevel;
        return parseHeatLevel(name);
    }

    public boolean isAllowed(ResourceLocation species) {
        String key = species.toString();
        String path = species.getPath();
        if (blacklistedSpecies != null && (blacklistedSpecies.contains(key) || blacklistedSpecies.contains(path))) {
            return false;
        }
        if (allowAnyFireType) return true;
        return whitelistedSpecies != null && (whitelistedSpecies.contains(key) || whitelistedSpecies.contains(path));
    }

    private static BlazeBurnerBlock.HeatLevel parseHeatLevel(String name) {
        if (name == null || name.isBlank()) return BlazeBurnerBlock.HeatLevel.SEETHING;
        try {
            return BlazeBurnerBlock.HeatLevel.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            // fall through to byIndex
        }
        try {
            int idx = Integer.parseInt(name);
            return BlazeBurnerBlock.HeatLevel.byIndex(idx);
        } catch (NumberFormatException ignored) {
            // unknown
        }
        return BlazeBurnerBlock.HeatLevel.SEETHING;
    }
}
