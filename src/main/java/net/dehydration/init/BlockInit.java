package net.dehydration.init;

import net.dehydration.block.BambooPumpBlock;
import net.dehydration.block.CampfireCauldronBlock;
import net.dehydration.block.CopperCauldronBlock;
import net.dehydration.block.CopperLeveledCauldronBlock;
import net.dehydration.block.entity.BambooPumpEntity;
import net.dehydration.block.entity.CampfireCauldronEntity;
import net.dehydration.block.entity.CopperCauldronBehavior;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class BlockInit {
    // Block
    public static final Block CAMPFIRE_CAULDRON_BLOCK = register("campfire_cauldron", true,
            new CampfireCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON).pistonBehavior(PistonBehavior.DESTROY)));
    public static final Block COPPER_CAULDRON_BLOCK = register("copper_cauldron", true, new CopperCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON)));
    public static final Block COPPER_WATER_CAULDRON_BLOCK = register("water_copper_cauldron", false,
            new CopperLeveledCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON), CopperLeveledCauldronBlock.RAIN_PREDICATE, CopperCauldronBehavior.WATER_COPPER_CAULDRON_BEHAVIOR));
    public static final Block COPPER_POWDERED_CAULDRON_BLOCK = register("powder_snow_copper_cauldron", false,
            new CopperLeveledCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON), CopperLeveledCauldronBlock.SNOW_PREDICATE, CopperCauldronBehavior.POWDER_SNOW_COPPER_CAULDRON_BEHAVIOR));
    public static final Block COPPER_PURIFIED_WATER_CAULDRON_BLOCK = register("purified_water_copper_cauldron", false,
            new CopperLeveledCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON), CopperLeveledCauldronBlock.RAIN_PREDICATE, CopperCauldronBehavior.PURIFIED_WATER_COPPER_CAULDRON_BEHAVIOR));
    public static final Block BAMBOO_PUMP_BLOCK = register("bamboo_pump", true,
            new BambooPumpBlock(AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).pistonBehavior(PistonBehavior.DESTROY).strength(1.2f, 4.0f).sounds(BlockSoundGroup.BAMBOO)));
    public static final Block PURIFIED_WATER = register("purified_water", false, new FluidBlock(FluidInit.PURIFIED_WATER, AbstractBlock.Settings.create().mapColor(MapColor.WATER_BLUE).replaceable()
            .noCollision().strength(100.0f).pistonBehavior(PistonBehavior.DESTROY).dropsNothing().liquid().sounds(BlockSoundGroup.INTENTIONALLY_EMPTY)));

    // Entity
    public static BlockEntityType<CampfireCauldronEntity> CAMPFIRE_CAULDRON_ENTITY = BlockEntityType.Builder.create(CampfireCauldronEntity::new, CAMPFIRE_CAULDRON_BLOCK).build(null);
    public static final BlockEntityType<BambooPumpEntity> BAMBOO_PUMP_ENTITY = BlockEntityType.Builder.create(BambooPumpEntity::new, BAMBOO_PUMP_BLOCK).build(null);

    private static Block register(String id, boolean addItemGroup, Block block) {
        return register(Identifier.of("dehydration", id), addItemGroup, block);
    }

    private static Block register(Identifier id, boolean addItemGroup, Block block) {
        if (addItemGroup) {
            Item item = Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
            ItemGroupEvents.modifyEntriesEvent(ItemInit.DEHYDRATION_ITEM_GROUP).register(entries -> entries.add(item));
        }
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void init() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, "dehydration:campfire_cauldron_entity", CAMPFIRE_CAULDRON_ENTITY);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, "dehydration:bamboo_pump_entity", BAMBOO_PUMP_ENTITY);
    }

}
