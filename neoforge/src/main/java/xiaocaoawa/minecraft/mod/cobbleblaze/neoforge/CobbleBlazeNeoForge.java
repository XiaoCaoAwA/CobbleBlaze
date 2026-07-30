package xiaocaoawa.minecraft.mod.cobbleblaze.neoforge;

import net.minecraft.world.InteractionResult;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BurnerInteractions;

@Mod(CobbleBlaze.MOD_ID)
public final class CobbleBlazeNeoForge {
    public CobbleBlazeNeoForge() {
        CobbleBlaze.init(FMLPaths.CONFIGDIR.get());

        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, this::onRightClickBlock);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        boolean handled = BurnerInteractions.onRightClickBlock(
                event.getEntity(),
                event.getLevel(),
                event.getPos(),
                event.getLevel().getBlockState(event.getPos()),
                event.getHand());
        if (handled) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
