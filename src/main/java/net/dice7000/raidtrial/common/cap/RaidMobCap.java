package net.dice7000.raidtrial.common.cap;

public class RaidMobCap implements IRaidMobCap{
    private boolean isRaidMob = false;

    @Override public void setIsRaidMob(boolean value) {
        this.isRaidMob = value;
    }
    @Override public boolean getIsRaidMob() {
        return isRaidMob;
    }
}
