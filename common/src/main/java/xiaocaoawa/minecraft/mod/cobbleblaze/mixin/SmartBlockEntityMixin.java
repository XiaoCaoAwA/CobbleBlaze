package xiaocaoawa.minecraft.mod.cobbleblaze.mixin;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;

/**
 * At the exact moment a blaze-family burner block entity is removed — notably CCA's straw
 * converting {@code blaze_burner} into {@code LiquidBlazeBurnerBlock}, which discards the old block
 * entity — hand the occupant to the transfer store so the replacement can reclaim it.
 *
 * <p>This mixin targets {@link SmartBlockEntity} (not the burner subclass) because {@code setRemoved}
 * is declared {@code final} here, so Mixin can inject it reliably; injecting on the subclass fails
 * because the method is only inherited. The instanceof guard keeps it a cheap no-op for every other
 * Create block entity. {@code require = 0} makes it a no-op where the vanilla name differs.</p>
 */
@Mixin(value = SmartBlockEntity.class, remap = false)
public abstract class SmartBlockEntityMixin {

    @Inject(method = "setRemoved", at = @At("HEAD"), require = 0)
    private void cobbleblaze$setRemoved(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (self instanceof BlazeBurnerOccupant burner && burner.cobbleblaze$getOccupant() != null) {
            burner.cobbleblaze$publishTransfer();
        }
    }
}
