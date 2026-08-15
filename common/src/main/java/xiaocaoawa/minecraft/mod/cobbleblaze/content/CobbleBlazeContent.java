package xiaocaoawa.minecraft.mod.cobbleblaze.content;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import xiaocaoawa.minecraft.mod.cobbleblaze.CobbleBlaze;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.burner.PokemonBlazeBurnerArmInteraction;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.burner.PokemonBlazeBurnerBlock;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.burner.PokemonBlazeBurnerBlockEntity;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.burner.PokemonBlazeBurnerBlockItem;
import xiaocaoawa.minecraft.mod.cobbleblaze.content.burner.PokemonBlazeBurnerMovementBehaviour;
import xiaocaoawa.minecraft.mod.cobbleblaze.burner.PokemonBoilerHeater;

public final class CobbleBlazeContent {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(CobbleBlaze.MOD_ID, Registries.BLOCK);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(CobbleBlaze.MOD_ID, Registries.ITEM);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(CobbleBlaze.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    private static final DeferredRegister<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPES =
            DeferredRegister.create(CobbleBlaze.MOD_ID, CreateRegistries.ARM_INTERACTION_POINT_TYPE);

    public static final RegistrySupplier<PokemonBlazeBurnerBlock> POKEMON_BLAZE_BURNER =
            BLOCKS.register("pokemon_blaze_burner", () -> new PokemonBlazeBurnerBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.NETHER_BRICKS)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .lightLevel(BlazeBurnerBlock::getLight)));

    public static final RegistrySupplier<PokemonBlazeBurnerBlockItem> POKEMON_BLAZE_BURNER_ITEM =
            ITEMS.register("pokemon_blaze_burner", () -> new PokemonBlazeBurnerBlockItem(
                    POKEMON_BLAZE_BURNER.get(), new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<BlockEntityType<PokemonBlazeBurnerBlockEntity>> POKEMON_BLAZE_BURNER_ENTITY =
            BLOCK_ENTITY_TYPES.register("pokemon_blaze_burner", () -> BlockEntityType.Builder
                    .of(PokemonBlazeBurnerBlockEntity::new, POKEMON_BLAZE_BURNER.get())
                    .build(null));

    /** Lets Create's Mechanical Arm target the Pokemon burner to feed it fuel (blaze cakes etc.). */
    public static final RegistrySupplier<ArmInteractionPointType> POKEMON_BLAZE_BURNER_ARM_POINT =
            ARM_INTERACTION_POINT_TYPES.register("pokemon_blaze_burner",
                    PokemonBlazeBurnerArmInteraction::forBlazeBurner);

    private static boolean registered;

    private CobbleBlazeContent() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        POKEMON_BLAZE_BURNER.listen(block -> {
            MovementBehaviour.REGISTRY.register(block, new PokemonBlazeBurnerMovementBehaviour());
            BoilerHeater.REGISTRY.register(block, PokemonBoilerHeater.INSTANCE);
        });
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITY_TYPES.register();
        // Create snapshots the sorted type list when its registry freezes; on platforms where our
        // registration lands after that (Fabric registers eagerly), re-run the snapshot ourselves.
        POKEMON_BLAZE_BURNER_ARM_POINT.listen(type -> ArmInteractionPointType.init());
        ARM_INTERACTION_POINT_TYPES.register();
        CreativeTabRegistry.append(CreativeModeTabs.FUNCTIONAL_BLOCKS, POKEMON_BLAZE_BURNER_ITEM);
    }
}
