package xiaocaoawa.minecraft.mod.cobbleblaze.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BurnerInteractions;

public final class CobbleBlazeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CobbleBlaze.init(FabricLoader.getInstance().getConfigDir());

        UseBlockCallback.EVENT.register((Player player, Level world, InteractionHand hand, BlockHitResult hitResult) -> {
            boolean handled = BurnerInteractions.onRightClickBlock(
                    player, world, hitResult.getBlockPos(), world.getBlockState(hitResult.getBlockPos()), hand);
            return handled ? InteractionResult.SUCCESS : InteractionResult.PASS;
        });
    }
}
