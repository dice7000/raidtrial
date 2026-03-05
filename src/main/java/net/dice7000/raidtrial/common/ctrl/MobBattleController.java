package net.dice7000.raidtrial.common.ctrl;

import com.mojang.logging.LogUtils;
import net.dice7000.raidtrial.common.cap.RTCapability;
import net.dice7000.raidtrial.mixin.method.RTMixinMethod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MobBattleController {
    private static Logger logger = LogUtils.getLogger();

    private final ServerLevel level;
    private Player player;
    private Vec3 startPos;

    private int wave = 0;
    private int mobsPerWave;
    private int spawnedThisWave = 0;
    private final int maxWave = 5;
    private final double fieldRadius = 30;

    private final Set<UUID> activeMobs = new HashSet<>();
    private int spawnCooldown = 0;

    private enum State { IDLE, RUNNING, FINISHED }
    private State state = State.IDLE;

    public MobBattleController(ServerLevel level) {
        this.level = level;
        this.state = State.IDLE;
    }

    public void start(Player player) {
        this.player = player;
        this.startPos = new Vec3(player.getX(), player.getY(),player.getZ());
        this.wave = 1;
        this.mobsPerWave = 12;
        this.spawnedThisWave = 0;
        this.activeMobs.clear();
        this.state = State.RUNNING;
    }

    public void retire() {
        this.player = null;
        this.startPos = Vec3.ZERO;
        this.wave = 0;
        this.mobsPerWave = 12;
        this.spawnedThisWave = 0;
        for (UUID uuid : this.activeMobs) {
            Entity entity = level.getEntity(uuid);
            if (!(entity == null)) {
                entity.discard();
            }
        }
        this.activeMobs.clear();
        this.state = State.IDLE;
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public void tick() {
        if (state == State.IDLE) return;
        if (state == State.FINISHED) {
            onFinished();
            state = State.IDLE;
            return;
        }

        cleanupDead();

        if (activeMobs.isEmpty() && spawnFinishedForWave()) {
            spawnedThisWave = 0;
            player.sendSystemMessage(Component.literal("wave end"));
            nextWave();
            return;
        }

        if (!spawnFinishedForWave()) {
            spawnMobsGradually();
        }

        showSpawnArea(level, BlockPos.containing(startPos), fieldRadius);
    }

    private void onFinished() {
        player = null;
        wave = 0;
    }

    private void showSpawnArea(ServerLevel level, BlockPos center, double radius) {
        int points = 240;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + 0.5 + Math.cos(angle) * radius;
            double z = center.getZ() + 0.5 + Math.sin(angle) * radius;
            double y = center.getY() + 0.1;
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z,
                    1, 0, 0, 0, 0);
        }
    }

    private void cleanupDead() {
        activeMobs.removeIf(uuid -> {
            Entity e = level.getEntity(uuid);
            return e == null || !e.isAlive();
        });
    }

    private boolean spawnFinishedForWave() {
        return spawnedThisWave >= mobsPerWave;
    }


    private void nextWave() {
        if (wave >= maxWave) {
            state = State.FINISHED;
            return;
        }
        wave++;
        mobsPerWave = 10 + (wave * 2);
        activeMobs.clear();
    }

    private void spawnMobsGradually() {
        if (spawnCooldown > 0) {
            spawnCooldown--;
            return;
        }

        Mob mob = createMob();
        if (mob != null) {
            level.addFreshEntity(mob);
            mob.getCapability(RTCapability.IS_RAID_MOB).ifPresent(cap -> cap.setIsRaidMob(true));
            ((RTMixinMethod) mob).raidtrial$setAnotherTarget(player);
            activeMobs.add(mob.getUUID());
            spawnedThisWave++;
        } else {
            logger.info("mob is null on add Level");
        }

        spawnCooldown = 10;
    }
    private Mob createMob() {
        WaveMobPool pool = getPoolForWave(wave);
        EntityType<? extends Mob> type = pool.getRandom(level);
        if (type == null) {
            logger.info("type is null on createMob");
            return null;
        }
        Mob mob = type.create(level);
        if (mob == null) {
            logger.info("mob is null on createMob");
            return null;
        }
        Vec3 pos = randomAroundPlayer(8, fieldRadius);
        mob.moveTo(pos.x, pos.y, pos.z, level.random.nextFloat() * 360F, 0);
        applyWaveBuffs(mob);
        return mob;
    }
    private WaveMobPool getPoolForWave(int wave) {
        return switch (wave) {
            case 1 -> WaveMobPool.WAVE1;
            case 2 -> WaveMobPool.WAVE2;
            case 3 -> WaveMobPool.WAVE3;
            case 4 -> WaveMobPool.WAVE4;
            case 5 -> WaveMobPool.WAVE5;
            default -> WaveMobPool.WAVE1;
        };
    }
    private Vec3 randomAroundPlayer(double min, double max) {
        double dist = min + level.random.nextDouble() * (max - min);
        double angle = level.random.nextDouble() * Math.PI * 2;

        double x = startPos.x + Math.cos(angle) * dist;
        double z = startPos.z + Math.sin(angle) * dist;
        double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)x, (int)z);

        return new Vec3(x, y, z);
    }
    private void applyWaveBuffs(Mob mob) {
        double hpMul = 2.0;
        double dmgMul = 2.0;
        double spdMul = 2.0;

        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            double newHp = hp.getBaseValue() * hpMul;
            hp.setBaseValue(newHp);
            mob.setHealth((float)newHp);
        }

        var dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) {
            dmg.setBaseValue(dmg.getBaseValue() * dmgMul);
        }

        var spd = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            spd.setBaseValue(spd.getBaseValue() * spdMul);
        }
    }


    public enum WaveMobPool {
        WAVE1(PoolType.VANILLA_MONSTER),
        WAVE2(PoolType.VANILLA_MONSTER),
        WAVE3(PoolType.ALL_MOD_MONSTER),
        WAVE4(PoolType.ALL_MOD_MONSTER),
        WAVE5(PoolType.BOSS_ONLY);
        public final PoolType type;

        WaveMobPool(PoolType type) {
            this.type = type;
        }

        public EntityType<? extends Mob> getRandom(ServerLevel level) {
            return RaidEntityCache.getRandom(type, level);
        }
    }

    public enum PoolType { VANILLA_MONSTER, ALL_MOD_MONSTER, BOSS_ONLY }
}

