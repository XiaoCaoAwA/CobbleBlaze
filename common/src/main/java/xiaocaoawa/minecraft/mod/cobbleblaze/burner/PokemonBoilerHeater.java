package xiaocaoawa.minecraft.mod.cobbleblaze.burner;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;

/** Create boiler heater that scales a Pokemon burner by the stored Pokemon's total stats. */
public final class PokemonBoilerHeater {
    public static final BoilerHeater INSTANCE = PokemonBoilerHeater::getHeat;

    private PokemonBoilerHeater() {}

    public static float getHeat(Level level, BlockPos pos, BlockState state) {
        int baseHeat = BoilerHeaters.blazeBurner(level, pos, state);
        if (baseHeat < 0) {
            return baseHeat;
        }
        if (level.getBlockEntity(pos) instanceof BlazeBurnerOccupant burner
                && burner.cobbleblaze$getOccupant() != null) {
            return baseHeat * CobbleBlaze.config().boilerHeatMultiplier(
                    burner.cobbleblaze$getTotalStats());
        }
        return baseHeat;
    }
}
