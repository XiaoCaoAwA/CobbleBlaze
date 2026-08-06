package xiaocaoawa.minecraft.mod.cobbleblaze.content.burner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.processing.burner.BlazeBurnerMovementBehaviour;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.client.CobbleBlazeClient;

/** Movement behaviour dedicated to Pokemon burners; it never invokes Create's blaze renderer. */
public final class PokemonBlazeBurnerMovementBehaviour extends BlazeBurnerMovementBehaviour {

    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld virtualWorld,
                                    ContraptionMatrices matrices, MultiBufferSource bufferSource) {
        CompoundTag data = context.blockEntityData;
        if (data == null || !data.contains("CobbleBlaze")) {
            return;
        }

        CobblemonOccupant occupant = CobblemonOccupant.load(data.getCompound("CobbleBlaze"));
        PoseStack poseStack = new PoseStack();
        // Create has already translated the model stack to this actor's local block position.
        ContraptionMatrices.transform(poseStack, matrices.getViewProjection());
        ContraptionMatrices.transform(poseStack, matrices.getModel());
        CobbleBlazeClient.renderContraption(context, occupant, poseStack, bufferSource);
    }
}
