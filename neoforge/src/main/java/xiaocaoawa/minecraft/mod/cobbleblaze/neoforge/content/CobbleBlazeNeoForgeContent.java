package xiaocaoawa.minecraft.mod.cobbleblaze.neoforge.content;

import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.burner.PokemonBlazeBurnerBlockItem;
import xiaocaoawa.minecraft.mod.cobbleblaze.neoforge.content.burner.PokemonLiquidBlazeBurnerBlock;
import xiaocaoawa.minecraft.mod.cobbleblaze.neoforge.content.burner.PokemonLiquidBlazeBurnerBlockEntity;

/** NeoForge content that is registered only when Create Crafts & Additions is installed. */
public final class CobbleBlazeNeoForgeContent {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CobbleBlaze.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CobbleBlaze.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CobbleBlaze.MOD_ID);

    public static final DeferredBlock<PokemonLiquidBlazeBurnerBlock> POKEMON_LIQUID_BLAZE_BURNER =
            BLOCKS.register("pokemon_liquid_blaze_burner", () -> new PokemonLiquidBlazeBurnerBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.NETHER_BRICKS)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .lightLevel(LiquidBlazeBurnerBlock::getLight)));

    public static final DeferredItem<PokemonBlazeBurnerBlockItem> POKEMON_LIQUID_BLAZE_BURNER_ITEM =
            ITEMS.register("pokemon_liquid_blaze_burner", () -> new PokemonBlazeBurnerBlockItem(
                    POKEMON_LIQUID_BLAZE_BURNER.get(), new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PokemonLiquidBlazeBurnerBlockEntity>>
            POKEMON_LIQUID_BLAZE_BURNER_ENTITY = BLOCK_ENTITY_TYPES.register(
                    "pokemon_liquid_blaze_burner",
                    () -> BlockEntityType.Builder.of(
                                    PokemonLiquidBlazeBurnerBlockEntity::new,
                                    POKEMON_LIQUID_BLAZE_BURNER.get())
                            .build(null));

    private static boolean registered;

    private CobbleBlazeNeoForgeContent() {}

    public static void register(IEventBus modBus) {
        if (registered) {
            return;
        }
        registered = true;
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        modBus.addListener(CobbleBlazeNeoForgeContent::registerCapabilities);
        modBus.addListener(CobbleBlazeNeoForgeContent::addCreativeTab);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                POKEMON_LIQUID_BLAZE_BURNER_ENTITY.get(),
                (blockEntity, side) -> blockEntity.cobbleblaze$getFluidHandler());
    }

    private static void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(POKEMON_LIQUID_BLAZE_BURNER_ITEM.get());
        }
    }
}
