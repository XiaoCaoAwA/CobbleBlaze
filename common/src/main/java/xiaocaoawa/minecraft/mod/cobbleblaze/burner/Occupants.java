package xiaocaoawa.minecraft.mod.cobbleblaze.burner;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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

    /** Returns the six displayed battle stats, using max HP rather than current HP. */
    public static int totalStats(Pokemon pokemon) {
        return pokemon.getMaxHealth()
                + pokemon.getAttack()
                + pokemon.getDefence()
                + pokemon.getSpecialAttack()
                + pokemon.getSpecialDefence()
                + pokemon.getSpeed();
    }

    public static int totalStats(HolderLookup.Provider registries, CompoundTag nbt) {
        if (!(registries instanceof RegistryAccess registryAccess) || nbt == null || nbt.isEmpty()) {
            return 0;
        }
        try {
            return totalStats(Pokemon.Companion.loadFromNBT(registryAccess, nbt));
        } catch (RuntimeException ignored) {
            return 0;
        }
    }
}
