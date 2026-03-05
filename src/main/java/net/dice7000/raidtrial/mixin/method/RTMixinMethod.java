package net.dice7000.raidtrial.mixin.method;

import net.minecraft.world.entity.LivingEntity;

public interface RTMixinMethod {
    void raidtrial$anotherSetHealth(float amount);
    void raidtrial$setAnotherTarget(LivingEntity entity);
}
