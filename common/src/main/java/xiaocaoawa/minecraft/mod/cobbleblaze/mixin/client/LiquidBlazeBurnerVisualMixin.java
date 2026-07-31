package xiaocaoawa.minecraft.mod.cobbleblaze.mixin.client;

import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerVisual;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.ScrollInstance;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;

/**
 * Suppresses CCA's liquid-blaze-burner rendering (the Flywheel path) when occupied — the mirror of
 * {@link BlazeBurnerVisualMixin} for Create's burner. Only applied when CCA is present.
 */
@Mixin(value = LiquidBlazeBurnerVisual.class, remap = false)
public abstract class LiquidBlazeBurnerVisualMixin {

    @Shadow
    private TransformedInstance head;

    @Shadow
    private TransformedInstance goggles;

    @Shadow
    private TransformedInstance hat;

    @Shadow
    private ScrollInstance flame;

    @Shadow
    private TransformedInstance smallRods;

    @Shadow
    private TransformedInstance largeRods;

    @Unique
    private LiquidBlazeBurnerBlockEntity cobbleblaze$blockEntity;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cobbleblaze$init(VisualizationContext context, LiquidBlazeBurnerBlockEntity blockEntity, float partialTick, CallbackInfo ci) {
        this.cobbleblaze$blockEntity = blockEntity;
    }

    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true)
    private void cobbleblaze$beginFrame(DynamicVisual.Context context, CallbackInfo ci) {
        boolean occupied = this.cobbleblaze$blockEntity instanceof BlazeBurnerOccupant burner
                && burner.cobbleblaze$getOccupant() != null;
        boolean noneHeat = this.cobbleblaze$blockEntity.getHeatLevelFromBlock() == BlazeBurnerBlock.HeatLevel.NONE;
        boolean visible = !occupied && !noneHeat;
        setVisible(this.head, visible);
        setVisible(this.goggles, visible);
        setVisible(this.hat, visible);
        setVisible(this.flame, visible);
        setVisible(this.smallRods, visible);
        setVisible(this.largeRods, visible);
        if (occupied) {
            ci.cancel();
        }
    }

    private static void setVisible(Instance instance, boolean visible) {
        if (instance != null) {
            instance.setVisible(visible);
        }
    }
}
