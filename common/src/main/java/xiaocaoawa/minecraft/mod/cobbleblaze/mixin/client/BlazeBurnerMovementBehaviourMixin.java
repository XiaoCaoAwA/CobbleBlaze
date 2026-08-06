package xiaocaoawa.minecraft.mod.cobbleblaze.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.processing.burner.BlazeBurnerMovementBehaviour;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.client.CobbleBlazeClient;

/** Renders Pokemon occupants before Create skips cold burners with a NONE heat level. */
@Mixin(value = BlazeBurnerMovementBehaviour.class, remap = false)
public abstract class BlazeBurnerMovementBehaviourMixin {

    @Inject(method = "renderInContraption", at = @At("HEAD"), cancellable = true)
    private void cobbleblaze$renderInContraption(MovementContext context, VirtualRenderWorld virtualWorld,
                                                  ContraptionMatrices matrices, MultiBufferSource bufferSource,
                                                  CallbackInfo ci) {
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
