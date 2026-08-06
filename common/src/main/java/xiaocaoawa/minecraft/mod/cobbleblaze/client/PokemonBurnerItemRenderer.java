package xiaocaoawa.minecraft.mod.cobbleblaze.client;

import com.cobblemon.mod.common.client.render.item.CobblemonBuiltinItemRenderer;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;

/** Draws a burner item as its cage plus the Cobblemon stored in its block-entity data. */
public final class PokemonBurnerItemRenderer implements CobblemonBuiltinItemRenderer {
    private final BurnerOccupantRenderer occupantRenderer = new BurnerOccupantRenderer();

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack poseStack,
                        MultiBufferSource bufferSource, int light, int overlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        BlockState state = blockItem.getBlock().defaultBlockState();
        Minecraft.getInstance().getBlockRenderer()
                .renderSingleBlock(state, poseStack, bufferSource, light, overlay);

        CobblemonOccupant occupant = readOccupant(stack);
        if (occupant != null) {
            occupantRenderer.render(
                    poseStack,
                    bufferSource,
                    occupant,
                    new FloatingState(),
                    light,
                    0.0F,
                    0.0F);
        }
    }

    private static CobblemonOccupant readOccupant(ItemStack stack) {
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null || !data.contains("CobbleBlaze")) {
            return null;
        }
        return CobblemonOccupant.load(data.copyTag().getCompound("CobbleBlaze"));
    }
}
