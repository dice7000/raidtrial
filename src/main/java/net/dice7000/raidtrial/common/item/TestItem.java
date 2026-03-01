package net.dice7000.raidtrial.common.item;

import net.dice7000.raidtrial.mixin.method.RTMixinMethod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
}
