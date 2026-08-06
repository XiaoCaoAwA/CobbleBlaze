package xiaocaoawa.minecraft.mod.cobbleblaze.mixin;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import xiaocaoawa.minecraft.mod.cobbleblaze.content.CobbleBlazeContent;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.burner.PokemonBlazeBurnerBlockEntity;

/**
 * Same occupant behaviour as {@link PokemonBlazeBurnerBlockEntity}, applied to Create Crafts &amp;
 * Additions' {@code LiquidBlazeBurnerBlockEntity} (the fluid-drinking variant the "straw" converts
 * a burner into). CCA's liquid burner is a parallel implementation (its own BE/Visual/Renderer),
 * so without this mixin the cobblemon would vanish and the blaze would return after using the straw.
 *
 * <p>Only loaded/applied when CCA is present (target class absent → Mixin skips it).</p>
 */
@Mixin(value = LiquidBlazeBurnerBlockEntity.class, remap = false)
public abstract class LiquidBlazeBurnerBlockEntityMixin implements BlazeBurnerOccupant {

    @Unique
    private CobblemonOccupant cobbleblaze$occupant;

    @Unique
    private int cobbleblaze$totalStats;

    @Unique
    private CompoundTag cobbleblaze$fullNbt;

    @Inject(method = "write", at = @At("RETURN"))
    private void cobbleblaze$write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (this.cobbleblaze$occupant != null) {
            tag.put("CobbleBlaze", this.cobbleblaze$occupant.save());
            tag.putInt("CobbleBlazeTotalStats", this.cobbleblaze$totalStats);
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
            BlockEntity self = (BlockEntity) (Object) this;
            this.cobbleblaze$totalStats = tag.contains("CobbleBlazeTotalStats")
                    ? tag.getInt("CobbleBlazeTotalStats")
                    : Occupants.totalStats(
                            self.getLevel() == null ? null : self.getLevel().registryAccess(),
                            this.cobbleblaze$fullNbt);
        } else {
            this.cobbleblaze$totalStats = tag.getInt("CobbleBlazeTotalStats");
        }
        if (previous != this.cobbleblaze$occupant) {
            BlockEntity self = (BlockEntity) (Object) this;
            OccupantChangeBus.fire(self.getBlockPos(), this.cobbleblaze$occupant);
        }
    }

    /** CCA's liquid burner derives its heat level from its fuel type; override it like Create's. */
    @Inject(method = "getHeatLevelFromFuelType", at = @At("HEAD"), cancellable = true, remap = false)
    private void cobbleblaze$getHeatLevelFromFuelType(LiquidBlazeBurnerBlockEntity.FuelType fuelType, CallbackInfoReturnable<BlazeBurnerBlock.HeatLevel> cir) {
        if (this.cobbleblaze$occupant != null
                && CobbleBlaze.config().hasInfiniteBurning(this.cobbleblaze$totalStats)
                && fuelType != LiquidBlazeBurnerBlockEntity.FuelType.SPECIAL) {
            cir.setReturnValue(CobbleBlaze.config().infiniteHeatLevelFor(this.cobbleblaze$occupant.species));
        }
    }

    /**
     * Claim an occupant handed off by a burner that was just converted into this one (CCA straw).
     * Runs on the first server tick after creation, since the fresh liquid burner loads no NBT.
     */
    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void cobbleblaze$tickClaim(CallbackInfo ci) {
        if (this.cobbleblaze$occupant == null) {
            BlockEntity self = (BlockEntity) (Object) this;
            Level level = self.getLevel();
            if (level != null && !level.isClientSide) {
                OccupantTransfer.Payload payload = OccupantTransfer.take(level.dimension(), self.getBlockPos());
                if (payload != null) {
                    System.out.println("[CobbleBlaze] liquid tick-claim: reclaimed occupant " + payload.descriptor().species + " at " + self.getBlockPos());
                    this.cobbleblaze$occupant = payload.descriptor();
                    this.cobbleblaze$fullNbt = payload.fullNbt();
                    this.cobbleblaze$totalStats = Occupants.totalStats(level.registryAccess(), this.cobbleblaze$fullNbt);
                    LiquidBlazeBurnerBlockEntity be = (LiquidBlazeBurnerBlockEntity) (Object) this;
                    be.updateBlockState();
                    be.sendData();
                }
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
        // Liquid blaze burners may only receive a Pokemon through OccupantTransfer during an
        // occupied Pokemon burner + straw conversion. Direct party deposits are forbidden.
        if (pokemon != null) {
            return;
        }
        LiquidBlazeBurnerBlockEntity self = (LiquidBlazeBurnerBlockEntity) (Object) this;
        this.cobbleblaze$occupant = null;
        this.cobbleblaze$fullNbt = null;
        this.cobbleblaze$totalStats = 0;
        self.updateBlockState();
        self.sendData();
    }

    @Override
    @Unique
    public @Nullable Pokemon cobbleblaze$retrieve() {
        if (this.cobbleblaze$fullNbt == null) {
            return null;
        }
        LiquidBlazeBurnerBlockEntity self = (LiquidBlazeBurnerBlockEntity) (Object) this;
        Level level = self.getLevel();
        RegistryAccess registries = level == null ? null : level.registryAccess();
        Pokemon pokemon = registries == null ? null : Pokemon.Companion.loadFromNBT(registries, this.cobbleblaze$fullNbt);
        this.cobbleblaze$occupant = null;
        this.cobbleblaze$fullNbt = null;
        self.sendData();
        // Return to CobbleBlaze's dedicated empty chamber, never Create's empty blaze burner.
        if (level != null && !level.isClientSide) {
            OccupantTransfer.remove(level.dimension(), self.getBlockPos());
            level.setBlock(self.getBlockPos(), CobbleBlazeContent.POKEMON_BLAZE_BURNER.get().defaultBlockState(), 3);
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
