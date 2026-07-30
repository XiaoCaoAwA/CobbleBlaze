package xiaocaoawa.minecraft.mod.cobbleblaze;

import java.nio.file.Path;
import xiaocaoawa.minecraft.mod.cobbleblaze.config.CobbleBlazeConfig;

public final class CobbleBlaze {
    public static final String MOD_ID = "cobbleblaze";

    private static volatile CobbleBlazeConfig config;

    /** Called by each platform's main initializer with the game's config directory. */
    public static void init(Path configDir) {
        config = CobbleBlazeConfig.load(configDir);
    }

    /** Always non-null: returns the loaded config, or safe defaults if init hasn't run yet. */
    public static CobbleBlazeConfig config() {
        CobbleBlazeConfig c = config;
        return c != null ? c : CobbleBlazeConfig.defaults();
    }
}
