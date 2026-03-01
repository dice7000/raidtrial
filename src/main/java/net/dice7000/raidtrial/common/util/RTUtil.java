package net.dice7000.raidtrial.common.util;

import net.dice7000.raidtrial.common.cap.IRaidMobCap;
import net.dice7000.raidtrial.common.cap.RTCapability;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class RTUtil {
    public static boolean isRaidMob(LivingEntity mob) {
        return mob.getCapability(RTCapability.IS_RAID_MOB).map(IRaidMobCap::getIsRaidMob).orElse(false);
    }

    public static float adjustHealthAmount(float amount, float damCap, float maxHealth, float currentHealth) {
        if (Float.isNaN(amount) || Float.isNaN(currentHealth) || Float.isNaN(maxHealth)) amount = currentHealth;

        float adjustedDamage = Mth.clamp(currentHealth, 0.0F, maxHealth) - Mth.clamp(amount, 0.0F, maxHealth);
        if (adjustedDamage > damCap) adjustedDamage = damCap;
        return currentHealth - adjustedDamage;
    }
    public static float adjustDamageAmount(float amount, float damCap, float maxHealth, float currentHealth) {
        if (Float.isNaN(amount) || Float.isNaN(currentHealth) || Float.isNaN(maxHealth)) amount = 0;

        return Mth.clamp(amount, 0.0F, damCap);
    }
}
