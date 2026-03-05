package net.dice7000.raidtrial.common.item;

import net.dice7000.raidtrial.common.cap.RTCapability;
import net.dice7000.raidtrial.mixin.method.RTMixinMethod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TestItem {
    public static class HurtItem extends Item {
        public HurtItem() {
            super(new Properties());
        }
        @Override public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
            if (pAttacker.level().isClientSide) return false;
            pTarget.hurt(pAttacker.damageSources().genericKill(), 2000000000);
            return super.hurtEnemy(pStack, pTarget, pAttacker);
        }
    }
    public static class SetHealthItem extends Item {
        public SetHealthItem() {
            super(new Properties());
        }
        @Override public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
            if (pAttacker.level().isClientSide) return false;
            pTarget.setHealth(0);
            return super.hurtEnemy(pStack, pTarget, pAttacker);
        }
    }
    public static class AnotherSetHealthItem extends Item {
        public AnotherSetHealthItem() {
            super(new Properties());
        }
        @Override public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
            if (pAttacker.level().isClientSide) return false;
            ((RTMixinMethod) pTarget).raidtrial$anotherSetHealth(0);
            return super.hurtEnemy(pStack, pTarget, pAttacker);
        }
    }
    public static class RaidMobSummonerItem extends Item {
        public RaidMobSummonerItem() {
            super(new Properties());
        }
        @Override public InteractionResult useOn(UseOnContext pContext) {
            BlockPos pos = pContext.getClickedPos();
            Level level = pContext.getLevel();
            LivingEntity entity = new Husk(EntityType.HUSK, level);
            entity.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
            entity.getCapability(RTCapability.IS_RAID_MOB).ifPresent((cap) -> {cap.setIsRaidMob(true);});
            level.addFreshEntity(entity);
            return super.useOn(pContext);
        }
    }
}
