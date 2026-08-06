package xiaocaoawa.minecraft.mod.cobbleblaze.neoforge;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.client.CobbleBlazeClient;

/**
 * Draws occupant Cobblemon models from the world-render pass. Create's own blaze is suppressed by
 * the BlazeBurnerVisual/BlazeBurnerRenderer mixins; this is the single draw path.
 */
@EventBusSubscriber(modid = CobbleBlaze.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class CobbleBlazeNeoForgeClient {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        CobbleBlazeClient.renderOccupants(
                mc.level,
                event.getPoseStack(),
                mc.renderBuffers().bufferSource(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false),
                event.getCamera().getPosition()
        );
    }
}
