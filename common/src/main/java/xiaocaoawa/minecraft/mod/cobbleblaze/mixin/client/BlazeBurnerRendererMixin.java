package xiaocaoawa.minecraft.mod.cobbleblaze.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;

/**
 * Suppresses Create's blaze rendering on the <b>Flywheel-off / fallback</b> path, where the BER
 * {@code renderSafe} actually runs. We only cancel (no drawing) when occupied — the Cobblemon
 * model is drawn from the world-render event, the same path used when Flywheel is on, so there is
 * never a double-draw.
 *
 * <p>Contraptions are handled by the dedicated Pokemon burner movement behaviour.</p>
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
}
