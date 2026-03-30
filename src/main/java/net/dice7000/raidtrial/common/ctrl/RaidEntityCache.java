package net.dice7000.raidtrial.common.ctrl;

import com.mojang.logging.LogUtils;
import net.dice7000.raidtrial.common.registry.RTRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static net.dice7000.raidtrial.common.ctrl.MobBattleController.WaveType.*;

public class RaidEntityCache {
    private static final Logger logger = LogUtils.getLogger();

    private static final List<EntityType<? extends Mob>> VANILLA_MONSTERS = new ArrayList<>();
    private static final List<EntityType<? extends Mob>> ALL_MOD_MONSTERS = new ArrayList<>();
    private static final List<EntityType<? extends Mob>> BOSSES = new ArrayList<>();

    public static void buildCache(ServerLevel level) {
        logger.info("start BuildCache");
        VANILLA_MONSTERS.clear();
        ALL_MOD_MONSTERS.clear();
        BOSSES.clear();
        logger.info("cleared All Mob Cache List");

        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            Entity entity = type.create(level);

            if (!(entity instanceof Mob)) {
                logger.debug("continue build Cache because type isn't mob");
                continue;
            }

            @SuppressWarnings("unchecked")
            EntityType<? extends Mob> living = (EntityType<? extends Mob>) type;

            if (living.getCategory() != MobCategory.MONSTER) {
                logger.debug("continue build Cache because MobCategory isn't monster");
                continue;
            }

            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (id == null) {
                logger.debug("continue build Cache because id is null");
                continue;
            }

            if (entity instanceof EnderDragon) continue;

            boolean isVanilla = id.getNamespace().equals("minecraft");
            boolean isBoss = type.is(RTRegistry.FORGE_BOSSES);

            if (isBoss) {
                BOSSES.add(living);
            } else if (isVanilla) {
                VANILLA_MONSTERS.add(living);
            } else {
                ALL_MOD_MONSTERS.add(living);
            }
        }

        logger.debug("vanilla monsters list: {}", VANILLA_MONSTERS);
        logger.debug("mod monsters list: {}", ALL_MOD_MONSTERS);
        logger.debug("boss monsters list: {}", BOSSES);
    }

    public static EntityType<? extends Mob> getRandomFromWaveType(MobBattleController.WaveType wave, ServerLevel level) {
        logger.debug("WaveType is {}", wave);
        List<EntityType<? extends Mob>> list;

        boolean vanilla = wave == VANILLA_MOB_ONLY || wave == VANILLA_AND_MOD_MOB;
        boolean mod = wave == VANILLA_AND_MOD_MOB || wave == MOD_MOB_ONLY || wave == MOD_AND_BOSS_MOB;
        boolean boss = wave == MOD_AND_BOSS_MOB || wave == BOSS_MOB_ONLY;

        if (vanilla && mod) {
            if (Math.random() < 0.2) list = VANILLA_MONSTERS; else list = ALL_MOD_MONSTERS;
        } else if (mod && boss) {
            if (Math.random() < 0.4) list = BOSSES; else list = ALL_MOD_MONSTERS;
        } else if (vanilla) list = VANILLA_MONSTERS;
        else if (mod) list = ALL_MOD_MONSTERS;
        else if (boss) list = BOSSES;
        else {
            logger.info("unsupported state");
            return EntityType.ZOMBIE;
        }

        if (list.isEmpty()) {
            logger.info("list is empty");
            return EntityType.ZOMBIE;
        } else if (list == VANILLA_MONSTERS) {
            logger.debug("Select from vanilla monsters");
        } else if (list == ALL_MOD_MONSTERS) {
            logger.debug("Select from mod monsters");
        } else {
            logger.debug("Select from boss monsters");
        }
        return list.get(level.random.nextInt(list.size()));
    }
}