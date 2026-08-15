package xiaocaoawa.minecraft.mod.cobbleblaze.neoforge.content.burner;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.OccupantTransfer;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.Occupants;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.CobbleBlazeContent;
import xiaocaoawa.minecraft.mod.cobbleblaze.neoforge.content.CobbleBlazeNeoForgeContent;

/** A Pokemon occupant plus CCA's real liquid tank and liquid-burning implementation. */
public final class PokemonLiquidBlazeBurnerBlockEntity extends LiquidBlazeBurnerBlockEntity
        implements BlazeBurnerOccupant {
    private CobblemonOccupant occupant;
    private CompoundTag fullNbt;
    private int totalStats;
    private boolean initialSyncPending;
    private boolean transferClaimAttempted;

    public PokemonLiquidBlazeBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(CobbleBlazeNeoForgeContent.POKEMON_LIQUID_BLAZE_BURNER_ENTITY.get(), pos, state);
    }

    @Override
    public void tick() {
        claimTransferredOccupant();
        super.tick();
        Level level = getLevel();
        if (initialSyncPending && level != null && !level.isClientSide) {
            initialSyncPending = false;
            sendData();
        }
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (occupant != null) {
            tag.put("CobbleBlaze", occupant.save());
            tag.putInt("CobbleBlazeTotalStats", totalStats);
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
            totalStats = tag.contains("CobbleBlazeTotalStats")
                    ? tag.getInt("CobbleBlazeTotalStats")
                    : Occupants.totalStats(registries, fullNbt);
        } else {
            totalStats = tag.getInt("CobbleBlazeTotalStats");
        }
    }

    @Override
    protected BlazeBurnerBlock.HeatLevel getHeatLevelFromFuelType(FuelType fuelType) {
        if (occupant == null) {
            return BlazeBurnerBlock.HeatLevel.NONE;
        }
        return CobbleBlaze.config().hasInfiniteBurning(totalStats)
                && fuelType != FuelType.SPECIAL
                ? CobbleBlaze.config().infiniteHeatLevelFor(occupant.species)
                : super.getHeatLevelFromFuelType(fuelType);
    }

    @Override
    public @Nullable CobblemonOccupant cobbleblaze$getOccupant() {
        return occupant;
    }

    @Override
    public int cobbleblaze$getTotalStats() {
        return totalStats;
    }

    @Override
    public void cobbleblaze$deposit(@Nullable Pokemon pokemon) {
        // This block can only inherit a Pokemon from an occupied chamber during straw conversion.
        if (pokemon != null) {
            return;
        }
        occupant = null;
        fullNbt = null;
        totalStats = 0;
        refreshState();
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
        totalStats = 0;
        if (!level.isClientSide) {
            OccupantTransfer.remove(level.dimension(), getBlockPos());
            BlockState emptyState = CobbleBlazeContent.POKEMON_BLAZE_BURNER.get().defaultBlockState();
            if (getBlockState().hasProperty(BlazeBurnerBlock.FACING)
                    && emptyState.hasProperty(BlazeBurnerBlock.FACING)) {
                emptyState = emptyState.setValue(
                        BlazeBurnerBlock.FACING, getBlockState().getValue(BlazeBurnerBlock.FACING));
            }
            level.setBlock(getBlockPos(), emptyState, 3);
        }
        return pokemon;
    }

    @Override
    public void cobbleblaze$publishTransfer() {
        Level level = getLevel();
        if (level != null && !level.isClientSide && occupant != null && fullNbt != null) {
            OccupantTransfer.offer(level.dimension(), getBlockPos(), occupant, fullNbt.copy());
        }
    }

    public IFluidHandler cobbleblaze$getFluidHandler() {
        return tankInventory;
    }

    /** Runs once on the first server tick; never polls afterwards (see OccupantTransfer). */
    private void claimTransferredOccupant() {
        if (transferClaimAttempted) {
            return;
        }
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        transferClaimAttempted = true;
        if (occupant != null) {
            return;
        }
        OccupantTransfer.Payload payload = OccupantTransfer.take(level.dimension(), getBlockPos());
        if (payload == null) {
            return;
        }
        occupant = payload.descriptor();
        fullNbt = payload.fullNbt().copy();
        totalStats = Occupants.totalStats(level.registryAccess(), fullNbt);
        refreshState();
    }

    public void refreshState() {
        updateBlockState();
        setChanged();
        sendData();
    }

    /** Restores the occupant immediately on both placement sides, before the server update arrives. */
    public void restoreFromItem(CompoundTag tag, boolean regeneratePokemonIdentity) {
        if (!tag.contains("CobbleBlaze")) {
            return;
        }
        occupant = CobblemonOccupant.load(tag.getCompound("CobbleBlaze"));
        totalStats = tag.getInt("CobbleBlazeTotalStats");

        Level level = getLevel();
        if (level == null || !level.isClientSide) {
            fullNbt = tag.contains("CobbleBlazePokemon")
                    ? tag.getCompound("CobbleBlazePokemon").copy()
                    : null;
            if (regeneratePokemonIdentity && fullNbt != null && level != null) {
                Pokemon pokemon = Pokemon.Companion.loadFromNBT(level.registryAccess(), fullNbt);
                Pokemon copy = pokemon.clone(true, level.registryAccess());
                fullNbt = copy.saveToNBT(level.registryAccess(), new CompoundTag());
            }
            initialSyncPending = true;
        }
    }

    public void writeItemData(CompoundTag tag, HolderLookup.Provider registries) {
        if (occupant == null || fullNbt == null) {
            return;
        }
        tag.put("CobbleBlaze", occupant.save());
        tag.putInt("CobbleBlazeTotalStats", totalStats);
        tag.put("CobbleBlazePokemon", fullNbt.copy());
        tag.put("TankContent", tankInventory.writeToNBT(registries, new CompoundTag()));
    }

}
