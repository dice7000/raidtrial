package net.dice7000.raidtrial.common.registry;

import net.dice7000.raidtrial.RaidTrial;
import net.dice7000.raidtrial.common.item.RaidRetireItem;
import net.dice7000.raidtrial.common.item.RaidStartItem;
import net.dice7000.raidtrial.common.item.TestItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RTItems {
    public static final DeferredRegister<Block> BLOCKS;
    public static final DeferredRegister<Item> ITEMS;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS;
    static {
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RaidTrial.MOD_ID);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RaidTrial.MOD_ID);
        CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RaidTrial.MOD_ID);
    }

    public static final RegistryObject<Block> EXAMPLE_BLOCK;
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM;
    public static final RegistryObject<Item> RAID_START_ITEM;
    public static final RegistryObject<Item> RAID_RETIRE_ITEM;
    public static final RegistryObject<Item> HURT_ITEM;
    public static final RegistryObject<Item> SETHEALTH_ITEM;
    public static final RegistryObject<Item> ANOTHER_SETHEALTH_ITEM;
    public static final RegistryObject<Item> RAIDMOB_SUMMONER_ITEM;
    static {
        EXAMPLE_BLOCK = BLOCKS.register("example_block",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
        EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
                () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));
        RAID_START_ITEM = ITEMS.register("raid_start_item",
                RaidStartItem::new);
        RAID_RETIRE_ITEM = ITEMS.register("raid_retire_item",
                RaidRetireItem::new);

        HURT_ITEM = ITEMS.register("hurt_item", TestItem.HurtItem::new);
        SETHEALTH_ITEM = ITEMS.register("sethealth_item", TestItem.SetHealthItem::new);
        ANOTHER_SETHEALTH_ITEM = ITEMS.register("another_sethealth_item", TestItem.AnotherSetHealthItem::new);
        RAIDMOB_SUMMONER_ITEM = ITEMS.register("raidmob_summoner_item", TestItem.RaidMobSummonerItem::new);
    }

    public static final RegistryObject<CreativeModeTab> RT_TAB = CREATIVE_MODE_TABS.register("raid_trial_tab", () ->
            CreativeModeTab.builder()
            .title(Component.literal("Raid Trial"))
            .icon(() -> RAID_START_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_BLOCK_ITEM.get());
                output.accept(RAID_START_ITEM.get());
                output.accept(RAID_RETIRE_ITEM.get());
                output.accept(HURT_ITEM.get());
                output.accept(SETHEALTH_ITEM.get());
                output.accept(ANOTHER_SETHEALTH_ITEM.get());
                output.accept(RAIDMOB_SUMMONER_ITEM.get());
            }).build());

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
