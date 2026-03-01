package net.dice7000.raidtrial.common.event;

import net.dice7000.raidtrial.RaidTrial;
import net.dice7000.raidtrial.common.cap.RaidCapProvider;
import net.dice7000.raidtrial.common.ctrl.BattleManager;
import net.dice7000.raidtrial.common.ctrl.MobBattleController;
import net.dice7000.raidtrial.common.util.RTUtil;
import net.dice7000.raidtrial.mixin.method.RTMixinMethod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class RTEvents {
    @Mod.EventBusSubscriber(modid = RaidTrial.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class CommonForgeBus {
        @SubscribeEvent public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() == null) return;
            event.addCapability(RaidTrial.RTLocation( "is_raid_mob"), new RaidCapProvider());
        }

        @SubscribeEvent public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            MinecraftServer server = event.getServer();
            for (ServerLevel level : server.getAllLevels()) {
                MobBattleController controller = BattleManager.get(level);
                controller.tick();
            }
        }
        @SubscribeEvent public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity target = event.getEntity();
            LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
            if (target == null | attacker == null) return;
            float defAmount = event.getAmount();
            if (RTUtil.isRaidMob(target)) {
                event.setAmount(RTUtil.adjustDamageAmount(defAmount, target.getMaxHealth() / 10, target.getMaxHealth(), target.getHealth()));
            }
            if (RTUtil.isRaidMob(attacker)) {
                ((RTMixinMethod) target).raidtrial$anotherSetHealth(target.getHealth() - defAmount / 5);
                target.hurt(attacker.damageSources().genericKill(), 0);
                if (target.isDeadOrDying()) target.die(attacker.damageSources().genericKill());
            }
        }
        @SubscribeEvent public static void onLivingDrops(LivingDropsEvent event) {
            LivingEntity entity = event.getEntity();
            if (RTUtil.isRaidMob(entity)) {
                event.getDrops().clear();
                event.setCanceled(true);
            }
        }
    }
}
