package net.dice7000.raidtrial.common.ctrl;

import com.mojang.logging.LogUtils;
import net.dice7000.raidtrial.common.registry.RTRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RaidEntityCache {
    private static Logger logger = LogUtils.getLogger();

    private static final List<EntityType<? extends Mob>> VANILLA_MONSTERS = new ArrayList<>();
    private static final List<EntityType<? extends Mob>> ALL_MONSTERS = new ArrayList<>();
    private static final List<EntityType<? extends Mob>> BOSSES = new ArrayList<>();

    public static void buildCache(ServerLevel level) {
        logger.info("start BuildCache");
        VANILLA_MONSTERS.clear();
        logger.info("cleared Vanilla Monsters");
        ALL_MONSTERS.clear();
        logger.info("cleared Mod Monsters");
        BOSSES.clear();
        logger.info("cleared Bosses");

        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            //logger.info("SPAM IS HERE!!!!!!!!!!!!!");
            Entity entity = type.create(level);

            if (!(entity instanceof Mob)) {
                logger.info("continue because type isn't mob on buildCache");
                continue;
            }

            EntityType<? extends Mob> living = (EntityType<? extends Mob>) type;

            if (living.getCategory() != MobCategory.MONSTER) {
                logger.info("continue because MobCategory isn't monster on buildCache");
                continue;
            }

            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (id == null) {
                logger.info("continue because id is null on buildCache");
                continue;
            }

            boolean isVanilla = id.getNamespace().equals("minecraft");
            boolean isBoss = type.is(RTRegistry.FORGE_BOSSES) | entity instanceof Warden;

            if (isBoss) {
                BOSSES.add(living);
            } else if (isVanilla) {
                VANILLA_MONSTERS.add(living);
                ALL_MONSTERS.add(living);
            } else {
                ALL_MONSTERS.add(living);
            }
        }
    }

    public static EntityType<? extends Mob> getRandom(MobBattleController.PoolType type, ServerLevel level) {
        List<EntityType<? extends Mob>> list = null;
        switch (type) {
            case VANILLA_MONSTER -> list = VANILLA_MONSTERS;
            case ALL_MOD_MONSTER -> list = ALL_MONSTERS;
            case BOSS_ONLY -> list = BOSSES;
        }

        if (list == null) {
            logger.info("list is null on getRandom in MEC");
            System.out.println();
            return null;
        } else if (list.isEmpty()) {
            logger.info("list is empty on getRandom in MEC");
            return null;
        }
        return list.get(level.random.nextInt(list.size()));
    }
}