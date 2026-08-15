package xiaocaoawa.minecraft.mod.cobbleblaze.content.burner;

import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mechanical Arm support for the Pokemon burners.
 *
 * <p>Create's built-in {@code BlazeBurnerType} (and CCA's {@code LiquidBlazeBurnerType}) only accept
 * their exact registered block, so our subclasses are invisible to the arm. This type mirrors Create's
 * deposit-only burner point but matches on a caller-supplied predicate and delegates fuel insertion
 * to a caller-supplied {@link Inserter} (e.g. {@link BlazeBurnerBlock#tryInsert}).
 */
public final class PokemonBlazeBurnerArmInteraction {
    private PokemonBlazeBurnerArmInteraction() {}

    /** Mirrors the signature of {@code BlazeBurnerBlock.tryInsert} / {@code LiquidBlazeBurnerBlock.tryInsert}. */
    @FunctionalInterface
    public interface Inserter {
        InteractionResultHolder<ItemStack> tryInsert(BlockState state, Level level, BlockPos pos, ItemStack stack,
                                                     boolean doNotConsume, boolean forceOverflow, boolean simulate);
    }

    /** Type for the plain Pokemon blaze burner, backed by Create's {@link BlazeBurnerBlock#tryInsert}. */
    public static Type forBlazeBurner() {
        return new Type(state -> state.getBlock() instanceof PokemonBlazeBurnerBlock, BlazeBurnerBlock::tryInsert);
    }

    public static final class Type extends ArmInteractionPointType {
        private final Predicate<BlockState> matcher;
        private final Inserter inserter;

        public Type(Predicate<BlockState> matcher, Inserter inserter) {
            this.matcher = matcher;
            this.inserter = inserter;
        }

        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return matcher.test(state);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new Point(this, level, pos, state, inserter);
        }
    }

    public static final class Point extends AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint {
        private final Inserter inserter;

        public Point(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state, Inserter inserter) {
            super(type, level, pos, state);
            this.inserter = inserter;
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            ItemStack input = stack.copy();
            InteractionResultHolder<ItemStack> res =
                    inserter.tryInsert(cachedState, level, pos, input, false, false, simulate);
            ItemStack remainder = res.getObject();
            if (input.isEmpty()) {
                return remainder;
            }
            if (!simulate) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
            }
            return input;
        }
    }
}
