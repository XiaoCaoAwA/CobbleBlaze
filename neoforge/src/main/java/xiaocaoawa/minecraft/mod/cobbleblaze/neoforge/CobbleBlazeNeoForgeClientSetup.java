package xiaocaoawa.minecraft.mod.cobbleblaze.neoforge;

import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.client.CobbleBlazeClient;
import xiaocaoawa.minecraft.mod.cobbleblaze.client.PokemonBurnerItemRenderer;
import xiaocaoawa.minecraft.mod.cobbleblaze.client.PokemonBurnerBlockEntityRenderer;
import com.cobblemon.mod.common.client.render.item.CobblemonBuiltinItemRendererRegistry;
import xiaocaoawa.minecraft.mod.cobbleblaze.neoforge.content.CobbleBlazeNeoForgeContent;
import xiaocaoawa.minecraft.mod.cobbleblaze.neoforge.content.burner.PokemonLiquidBlazeBurnerBlockEntity;

/** Performs client registrations while NeoForge still permits render-layer changes. */
@EventBusSubscriber(modid = CobbleBlaze.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class CobbleBlazeNeoForgeClientSetup {
    private CobbleBlazeNeoForgeClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CobbleBlazeClient.init();
        if (ModList.get().isLoaded("createaddition")) {
            RenderTypeRegistry.register(
                    RenderType.cutoutMipped(),
                    CobbleBlazeNeoForgeContent.POKEMON_LIQUID_BLAZE_BURNER.get());
            BlockEntityRendererRegistry.register(
                    CobbleBlazeNeoForgeContent.POKEMON_LIQUID_BLAZE_BURNER_ENTITY.get(),
                    context -> new PokemonBurnerBlockEntityRenderer<PokemonLiquidBlazeBurnerBlockEntity>());
            CobblemonBuiltinItemRendererRegistry.INSTANCE.register(
                    CobbleBlazeNeoForgeContent.POKEMON_LIQUID_BLAZE_BURNER_ITEM.get(),
                    new PokemonBurnerItemRenderer());
        }
    }
}
