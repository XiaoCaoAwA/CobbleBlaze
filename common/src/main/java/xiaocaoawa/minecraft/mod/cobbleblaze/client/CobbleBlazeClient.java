package xiaocaoawa.minecraft.mod.cobbleblaze.client;

import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.client.render.item.CobblemonBuiltinItemRendererRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.CobbleBlazeContent;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.burner.PokemonBlazeBurnerBlockEntity;

/** Client registrations and the animation state used by moving Pokemon burners. */
public final class CobbleBlazeClient {

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
        BlockEntityRendererRegistry.register(
                CobbleBlazeContent.POKEMON_BLAZE_BURNER_ENTITY.get(),
                context -> new PokemonBurnerBlockEntityRenderer<PokemonBlazeBurnerBlockEntity>());
        CobblemonBuiltinItemRendererRegistry.INSTANCE.register(
                CobbleBlazeContent.POKEMON_BLAZE_BURNER_ITEM.get(), ITEM_RENDERER);
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
