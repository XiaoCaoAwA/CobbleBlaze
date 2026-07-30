package xiaocaoawa.minecraft.mod.cobbleblaze.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerRenderer;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
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
 * Suppresses CCA's liquid-blaze-burner BER rendering when occupied (mirror of
 * {@link BlazeBurnerRendererMixin}), and draws the cobblemon on contraptions. Only applied when
 * CCA is present.
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

    @Inject(method = "renderInContraption", at = @At("HEAD"), cancellable = true)
    private static void cobbleblaze$renderInContraption(MovementContext context, VirtualRenderWorld virtualWorld,
                                                 ContraptionMatrices matrices, MultiBufferSource bufferSource,
                                                 LerpedFloat headAngle, boolean trainHat, CallbackInfo ci) {
        CompoundTag data = context.blockEntityData;
        if (data == null || !data.contains("CobbleBlaze")) {
            return;
        }
        CobblemonOccupant occupant = CobblemonOccupant.load(data.getCompound("CobbleBlaze"));
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(matrices.getViewProjection().last().pose());
        poseStack.mulPose(matrices.getModel().last().pose());
        CobbleBlazeClient.renderContraption(context, occupant, poseStack, bufferSource);
        ci.cancel();
    }
}

