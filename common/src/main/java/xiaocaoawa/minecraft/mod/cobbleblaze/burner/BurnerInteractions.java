package xiaocaoawa.minecraft.mod.cobbleblaze.burner;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.callback.PartySelectCallbacks;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.ArrayList;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.burner.PokemonBlazeBurnerBlock;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.burner.PokemonBlazeBurnerBlockEntity;

/**
 * Server-side handling of right-clicking a Pokemon blaze burner: open Cobblemon's party-select screen to
 * deposit a fire-type Cobblemon, or (sneaking) retrieve the one inside.
 *
 * <p>Triggered by each platform's interact event; returns {@code true} when it consumes the click.</p>
 *
 * <p>The regular Create blaze burner is deliberately ignored. The dedicated CobbleBlaze block has
 * a block entity even while empty, so depositing never needs to create or light a Create burner.</p>
 */
public final class BurnerInteractions {
    private static final ResourceLocation CCA_STRAW =
            ResourceLocation.fromNamespaceAndPath("createaddition", "straw");
    private static final ResourceLocation CCA_LIQUID_BURNER =
            ResourceLocation.fromNamespaceAndPath("createaddition", "liquid_blaze_burner");
    private static final ResourceLocation POKEMON_LIQUID_BURNER =
            ResourceLocation.fromNamespaceAndPath(CobbleBlaze.MOD_ID, "pokemon_liquid_blaze_burner");

    private BurnerInteractions() {}

    public static boolean onRightClickBlock(Player player, Level level, BlockPos pos, BlockState state, InteractionHand hand) {
        boolean isPokemonBurner = state.getBlock() instanceof PokemonBlazeBurnerBlock;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        boolean isPokemonLiquidBurner = blockId.equals(POKEMON_LIQUID_BURNER);
        BlockEntity be = level.getBlockEntity(pos);
        boolean hasOccupantBe = be instanceof BlazeBurnerOccupant;
        boolean hasCobblemon = hasOccupantBe
                && ((BlazeBurnerOccupant) be).cobbleblaze$getOccupant() != null;
        ItemStack heldItem = player.getItemInHand(hand);

        // Convert into CobbleBlaze's dedicated liquid Pokemon burner. It subclasses CCA's liquid
        // burner on NeoForge, so it has the same fluid tank without becoming a blaze-head burner.
        if (isPokemonBurner && hasCobblemon && isCcaStraw(heldItem)) {
            if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide) {
                return false;
            }
            return attachCcaStraw(serverPlayer, level, pos, state, hand, (BlazeBurnerOccupant) be);
        }

        // Never open the Pokemon selector for CCA's ordinary liquid blaze burner. Legacy worlds
        // may still contain one of the old converted occupants, so allow only retrieval from it.
        if (!isPokemonBurner && !isPokemonLiquidBurner) {
            boolean legacyOccupiedLiquid = blockId.equals(CCA_LIQUID_BURNER) && hasCobblemon;
            if (legacyOccupiedLiquid && heldItem.isEmpty() && player.isShiftKeyDown()
                    && player instanceof ServerPlayer serverPlayer && !level.isClientSide) {
                return tryRetrieve((BlazeBurnerOccupant) be, serverPlayer);
            }
            return false;
        }
        // Only an empty hand opens our UI, so Create's fuel-feeding (which needs an item) is untouched.
        if (!heldItem.isEmpty()) {
            return false;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide) {
            return false;
        }

        // Sneak-right-click retrieves the cobblemon (if any); otherwise pass through.
        if (player.isShiftKeyDown()) {
            return hasCobblemon && tryRetrieve((BlazeBurnerOccupant) be, serverPlayer);
        }

        // A straw-equipped Pokemon burner only keeps the Pokemon it was converted with. It must
        // not accept a new Pokemon directly after being emptied or obtained through commands.
        if (isPokemonLiquidBurner) {
            return false;
        }

        // Any relevant burner without a cobblemon can accept one.
        if (hasCobblemon) {
            return false;
        }
        return openSelection(serverPlayer, level, pos);
    }

    private static boolean isCcaStraw(ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(CCA_STRAW);
    }

    private static boolean attachCcaStraw(ServerPlayer player, Level level, BlockPos pos, BlockState state,
                                          InteractionHand hand, BlazeBurnerOccupant burner) {
        if (!BuiltInRegistries.BLOCK.containsKey(POKEMON_LIQUID_BURNER)) {
            return false;
        }
        Block liquidBurner = BuiltInRegistries.BLOCK.get(POKEMON_LIQUID_BURNER);
        BlockState liquidState = liquidBurner.defaultBlockState();
        if (liquidState.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            liquidState = liquidState.setValue(
                    BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SMOULDERING);
        }
        if (state.hasProperty(BlazeBurnerBlock.FACING)
                && liquidState.hasProperty(BlazeBurnerBlock.FACING)) {
            liquidState = liquidState.setValue(
                    BlazeBurnerBlock.FACING, state.getValue(BlazeBurnerBlock.FACING));
        }

        burner.cobbleblaze$publishTransfer();
        if (!level.setBlockAndUpdate(pos, liquidState)) {
            OccupantTransfer.remove(level.dimension(), pos);
            return false;
        }
        if (!player.isCreative()) {
            player.getItemInHand(hand).shrink(1);
        }
        return true;
    }

    private static BlazeBurnerOccupant ensureBurnerEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PokemonBlazeBurnerBlockEntity occupant) {
            return occupant;
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
                    if (burner != null && burner.cobbleblaze$getOccupant() == null) {
                        burner.cobbleblaze$deposit(chosen);
                        if (burner.cobbleblaze$getOccupant() != null) {
                            party.remove(chosen);
                            player.sendSystemMessage(Component.translatable(
                                    "cobbleblaze.message.deposited", chosen.getSpecies().getName()));
                        }
                    } else if (burner != null) {
                        player.sendSystemMessage(Component.translatable("cobbleblaze.message.occupied"));
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
