package xiaocaoawa.minecraft.mod.cobbleblaze.content.burner;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;

public class PokemonBlazeBurnerBlockItem extends BlockItem {
    public PokemonBlazeBurnerBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null || !data.contains("CobbleBlaze")) {
            return;
        }
        CobblemonOccupant occupant = CobblemonOccupant.load(data.copyTag().getCompound("CobbleBlaze"));
        Component speciesName = Component.translatableWithFallback(
                "cobblemon.species." + occupant.species.getPath() + ".name",
                occupant.species.toString());
        tooltip.add(Component.translatable("cobbleblaze.tooltip.occupant", speciesName)
                .withStyle(ChatFormatting.GRAY));
    }
}
