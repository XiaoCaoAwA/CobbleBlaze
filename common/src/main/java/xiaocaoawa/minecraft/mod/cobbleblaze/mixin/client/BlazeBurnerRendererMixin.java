package xiaocaoawa.minecraft.mod.cobbleblaze.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerRenderer;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.client.CobbleBlazeClient;

/**
 * Suppresses Create's blaze rendering on the <b>Flywheel-off / fallback</b> path, where the BER
 * {@code renderSafe} actually runs. We only cancel (no drawing) when occupied — the Cobblemon
 * model is drawn from the world-render event, the same path used when Flywheel is on, so there is
 * never a double-draw.
 *
 * <p>Also handles <b>contraptions</b> (trains): {@code renderInContraption} is the only render path
 * used for moving burners regardless of Flywheel, so we draw the cobblemon here too.</p>
 */
@Mixin(value = BlazeBurnerRenderer.class, remap = false)
public abstract class BlazeBurnerRendererMixin {

    @Inject(method = "renderSafe", at = @At("HEAD"), cancellable = true)
    private void cobbleblaze$renderSafe(BlazeBurnerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                                        MultiBufferSource bufferSource, int light, int overlay, CallbackInfo ci) {
        if (blockEntity instanceof BlazeBurnerOccupant burner && burner.cobbleblaze$getOccupant() != null) {
            ci.cancel();
        }
    }

    @Inject(method = "renderInContraption", at = @At("HEAD"), cancellable = true)
    private static void cobbleblaze$renderInContraption(MovementContext context, VirtualRenderWorld virtualWorld,
                                                 ContraptionMatrices matrices, MultiBufferSource bufferSource,
                                                 LerpedFloat headAngle, boolean trainHat, CallbackInfo ci) {
        CompoundTag data = context.blockEntityData;
        if (data == null || !data.contains("CobbleBlaze")) {
            return;
        }
        CobblemonOccupant occupant = CobblemonOccupant.load(data.getCompound("CobbleBlaze"));
        // renderShared uses ms = viewProjection for the draw and model for the buffer transform, so the
        // combined transform placing vertices at the block is viewProjection × model.
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(matrices.getViewProjection().last().pose());
        poseStack.mulPose(matrices.getModel().last().pose());
        CobbleBlazeClient.renderContraption(context, occupant, poseStack, bufferSource);
        ci.cancel();
    }
}

