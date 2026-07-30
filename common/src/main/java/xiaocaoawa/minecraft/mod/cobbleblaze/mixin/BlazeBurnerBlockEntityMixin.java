package xiaocaoawa.minecraft.mod.cobbleblaze.mixin;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.OccupantChangeBus;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.OccupantTransfer;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.Occupants;

/**
 * Adds the "Cobblemon occupant" concept to Create's {@link BlazeBurnerBlockEntity}.
 *
 * <p>Storage: {@code occupant} (render descriptor, synced to client) + {@code fullNbt} (full Pokémon
 * data, server-only). Both ride Create's own {@code write}/{@code read}. {@code getHeatLevel} is
 * overridden so an occupied burner holds a configurable heat level (infinite "power generation").</p>
 *
 * <p>Cross-block transfer (CCA straw converts blaze_burner → LiquidBlazeBurnerBlock, which discards
 * the BE): the occupant is also published to a position-keyed store ({@link OccupantTransfer}) on
 * deposit and (lazily) on tick, so the new liquid burner's tick can reclaim it. We publish from
 * {@code tick} rather than {@code setRemoved} because {@code setRemoved} is an inherited final
 * method that Mixin can't reliably inject.</p>
 */
@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public abstract class BlazeBurnerBlockEntityMixin implements BlazeBurnerOccupant {

    @Unique
    private CobblemonOccupant cobbleblaze$occupant;

    @Unique
    private CompoundTag cobbleblaze$fullNbt;

    @Unique
    private boolean cobbleblaze$transferDirty;

    @Inject(method = "write", at = @At("RETURN"))
    private void cobbleblaze$write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (this.cobbleblaze$occupant != null) {
            tag.put("CobbleBlaze", this.cobbleblaze$occupant.save());
        }
        if (!clientPacket && this.cobbleblaze$fullNbt != null) {
            tag.put("CobbleBlazePokemon", this.cobbleblaze$fullNbt);
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void cobbleblaze$read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        CobblemonOccupant previous = this.cobbleblaze$occupant;
        this.cobbleblaze$occupant = tag.contains("CobbleBlaze")
                ? CobblemonOccupant.load(tag.getCompound("CobbleBlaze"))
                : null;

        if (!clientPacket) {
            this.cobbleblaze$fullNbt = tag.contains("CobbleBlazePokemon")
                    ? tag.getCompound("CobbleBlazePokemon").copy()
                    : null;
            // Re-publish to the transfer store after a server restart (level may be null here, so the
            // actual publish happens on the next tick).
            if (this.cobbleblaze$occupant != null) {
                this.cobbleblaze$transferDirty = true;
            }
        }

        if (previous != this.cobbleblaze$occupant) {
            BlockEntity self = (BlockEntity) (Object) this;
            OccupantChangeBus.fire(self.getBlockPos(), this.cobbleblaze$occupant);
        }
    }

    @Inject(method = "getHeatLevel", at = @At("HEAD"), cancellable = true)
    private void cobbleblaze$getHeatLevel(CallbackInfoReturnable<BlazeBurnerBlock.HeatLevel> cir) {
        if (this.cobbleblaze$occupant != null) {
            cir.setReturnValue(CobbleBlaze.config().heatLevelFor(this.cobbleblaze$occupant.species));
        }
    }

    /** Keep the position-keyed transfer store in sync so a straw conversion can reclaim the occupant. */
    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void cobbleblaze$tickPublish(CallbackInfo ci) {
        if (this.cobbleblaze$occupant != null && this.cobbleblaze$transferDirty) {
            BlockEntity self = (BlockEntity) (Object) this;
            Level level = self.getLevel();
            if (level != null && !level.isClientSide) {
                OccupantTransfer.offer(level.dimension(), self.getBlockPos(), this.cobbleblaze$occupant, this.cobbleblaze$fullNbt);
                this.cobbleblaze$transferDirty = false;
            }
        }
    }

    @Override
    @Unique
    public @Nullable CobblemonOccupant cobbleblaze$getOccupant() {
        return this.cobbleblaze$occupant;
    }

    @Override
    @Unique
    public void cobbleblaze$deposit(@Nullable Pokemon pokemon) {
        BlazeBurnerBlockEntity self = (BlazeBurnerBlockEntity) (Object) this;
        if (pokemon == null) {
            this.cobbleblaze$occupant = null;
            this.cobbleblaze$fullNbt = null;
            Level level = self.getLevel();
            if (level != null && !level.isClientSide) {
                OccupantTransfer.remove(level.dimension(), self.getBlockPos());
            }
        } else {
            Level level = self.getLevel();
            if (level != null) {
                RegistryAccess registries = level.registryAccess();
                this.cobbleblaze$fullNbt = pokemon.saveToNBT(registries, new CompoundTag());
                if (!level.isClientSide) {
                    OccupantTransfer.offer(level.dimension(), self.getBlockPos(), this.cobbleblaze$occupant == null ? Occupants.fromPokemon(pokemon) : this.cobbleblaze$occupant, this.cobbleblaze$fullNbt);
                }
            }
            this.cobbleblaze$occupant = Occupants.fromPokemon(pokemon);
            this.cobbleblaze$transferDirty = true;
        }
        self.updateBlockState();
        self.sendData();
    }

    @Override
    @Unique
    public @Nullable Pokemon cobbleblaze$retrieve() {
        if (this.cobbleblaze$fullNbt == null) {
            return null;
        }
        BlazeBurnerBlockEntity self = (BlazeBurnerBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return null;
        }
        RegistryAccess registries = level.registryAccess();
        Pokemon pokemon = Pokemon.Companion.loadFromNBT(registries, this.cobbleblaze$fullNbt);
        this.cobbleblaze$occupant = null;
        this.cobbleblaze$fullNbt = null;
        this.cobbleblaze$transferDirty = false;
        if (!level.isClientSide) {
            OccupantTransfer.remove(level.dimension(), self.getBlockPos());
        }
        self.updateBlockState();
        self.sendData();
        return pokemon;
    }
}
