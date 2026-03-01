package net.dice7000.raidtrial.mixin.mixin;

import net.dice7000.raidtrial.common.cap.RTCapability;
import net.dice7000.raidtrial.common.util.RTUtil;
import net.dice7000.raidtrial.mixin.method.RTMixinMethod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin implements RTMixinMethod {
    @Unique private final Mob rt$tC = (Mob) (Object) this;

    @Override public void raidtrial$setIsRaidMob(boolean value) {
        rt$tC.getCapability(RTCapability.IS_RAID_MOB).ifPresent(cap -> {
            cap.setIsRaidMob(value);
        });
    }
    //@Override public boolean raidtrial$getIsRaidMob() {return RaidTrial.isRaidMob(rt$tC);}

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickInject(CallbackInfo ci) {
        if (RTUtil.isRaidMob( rt$tC)) {
            if (rt$tC.getHealth() < rt$tC.getMaxHealth() && rt$tC.tickCount % 20 == 0) {
                rt$tC.heal(1.0F);
            }
        }
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"))
    public void RTDoHurtTargetInject(Entity pTarget, CallbackInfoReturnable<Boolean> cir) {
    }

    @Unique private LivingEntity raidtrial$anotherTarget = null;
    @Override public void raidtrial$setAnotherTarget(LivingEntity entity) {
        raidtrial$anotherTarget = entity;
    }

    @Inject(method = "getTarget", at = @At("HEAD"), cancellable = true)
    public void RTGetTargetInject(CallbackInfoReturnable<LivingEntity> cir) {
        if (RTUtil.isRaidMob(rt$tC)) {
            if (rt$tC.isAlive() && !(raidtrial$anotherTarget == null)) cir.setReturnValue(raidtrial$anotherTarget);
        }
    }
}
