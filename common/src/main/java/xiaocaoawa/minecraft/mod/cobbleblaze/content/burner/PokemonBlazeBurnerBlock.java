package xiaocaoawa.minecraft.mod.cobbleblaze.content.burner;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.CobbleBlazeContent;

/** A dedicated empty cage that only accepts and stores Cobblemon occupants. */
public final class PokemonBlazeBurnerBlock extends BlazeBurnerBlock {
    public static final MapCodec<PokemonBlazeBurnerBlock> CODEC = simpleCodec(PokemonBlazeBurnerBlock::new);

    public PokemonBlazeBurnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends PokemonBlazeBurnerBlock> codec() {
        return CODEC;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Class<BlazeBurnerBlockEntity> getBlockEntityClass() {
        return (Class) PokemonBlazeBurnerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BlazeBurnerBlockEntity> getBlockEntityType() {
        return CobbleBlazeContent.POKEMON_BLAZE_BURNER_ENTITY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PokemonBlazeBurnerBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        CustomData data = context.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
        boolean occupied = data != null && data.contains("CobbleBlaze");
        return defaultBlockState()
                .setValue(HEAT_LEVEL, occupied ? HeatLevel.SMOULDERING : HeatLevel.NONE)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof PokemonBlazeBurnerBlockEntity burner) {
            CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data != null) {
                boolean regeneratePokemonIdentity = placer instanceof Player player && player.hasInfiniteMaterials();
                burner.restoreFromItem(data.copyTag(), regeneratePokemonIdentity);
            }
            burner.refreshHeatState();
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        return List.of(createStack(blockEntity));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level,
                                       BlockPos pos, Player player) {
        return createStack(level.getBlockEntity(pos));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return createStack(level.getBlockEntity(pos));
    }

    private static ItemStack createStack(BlockEntity blockEntity) {
        ItemStack stack = new ItemStack(CobbleBlazeContent.POKEMON_BLAZE_BURNER_ITEM.get());
        if (blockEntity instanceof PokemonBlazeBurnerBlockEntity burner) {
            CompoundTag data = new CompoundTag();
            burner.writeItemData(data);
            if (!data.isEmpty()) {
                BlockItem.setBlockEntityData(stack, burner.getType(), data);
            }
        }
        return stack;
    }
}
