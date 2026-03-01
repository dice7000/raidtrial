package net.dice7000.raidtrial.common.cap;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class RTCapability {
    public static Capability<IRaidMobCap> IS_RAID_MOB = CapabilityManager.get(new CapabilityToken<>(){});
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IRaidMobCap.class);
    }
}
