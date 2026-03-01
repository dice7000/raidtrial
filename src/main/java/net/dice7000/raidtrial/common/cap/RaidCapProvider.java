package net.dice7000.raidtrial.common.cap;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class RaidCapProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final RaidMobCap backend = new RaidMobCap();
    private final LazyOptional<IRaidMobCap> optional = LazyOptional.of(() -> backend);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == RTCapability.IS_RAID_MOB ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Summoned", backend.getIsRaidMob());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.setIsRaidMob(nbt.getBoolean("Summoned"));
    }
}
