package xiaocaoawa.minecraft.mod.cobbleblaze.burner;

import com.cobblemon.mod.common.pokemon.Pokemon;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented (via Mixin) onto Create's {@code BlazeBurnerBlockEntity}. Lets non-mixin code read
 * and change the occupant by casting the block entity to this interface.
 *
 * <p>{@code deposit}/{@code retrieve} are server-side only; {@code getOccupant} is safe on both
 * sides (the descriptor is synced to the client for rendering).</p>
 */
public interface BlazeBurnerOccupant {

    /** The render descriptor, or {@code null} if the burner is empty. */
    @Nullable
    CobblemonOccupant cobbleblaze$getOccupant();

    /** Stores the given Pokémon in the burner (full data preserved), persists + syncs. Pass {@code null} to clear. */
    void cobbleblaze$deposit(@Nullable Pokemon pokemon);

    /** Removes and returns the stored Pokémon (rebuilt from its saved data), or {@code null} if empty. */
    @Nullable
    Pokemon cobbleblaze$retrieve();
}
