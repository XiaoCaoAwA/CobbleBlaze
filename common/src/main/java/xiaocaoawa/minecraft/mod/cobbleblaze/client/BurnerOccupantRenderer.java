package xiaocaoawa.minecraft.mod.cobbleblaze.client;

import com.cobblemon.mod.common.client.render.ModelLayer;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel;
import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Iterator;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;

/**
 * Draws a single Cobblemon model into a {@link PoseStack} that is already positioned at the
 * burner's block corner. Uses Cobblemon's {@link VaryingModelRepository} exactly like
 * {@code RestorationTankRenderer} does (entityless, via {@link FloatingState}).
 *
 * <p>This is the only draw path: a world-render event positions the stack and calls here. We never
 * touch Create's blaze model, so burner transforms (conductor hat, fluid mode) can't "revert" it,
 * and there is no placement delay.</p>
 */
public final class BurnerOccupantRenderer {

    private final RenderContext context = new RenderContext();

    public BurnerOccupantRenderer() {
        context.put(RenderContext.Companion.getDO_QUIRKS(), Boolean.TRUE);
        context.put(RenderContext.Companion.getRENDER_STATE(), RenderContext.RenderState.BLOCK);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, CobblemonOccupant occupant,
                       FloatingState state, int light, float partialTicks, float playerFacingYaw) {
        state.updatePartialTicks(partialTicks);
        state.setCurrentAspects(occupant.aspects);

        ResourceLocation id = occupant.species;
        PosableModel model = VaryingModelRepository.INSTANCE.getPoser(id, state);
        ResourceLocation texture = VaryingModelRepository.INSTANCE.getTexture(id, state);
        Iterable<ModelLayer> layers = VaryingModelRepository.INSTANCE.getLayers(id, state);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutout(texture));
        state.setCurrentModel(model);
        state.setPoseToFirstSuitable((PoseType) null);

        float scale = CobbleBlaze.config().modelScale * occupant.baseScale;
        float yOffset = CobbleBlaze.config().modelYOffset;
        // Mirror the blaze head: turn to face the player. modelRotation is a manual offset on top of that.
        float yaw = CobbleBlaze.config().modelRotation - playerFacingYaw;

        model.setContext(context);
        context.put(RenderContext.Companion.getTEXTURE(), texture);
        context.put(RenderContext.Companion.getSPECIES(), id);
        context.put(RenderContext.Companion.getRENDER_STATE(), RenderContext.RenderState.BLOCK);
        context.put(RenderContext.Companion.getPOSABLE_STATE(), state);
        context.put(RenderContext.Companion.getSCALE(), scale);

        poseStack.pushPose();
        // Centre on the block, flip to Bedrock handedness, then lift into the burner opening.
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.0, -yOffset, 0.0);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        model.applyAnimations(null, state, 0.0F, 0.0F, state.getAnimationSeconds() * 20.0F, 0.0F, 0.0F);
        model.render(context, poseStack, buffer, light, OverlayTexture.NO_OVERLAY, -1);
        Iterator<ModelLayer> layerIterator = layers.iterator();
        if (layerIterator.hasNext()) {
            model.withLayerContext(bufferSource, state, layers, (Function0<Unit>) () -> {
                model.render(context, poseStack, buffer, light, OverlayTexture.NO_OVERLAY, -1);
                return Unit.INSTANCE;
            });
        }
        model.setDefault();
        poseStack.popPose();
    }
}
