package xiaocaoawa.minecraft.mod.cobbleblaze.content.burner;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.BlazeBurnerOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.CobblemonOccupant;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.OccupantTransfer;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.Occupants;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.CobbleBlazeContent;

/** Stores one complete Pokemon and exposes its heat to Create machinery. */
public final class PokemonBlazeBurnerBlockEntity extends BlazeBurnerBlockEntity implements BlazeBurnerOccupant {
    private CobblemonOccupant occupant;
    private CompoundTag fullNbt;
    private int totalStats;
    private boolean initialSyncPending;

    public PokemonBlazeBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(CobbleBlazeContent.POKEMON_BLAZE_BURNER_ENTITY.get(), pos, state);
    }

    @Override
    public void tick() {
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
    protected BlazeBurnerBlock.HeatLevel getHeatLevel() {
        if (occupant == null) {
            return BlazeBurnerBlock.HeatLevel.NONE;
        }
        return CobbleBlaze.config().hasInfiniteBurning(totalStats)
                && activeFuel != BlazeBurnerBlockEntity.FuelType.SPECIAL
                ? CobbleBlaze.config().infiniteHeatLevelFor(occupant.species)
                : super.getHeatLevel();
    }

    @Override
    protected boolean tryUpdateFuel(ItemStack stack, boolean forceOverflow, boolean simulate) {
        if (occupant == null) {
            return false;
        }
        if (CobbleBlaze.config().hasInfiniteBurning(totalStats)
                && !isSuperheatedFuel(stack)) {
            return false;
        }
        return super.tryUpdateFuel(stack, forceOverflow, simulate);
    }

    private static boolean isSuperheatedFuel(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(
                ResourceLocation.fromNamespaceAndPath("create", "blaze_cake"));
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
        if (pokemon == null) {
            occupant = null;
            fullNbt = null;
            totalStats = 0;
        } else {
            Level level = getLevel();
            if (level == null || level.isClientSide) {
                return;
            }
            fullNbt = pokemon.saveToNBT(level.registryAccess(), new CompoundTag());
            occupant = Occupants.fromPokemon(pokemon);
            totalStats = Occupants.totalStats(pokemon);
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
        totalStats = 0;
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
            // Placement's initial block update can precede BLOCK_ENTITY_DATA loading. Resend after
            // the block entity has completed its first server tick so the client cannot retain null.
            initialSyncPending = true;
        }
    }

    public void writeItemData(CompoundTag tag) {
        if (occupant == null || fullNbt == null) {
            return;
        }
        tag.put("CobbleBlaze", occupant.save());
        tag.putInt("CobbleBlazeTotalStats", totalStats);
        if (fullNbt != null) {
            tag.put("CobbleBlazePokemon", fullNbt.copy());
        }
    }

}
