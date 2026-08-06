package xiaocaoawa.minecraft.mod.cobbleblaze.content.burner;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.OccupantChangeBus;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.OccupantTransfer;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.Occupants;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.CobbleBlazeContent;

/** Stores one complete Pokemon and exposes its heat to Create machinery. */
public final class PokemonBlazeBurnerBlockEntity extends BlazeBurnerBlockEntity implements BlazeBurnerOccupant {
    private CobblemonOccupant occupant;
    private CompoundTag fullNbt;

    public PokemonBlazeBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(CobbleBlazeContent.POKEMON_BLAZE_BURNER_ENTITY.get(), pos, state);
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (occupant != null) {
            tag.put("CobbleBlaze", occupant.save());
        }
        if (!clientPacket && fullNbt != null) {
            tag.put("CobbleBlazePokemon", fullNbt.copy());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        occupant = tag.contains("CobbleBlaze")
                ? CobblemonOccupant.load(tag.getCompound("CobbleBlaze"))
                : null;
        if (!clientPacket) {
            fullNbt = tag.contains("CobbleBlazePokemon")
                    ? tag.getCompound("CobbleBlazePokemon").copy()
                    : null;
        }
        OccupantChangeBus.fire(getBlockPos(), occupant);
    }

    @Override
    protected BlazeBurnerBlock.HeatLevel getHeatLevel() {
        return occupant == null
                ? BlazeBurnerBlock.HeatLevel.NONE
                : CobbleBlaze.config().heatLevelFor(occupant.species);
    }

    @Override
    protected boolean tryUpdateFuel(ItemStack stack, boolean forceOverflow, boolean simulate) {
        return false;
    }

    @Override
    public @Nullable CobblemonOccupant cobbleblaze$getOccupant() {
        return occupant;
    }

    @Override
    public void cobbleblaze$deposit(@Nullable Pokemon pokemon) {
        if (pokemon == null) {
            occupant = null;
            fullNbt = null;
        } else {
            Level level = getLevel();
            if (level == null || level.isClientSide) {
                return;
            }
            fullNbt = pokemon.saveToNBT(level.registryAccess(), new CompoundTag());
            occupant = Occupants.fromPokemon(pokemon);
        }
        refreshHeatState();
    }

    @Override
    public @Nullable Pokemon cobbleblaze$retrieve() {
        Level level = getLevel();
        if (fullNbt == null || level == null) {
            return null;
        }
        RegistryAccess registries = level.registryAccess();
        Pokemon pokemon = Pokemon.Companion.loadFromNBT(registries, fullNbt);
        if (pokemon == null) {
            return null;
        }
        occupant = null;
        fullNbt = null;
        if (!level.isClientSide) {
            OccupantTransfer.remove(level.dimension(), getBlockPos());
        }
        refreshHeatState();
        return pokemon;
    }

    @Override
    public void cobbleblaze$publishTransfer() {
        Level level = getLevel();
        if (level != null && !level.isClientSide && occupant != null && fullNbt != null) {
            OccupantTransfer.offer(level.dimension(), getBlockPos(), occupant, fullNbt.copy());
        }
    }

    public void refreshHeatState() {
        updateBlockState();
        setChanged();
        sendData();
    }
}
