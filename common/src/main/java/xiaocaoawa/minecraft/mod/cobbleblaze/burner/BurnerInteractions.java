package xiaocaoawa.minecraft.mod.cobbleblaze.burner;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.callback.PartySelectCallbacks;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import java.util.ArrayList;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;

/**
 * Server-side handling of right-clicking a blaze burner: open Cobblemon's party-select screen to
 * deposit a fire-type Cobblemon, or (sneaking) retrieve the one inside.
 *
 * <p>Triggered by each platform's interact event; returns {@code true} when it consumes the click.</p>
 *
 * <p>Note on Create's empty cage: a {@code blaze_burner} at {@code HeatLevel.NONE} returns {@code null}
 * from {@code newBlockEntity} — it has no block entity until a blaze is captured (SMOULDERING+). So
 * depositing into an empty cage first flips it to SMOULDERING (creating the BE), then stores the
 * cobblemon. See {@link #ensureBurnerEntity}.</p>
 */
public final class BurnerInteractions {

    private BurnerInteractions() {}

    public static boolean onRightClickBlock(Player player, Level level, BlockPos pos, BlockState state, InteractionHand hand) {
        boolean isCreateBurner = state.getBlock() instanceof BlazeBurnerBlock;
        BlockEntity be = level.getBlockEntity(pos);
        boolean hasOccupantBe = be instanceof BlazeBurnerOccupant;
        // Handle Create's blaze_burner (even the empty cage) and any block whose BE is ours (CCA liquid burner).
        if (!isCreateBurner && !hasOccupantBe) {
            return false;
        }
        // Only an empty hand opens our UI, so Create's fuel-feeding (which needs an item) is untouched.
        if (!player.getItemInHand(hand).isEmpty()) {
            return false;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide) {
            return false;
        }

        boolean hasCobblemon = hasOccupantBe && ((BlazeBurnerOccupant) be).cobbleblaze$getOccupant() != null;

        // Sneak-right-click retrieves the cobblemon (if any); otherwise pass through.
        if (player.isShiftKeyDown()) {
            return hasCobblemon && tryRetrieve((BlazeBurnerOccupant) be, serverPlayer);
        }

        // Any relevant burner without a cobblemon can accept one.
        if (hasCobblemon) {
            return false;
        }
        return openSelection(serverPlayer, level, pos);
    }

    /**
     * Returns the {@link BlazeBurnerOccupant} at {@code pos}, creating one if this is Create's empty
     * cage (NONE, no block entity) by flipping it to SMOULDERING.
     */
    private static BlazeBurnerOccupant ensureBurnerEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlazeBurnerOccupant occupant) {
            return occupant;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BlazeBurnerBlock block) || !state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            return null;
        }
        // Empty cage has no BE; flip to SMOULDERING so Create creates a BlazeBurnerBlockEntity.
        BlockState lit = state.setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SMOULDERING);
        level.setBlock(pos, lit, 3);
        be = level.getBlockEntity(pos);
        if (be instanceof BlazeBurnerOccupant occupant) {
            return occupant;
        }
        // Fallback: force-create the block entity.
        BlockEntity created = block.newBlockEntity(pos, lit);
        if (created != null) {
            level.setBlockEntity(created);
            if (created instanceof BlazeBurnerOccupant occupant) {
                return occupant;
            }
        }
        return null;
    }

    private static boolean openSelection(ServerPlayer player, Level level, BlockPos pos) {
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        List<Pokemon> candidates = new ArrayList<>();
        for (Pokemon pokemon : party) {
            candidates.add(pokemon);
        }
        if (candidates.isEmpty()) {
            player.sendSystemMessage(Component.translatable("cobbleblaze.message.no_pokemon"));
            return true;
        }

        PartySelectCallbacks.INSTANCE.createFromPokemon(
                player,
                Component.translatable("cobbleblaze.gui.select_pokemon"),
                candidates,
                (Function1<Pokemon, Boolean>) pokemon -> Occupants.isFireType(pokemon)
                        && CobbleBlaze.config().isAllowed(pokemon.getSpecies().getResourceIdentifier()),
                (Function1<ServerPlayer, Unit>) sp -> Unit.INSTANCE,
                (Function1<Pokemon, Unit>) chosen -> {
                    BlazeBurnerOccupant burner = ensureBurnerEntity(level, pos);
                    if (burner != null) {
                        burner.cobbleblaze$deposit(chosen);
                        party.remove(chosen);
                        player.sendSystemMessage(Component.translatable(
                                "cobbleblaze.message.deposited", chosen.getSpecies().getName()));
                    }
                    return Unit.INSTANCE;
                }
        );
        return true;
    }

    private static boolean tryRetrieve(BlazeBurnerOccupant burner, ServerPlayer player) {
        if (burner == null || burner.cobbleblaze$getOccupant() == null) {
            player.sendSystemMessage(Component.translatable("cobbleblaze.message.empty"));
            return true;
        }
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        PartyPosition slot = party.getFirstAvailablePosition();
        if (slot == null) {
            player.sendSystemMessage(Component.translatable("cobbleblaze.message.party_full"));
            return true;
        }
        Pokemon pokemon = burner.cobbleblaze$retrieve();
        if (pokemon == null) {
            return true;
        }
        party.set(slot, pokemon);
        player.sendSystemMessage(Component.translatable(
                "cobbleblaze.message.retrieved", pokemon.getSpecies().getName()));
        return true;
    }
}
