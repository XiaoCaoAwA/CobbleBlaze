package xiaocaoawa.minecraft.mod.cobbleblaze.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import xiaocaoawa.minecraft.mod.cobbleblaze.client.CobbleBlazeClient;

public final class CobbleBlazeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CobbleBlazeClient.init();

        // Draw the occupant after the entity pass. Create's own blaze is suppressed by the
        // BlazeBurnerVisual/BlazeBurnerRenderer mixins; this is the single draw path.
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            CobbleBlazeClient.renderOccupants(
                    context.world(),
                    context.matrixStack(),
                    context.consumers(),
                    context.tickCounter().getGameTimeDeltaPartialTick(false),
                    context.camera().getPosition()
            );
        });
    }
}
