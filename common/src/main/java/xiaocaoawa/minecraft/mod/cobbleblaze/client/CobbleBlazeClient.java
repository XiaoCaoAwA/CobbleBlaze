package xiaocaoawa.minecraft.mod.cobbleblaze.client;

import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.client.render.item.CobblemonBuiltinItemRendererRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.OccupantChangeBus;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.CobbleBlazeContent;

/**
 * Client-only bookkeeping for rendering: tracks which loaded burners are occupied (fed by
 * {@link OccupantChangeBus}), keeps a {@link FloatingState} per burner, and exposes the frame
 * entry point called by each platform's world-render hook.
 */
public final class CobbleBlazeClient {

    private static final Set<BlockPos> OCCUPIED = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<BlockPos, FloatingState> STATES = new ConcurrentHashMap<>();
    private static final BurnerOccupantRenderer RENDERER = new BurnerOccupantRenderer();
    private static final PokemonBurnerItemRenderer ITEM_RENDERER = new PokemonBurnerItemRenderer();

    /** Full-bright, matching how Create lights the blaze parts inside the burner. */
    private static final int LIGHT = 0xF000F0;

    private static boolean initialized;

    private CobbleBlazeClient() {}

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        // Create registers its blaze burner on this layer; the inherited OBJ cage needs the same
        // alpha-tested layer or its transparent texture areas render as opaque dark polygons.
        RenderTypeRegistry.register(RenderType.cutoutMipped(), CobbleBlazeContent.POKEMON_BLAZE_BURNER.get());
        CobblemonBuiltinItemRendererRegistry.INSTANCE.register(
                CobbleBlazeContent.POKEMON_BLAZE_BURNER_ITEM.get(), ITEM_RENDERER);

        OccupantChangeBus.setListener((pos, occupant) -> {
            if (occupant == null) {
                OCCUPIED.remove(pos);
                STATES.remove(pos);
            } else {
                OCCUPIED.add(pos);
                STATES.computeIfAbsent(pos, p -> new FloatingState());
            }
        });
    }

    /**
     * Called once per frame from the world-render event. {@code camera} is the camera position;
     * the {@code poseStack} is camera-space (origin at the camera), so we translate by world-minus-camera.
     */
    public static void renderOccupants(Level level, PoseStack poseStack, MultiBufferSource bufferSource,
                                       float partialTick, Vec3 camera) {
        if (OCCUPIED.isEmpty()) {
            return;
        }
        // Snapshot to avoid CME / mutation-during-iteration across the listener.
        List<BlockPos> snapshot = new ArrayList<>(OCCUPIED);
        for (BlockPos pos : snapshot) {
            CobblemonOccupant occupant = readOccupant(level, pos);
            if (occupant == null) {
                OCCUPIED.remove(pos);
                STATES.remove(pos);
                continue;
            }
            FloatingState state = STATES.computeIfAbsent(pos, p -> new FloatingState());

            // Yaw toward the player (same idea as Create's blaze head tracking). Computed from the
            // camera (= local player) position relative to the burner centre.
            double dx = camera.x - (pos.getX() + 0.5);
            double dz = camera.z - (pos.getZ() + 0.5);
            float playerFacingYaw = (float) Math.toDegrees(Math.atan2(dx, dz));

            poseStack.pushPose();
            poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            RENDERER.render(poseStack, bufferSource, occupant, state, LIGHT, partialTick, playerFacingYaw);
            poseStack.popPose();
        }
    }

    private static CobblemonOccupant readOccupant(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlazeBurnerOccupant burner) {
            return burner.cobbleblaze$getOccupant();
        }
        return null;
    }

    // One animation state per moving burner; weak keys release it with the contraption context.
    private static final WeakHashMap<MovementContext, FloatingState> CONTRAPTION_STATES = new WeakHashMap<>();

    /**
     * Draws a Pokemon for a burner riding a contraption. The pose stack is composed by the movement
     * behaviour from Create's view and model stacks, including the actor's local block position.
     */
    public static void renderContraption(MovementContext context, CobblemonOccupant occupant,
                                         PoseStack poseStack, MultiBufferSource bufferSource) {
        FloatingState state = CONTRAPTION_STATES.computeIfAbsent(context, ignored -> new FloatingState());
        RENDERER.render(poseStack, bufferSource, occupant, state, LIGHT, 0.0F, 0.0F);
    }
}
