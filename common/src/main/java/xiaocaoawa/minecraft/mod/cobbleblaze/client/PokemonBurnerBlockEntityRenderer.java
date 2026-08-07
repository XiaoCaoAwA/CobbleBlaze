package xiaocaoawa.minecraft.mod.cobbleblaze.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.Vec3;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;

/** Renders a Pokemon directly from its owning burner block entity. */
public final class PokemonBurnerBlockEntityRenderer<T extends net.minecraft.world.level.block.entity.BlockEntity
        & BlazeBurnerOccupant> implements BlockEntityRenderer<T> {
    private static final int FULL_BRIGHT = 0xF000F0;
    private final BurnerOccupantRenderer occupantRenderer = new BurnerOccupantRenderer();
    private final Map<T, FloatingState> states = new WeakHashMap<>();

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int light, int overlay) {
        CobblemonOccupant occupant = blockEntity.cobbleblaze$getOccupant();
        if (occupant == null) {
            return;
        }
        FloatingState state = states.computeIfAbsent(blockEntity, ignored -> new FloatingState());
        occupantRenderer.render(poseStack, bufferSource, occupant, state, FULL_BRIGHT, partialTick, 0.0F);
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPosition) {
        return true;
    }
}
