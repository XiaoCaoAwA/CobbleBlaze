package xiaocaoawa.minecraft.mod.cobbleblaze.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;

/**
 * Suppresses CCA's stationary liquid-blaze-burner renderer when occupied. Moving burners are
 * handled by the dedicated Pokemon burner movement behaviour; this mixin only applies when CCA exists.
 */
@Mixin(value = LiquidBlazeBurnerRenderer.class, remap = false)
public abstract class LiquidBlazeBurnerRendererMixin {

    @Inject(method = "renderSafe", at = @At("HEAD"), cancellable = true)
    private void cobbleblaze$renderSafe(LiquidBlazeBurnerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                                        MultiBufferSource bufferSource, int light, int overlay, CallbackInfo ci) {
        if (blockEntity instanceof BlazeBurnerOccupant burner && burner.cobbleblaze$getOccupant() != null) {
            ci.cancel();
        }
    }
}
