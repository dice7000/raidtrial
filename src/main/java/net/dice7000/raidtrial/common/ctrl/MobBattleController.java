package net.dice7000.raidtrial.common.ctrl;

import com.mojang.logging.LogUtils;
import net.dice7000.raidtrial.RTConfig;
import net.dice7000.raidtrial.common.cap.RTCapability;
import net.dice7000.raidtrial.mixin.method.RTMixinMethod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;

public class MobBattleController {
    private static final Logger logger = LogUtils.getLogger();

    private final ServerLevel level;
    private Player player;
    private BlockPos startBlockPos;
    private Vec3 startPos;

    private int wave = 0;
    private int mobsPerWave;
    private int spawnedThisWave = 0;
    private static final int maxWave = RTConfig.configMaxWave;
    private final double fieldRadius = RTConfig.configFieldRadius;

    private final Set<UUID> activeMobs = new HashSet<>();
    private final Set<UUID> participants = new HashSet<>();
    private int spawnCooldown = 0;

    private enum State { IDLE, RUNNING, FINISHED }
    private State state = State.IDLE;

    public MobBattleController(ServerLevel level) {
        this.level = level;
    }

    public void start(Player player,BlockPos pos) {
        this.player = player;
        this.startBlockPos = pos;
        this.startPos = pos.getCenter();
        List<ServerPlayer> players = getPlayersInRange(level);
        for (ServerPlayer serverPlayer : players) {
            participants.add(player.getUUID());
            serverPlayer.sendSystemMessage(Component.literal("raid started"));
        }
        this.wave = 1;
        this.mobsPerWave = RTConfig.configMobsPerWave + 2;
        this.spawnedThisWave = 0;
        this.activeMobs.clear();
        level.playSound(null, startBlockPos, SoundEvents.END_PORTAL_SPAWN, SoundSource.NEUTRAL, 1.0F, 1.0F);
        this.state = State.RUNNING;
        logger.info("maxWave: {}, mobsPerWave: {}", maxWave, mobsPerWave);
    }

    private List<ServerPlayer> getPlayersInRange(ServerLevel level) {
        AABB box = new AABB(
                startBlockPos.getX() - fieldRadius, startBlockPos.getY() - 10, startBlockPos.getZ() - fieldRadius,
                startBlockPos.getX() + fieldRadius, startBlockPos.getY() + 10, startBlockPos.getZ() + fieldRadius
        );
        return level.getEntitiesOfClass(ServerPlayer.class, box);
    }

    public void retire() {
        this.player = null;
        this.startBlockPos = null;
        this.startPos = null;
        this.wave = 0;
        this.mobsPerWave = RTConfig.configMobsPerWave;
        this.spawnedThisWave = 0;
        for (UUID uuid : this.activeMobs) {
            Entity entity = level.getEntity(uuid);
            if (!(entity == null)) {
                entity.discard();
            }
        }
        this.activeMobs.clear();
        for (UUID uuid : participants) {
            ServerPlayer player = (ServerPlayer) level.getPlayerByUUID(uuid);
            if (player != null) {
                player.sendSystemMessage(Component.literal("raid failed"));
            }
        }
        participants.clear();
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

        checkPlayerOutOfRange(level);
        if (participants.isEmpty() | areAllParticipantsDead(level)) {
            retire();
            return;
        }

        if (activeMobs.isEmpty() && spawnFinishedForWave()) {
            spawnedThisWave = 0;
            player.sendSystemMessage(Component.literal("wave end"));
            nextWave();
            return;
        }

        if (!spawnFinishedForWave()) {
            spawnMobsGradually();
        }

        showArea(level, BlockPos.containing(startPos), fieldRadius);
    }

    private void onFinished() {
        player = null;
        wave = 0;
    }

    private void showArea(ServerLevel level, BlockPos center, double radius) {
        int points = (int) (radius * 8);
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

    private boolean areAllParticipantsDead(ServerLevel level) {
        for (UUID uuid : participants) {
            ServerPlayer player = (ServerPlayer) level.getPlayerByUUID(uuid);
            if (player != null && !player.isDeadOrDying()) {
                return false;
            }
        }
        return true;
    }

    private void checkPlayerOutOfRange(ServerLevel level) {
        Iterator<UUID> it = participants.iterator();

        while (it.hasNext()) {
            UUID uuid = it.next();
            ServerPlayer player = (ServerPlayer) level.getPlayerByUUID(uuid);

            if (player == null) {
                it.remove();
                continue;
            }

            if (isOutsideRange(player)) {
                it.remove();
                player.sendSystemMessage(Component.literal("you were retired because you left the battle area"));
            }
        }
    }
    private boolean isOutsideRange(ServerPlayer player) {
        double dx = player.getX() - (startBlockPos.getX() + 0.5);
        double dz = player.getZ() - (startBlockPos.getZ() + 0.5);
        double distSq = dx * dx + dz * dz;
        double battleFieldRadius = fieldRadius + Math.max(fieldRadius, 10);
        return distSq > battleFieldRadius * battleFieldRadius;
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
        EntityType<? extends Mob> type = getRandomFromWaveInt(wave, level);
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


    public static EntityType<? extends Mob> getRandomFromWaveInt(int wave, ServerLevel level) {
        return RaidEntityCache.getRandomFromWaveType(WaveType.calculateWaveType(wave), level);
    }

    public enum WaveType {
        VANILLA_MOB_ONLY, VANILLA_AND_MOD_MOB, MOD_MOB_ONLY, MOD_AND_BOSS_MOB, BOSS_MOB_ONLY;

        public static WaveType calculateWaveType(int wave) {
            float progress = (float) wave / maxWave;
            logger.debug("wave: {}, maxWave: {}, progress: {}", wave, maxWave, progress);
                 if (progress <= 0.2) return VANILLA_MOB_ONLY;
            else if (progress <= 0.4) return VANILLA_AND_MOD_MOB;
            else if (progress <= 0.6) return MOD_MOB_ONLY;
            else if (progress <= 0.8) return MOD_AND_BOSS_MOB;
            else                      return BOSS_MOB_ONLY;
        }
    }
}

