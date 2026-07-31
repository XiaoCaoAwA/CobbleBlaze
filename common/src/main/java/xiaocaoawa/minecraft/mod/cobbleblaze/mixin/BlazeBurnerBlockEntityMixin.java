package xiaocaoawa.minecraft.mod.cobbleblaze.mixin;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
 * data, server-only), both riding Create's own {@code write}/{@code read}. {@code getHeatLevel} is
 * overridden so an occupied burner holds a configurable heat level. The cross-block transfer on a
 * straw conversion is handled by {@link SmartBlockEntityMixin} + {@link #cobbleblaze$publishTransfer()}.</p>
 */
@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public abstract class BlazeBurnerBlockEntityMixin implements BlazeBurnerOccupant {

    @Unique
    private CobblemonOccupant cobbleblaze$occupant;

    @Unique
    private CompoundTag cobbleblaze$fullNbt;

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
        } else {
            Level level = self.getLevel();
            if (level != null) {
                this.cobbleblaze$fullNbt = pokemon.saveToNBT(level.registryAccess(), new CompoundTag());
            }
            this.cobbleblaze$occupant = Occupants.fromPokemon(pokemon);
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
        RegistryAccess registries = level == null ? null : level.registryAccess();
        Pokemon pokemon = registries == null ? null : Pokemon.Companion.loadFromNBT(registries, this.cobbleblaze$fullNbt);
        this.cobbleblaze$occupant = null;
        this.cobbleblaze$fullNbt = null;
        // Sync the cleared occupant so the client stops rendering the cobblemon BEFORE we drop the BE.
        self.sendData();
        // Revert to a true empty cage: force NONE heat (don't use updateBlockState — getHeatLevel
        // would return SMOULDERING for any BE), then drop the block entity entirely.
        if (level != null && !level.isClientSide) {
            OccupantTransfer.remove(level.dimension(), self.getBlockPos());
            BlockState state = self.getBlockState();
            if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
                level.setBlock(self.getBlockPos(), state.setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE), 3);
            }
            level.removeBlockEntity(self.getBlockPos());
        }
        return pokemon;
    }

    @Override
    @Unique
    public void cobbleblaze$publishTransfer() {
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level != null && !level.isClientSide && this.cobbleblaze$occupant != null) {
            OccupantTransfer.offer(level.dimension(), self.getBlockPos(), this.cobbleblaze$occupant, this.cobbleblaze$fullNbt);
        }
    }
}
