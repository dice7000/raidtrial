package net.dice7000.raidtrial.mixin.method;

import net.minecraft.world.entity.LivingEntity;

public interface RTMixinMethod {
    void raidtrial$setIsRaidMob(boolean value);

    void raidtrial$anotherSetHealth(float amount);

    void raidtrial$setAnotherTarget(LivingEntity entity);

    //boolean raidtrial$getIsRaidMob();
}
