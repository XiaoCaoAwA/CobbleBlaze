package xiaocaoawa.minecraft.mod.cobbleblaze.burner;

import net.minecraft.core.BlockPos;

/**
 * Decouples the (both-side) {@code BlazeBurnerBlockEntityMixin} from the (client-only) render
 * registry. The block-entity mixin fires changes through here; the client registers a listener to
 * maintain the set of occupied burners to render. On the dedicated server the listener stays null
 * and the calls are no-ops.
 */
public final class OccupantChangeBus {

    @FunctionalInterface
    public interface Listener {
        void onOccupantChanged(BlockPos pos, CobblemonOccupant occupant);
    }

    private static volatile Listener listener;

    private OccupantChangeBus() {}

    public static void setListener(Listener listener) {
        OccupantChangeBus.listener = listener;
    }

    public static void fire(BlockPos pos, CobblemonOccupant occupant) {
        Listener l = listener;
        if (l != null) {
            l.onOccupantChanged(pos, occupant);
        }
    }
}
