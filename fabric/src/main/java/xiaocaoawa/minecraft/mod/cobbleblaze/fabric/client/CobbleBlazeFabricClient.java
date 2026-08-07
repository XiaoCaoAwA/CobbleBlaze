package xiaocaoawa.minecraft.mod.cobbleblaze.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import xiaocaoawa.minecraft.mod.cobbleblaze.client.CobbleBlazeClient;

public final class CobbleBlazeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CobbleBlazeClient.init();
    }
}
