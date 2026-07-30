package xiaocaoawa.minecraft.mod.cobbleblaze.burner;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/** Helpers for translating between live {@link Pokemon} instances and our render descriptor. */
public final class Occupants {

    private Occupants() {}

    public static CobblemonOccupant fromPokemon(Pokemon pokemon) {
        ResourceLocation species = pokemon.getSpecies().getResourceIdentifier();
        Set<String> aspects = new LinkedHashSet<>(pokemon.getAspects());
        float baseScale = pokemon.getForm().getBaseScale();
        return new CobblemonOccupant(species, aspects, baseScale);
    }

    public static boolean isFireType(Pokemon pokemon) {
        for (ElementalType type : pokemon.getTypes()) {
            if (type.equals(ElementalTypes.FIRE)) {
                return true;
            }
        }
        return false;
    }
}
