package xiaocaoawa.minecraft.mod.cobbleblaze.mixin.client;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerVisual;
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
 * Suppresses Create's blaze rendering on the <b>Flywheel</b> path (the default in 1.21.1 Create).
 * When a burner is occupied we hide every blaze instance (head, goggles, hat, rods, flame) via
 * {@code setVisible(false)} and skip the per-frame animation; the Cobblemon model is drawn
 * separately from the world-render event. This is suppression only — no drawing here.
 *
 * <p>Without this mixin the blaze would render <em>on top of</em> the Cobblemon whenever Flywheel
 * is enabled, because {@code BlazeBurnerRenderer} (the BER) is skipped entirely when Flywheel is on.</p>
 */
@Mixin(value = BlazeBurnerVisual.class, remap = false)
public abstract class BlazeBurnerVisualMixin {

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
    private BlazeBurnerBlockEntity cobbleblaze$blockEntity;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cobbleblaze$init(VisualizationContext context, BlazeBurnerBlockEntity blockEntity, float partialTick, CallbackInfo ci) {
        this.cobbleblaze$blockEntity = blockEntity;
    }

    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true)
    private void cobbleblaze$beginFrame(DynamicVisual.Context context, CallbackInfo ci) {
        boolean occupied = this.cobbleblaze$blockEntity instanceof BlazeBurnerOccupant burner
                && burner.cobbleblaze$getOccupant() != null;
        // NONE = empty cage. Normal burners are always SMOULDERING+ (Create's getHeatLevel never
        // returns NONE for a block entity), so this only hides a stale BE left after retrieve.
        boolean noneHeat = this.cobbleblaze$blockEntity.getHeatLevelFromBlock() == BlazeBurnerBlock.HeatLevel.NONE;
        boolean visible = !occupied && !noneHeat;
        setVisible(this.head, visible);
        setVisible(this.goggles, visible);
        setVisible(this.hat, visible);
        setVisible(this.flame, visible);
        setVisible(this.smallRods, visible);
        setVisible(this.largeRods, visible);
        if (occupied) {
            // Skip Create's per-frame animation/positioning; instances are hidden anyway.
            ci.cancel();
        }
    }

    private static void setVisible(Instance instance, boolean visible) {
        if (instance != null) {
            instance.setVisible(visible);
        }
    }
}
