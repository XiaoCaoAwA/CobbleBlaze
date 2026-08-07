package xiaocaoawa.minecraft.mod.cobbleblaze.neoforge.content.burner;

import com.mojang.serialization.MapCodec;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlock;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import xiaocaoawa.minecraft.mod.cobbleblaze.neoforge.content.CobbleBlazeNeoForgeContent;

/** CCA liquid burner shell backed by a dedicated Pokemon-owning block entity. */
public final class PokemonLiquidBlazeBurnerBlock extends LiquidBlazeBurnerBlock {
    public static final MapCodec<PokemonLiquidBlazeBurnerBlock> CODEC =
            simpleCodec(PokemonLiquidBlazeBurnerBlock::new);

    public PokemonLiquidBlazeBurnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends PokemonLiquidBlazeBurnerBlock> codec() {
        return CODEC;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Class<LiquidBlazeBurnerBlockEntity> getBlockEntityClass() {
        return (Class) PokemonLiquidBlazeBurnerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LiquidBlazeBurnerBlockEntity> getBlockEntityType() {
        return CobbleBlazeNeoForgeContent.POKEMON_LIQUID_BLAZE_BURNER_ENTITY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PokemonLiquidBlazeBurnerBlockEntity(pos, state);
    }

    @Override
    public Item asItem() {
        return CobbleBlazeNeoForgeContent.POKEMON_LIQUID_BLAZE_BURNER_ITEM.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        CustomData data = context.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
        boolean occupied = data != null && data.contains("CobbleBlaze");
        return defaultBlockState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL,
                        occupied ? BlazeBurnerBlock.HeatLevel.SMOULDERING : BlazeBurnerBlock.HeatLevel.NONE)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
                            LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof PokemonLiquidBlazeBurnerBlockEntity burner) {
            CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data != null) {
                boolean regeneratePokemonIdentity = placer instanceof Player player && player.hasInfiniteMaterials();
                burner.restoreFromItem(data.copyTag(), regeneratePokemonIdentity);
            }
            burner.refreshState();
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        return List.of(createStack(blockEntity, builder.getLevel().registryAccess()));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level,
                                       BlockPos pos, Player player) {
        return createStack(level.getBlockEntity(pos), level.registryAccess());
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return createStack(level.getBlockEntity(pos), level.registryAccess());
    }

    private static ItemStack createStack(BlockEntity blockEntity, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(CobbleBlazeNeoForgeContent.POKEMON_LIQUID_BLAZE_BURNER_ITEM.get());
        if (blockEntity instanceof PokemonLiquidBlazeBurnerBlockEntity burner) {
            CompoundTag data = new CompoundTag();
            burner.writeItemData(data, registries);
            if (!data.isEmpty()) {
                BlockItem.setBlockEntityData(stack, burner.getType(), data);
            }
        }
        return stack;
    }
}
