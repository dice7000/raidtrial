package net.dice7000.raidtrial.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;

public class RTRegistry {
    public static final TagKey<EntityType<?>> FORGE_BOSSES = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("forge", "bosses"));

    public static void register(IEventBus modEventBus) {
        RTItems.register(modEventBus);
    }
}
