package net.dice7000.raidtrial.mixin.mixin;

import com.mojang.logging.LogUtils;
import net.dice7000.raidtrial.common.util.RTUtil;
import net.dice7000.raidtrial.mixin.method.RTMixinMethod;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements RTMixinMethod {
    @Shadow public abstract float getHealth();
    @Shadow public abstract float getMaxHealth();
    @Shadow @Final private static EntityDataAccessor<Float> DATA_HEALTH_ID;

    @Shadow
    public abstract boolean isDeadOrDying();

    @Shadow
    public int deathTime;

    @Shadow
    public abstract void setHealth(float pHealth);

    @Unique private final LivingEntity rt$tC = (LivingEntity) (Object) this;
    @Unique private int raidtrial$setHealthCooldown = 20;

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    public void RTSetHealthInject(float pHealth, CallbackInfo ci) {
        if (RTUtil.isRaidMob(rt$tC)) {
            float modifiedHealth = RTUtil.adjustHealthAmount(pHealth, getMaxHealth() / 30, getMaxHealth(), getHealth());
            if (raidtrial$setHealthCooldown > 0) {
                ci.cancel();
            } else {
                rt$tC.getEntityData().set(DATA_HEALTH_ID, Mth.clamp(modifiedHealth, 0.0F, getMaxHealth()));
            }
            ci.cancel();
        }
    }
    @Override public void raidtrial$anotherSetHealth(float amount) {
        rt$tC.getEntityData().set(DATA_HEALTH_ID, amount);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void RTTickInject(CallbackInfo ci) {
        if (raidtrial$setHealthCooldown > 0) {
            raidtrial$setHealthCooldown--;
        }
    }
}
