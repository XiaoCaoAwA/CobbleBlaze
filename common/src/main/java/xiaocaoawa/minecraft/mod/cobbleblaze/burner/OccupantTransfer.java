package xiaocaoawa.minecraft.mod.cobbleblaze.burner;

import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Server-side bridge that carries an occupant across a block-type swap — specifically CCA's straw
 * converting Create's {@code BlazeBurnerBlock} into its {@code LiquidBlazeBurnerBlock}, which CCA
 * does WITHOUT copying block-entity NBT. The removed burner offers its occupant here; the freshly
 * created liquid burner claims it on its first tick.
 *
 * <p>Keyed by dimension + position so it survives the (near-instant) block swap.</p>
 */
public final class OccupantTransfer {

    public record Payload(CobblemonOccupant descriptor, CompoundTag fullNbt) {}

    private static final ConcurrentHashMap<String, Payload> PENDING = new ConcurrentHashMap<>();

    private OccupantTransfer() {}

    private static String key(ResourceKey<Level> dimension, BlockPos pos) {
        return dimension.location().toString() + "@" + pos;
    }

    public static void offer(ResourceKey<Level> dimension, BlockPos pos, CobblemonOccupant descriptor, CompoundTag fullNbt) {
        if (descriptor != null) {
            PENDING.put(key(dimension, pos), new Payload(descriptor, fullNbt));
            System.out.println("[CobbleBlaze] transfer offer: " + descriptor.species + " @ " + pos);
        }
    }

    public static Payload take(ResourceKey<Level> dimension, BlockPos pos) {
        Payload p = PENDING.remove(key(dimension, pos));
        System.out.println("[CobbleBlaze] transfer take @ " + pos + " -> " + (p == null ? "none" : p.descriptor().species));
        return p;
    }

    public static void remove(ResourceKey<Level> dimension, BlockPos pos) {
        PENDING.remove(key(dimension, pos));
    }
}
