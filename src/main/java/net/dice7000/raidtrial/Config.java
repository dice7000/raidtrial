package net.dice7000.raidtrial;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = RaidTrial.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue MAX_WAVE =
            BUILDER.comment("Raid's max wave. Default: 10")
                    .defineInRange("MaxWave", 10, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_PER_WAVE =
            BUILDER.comment("Number of mobs per wave1. Default: 12")
                    .defineInRange("MobsPerWave", 12, 1, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int maxWave;
    public static int mobsPerWave;

    @SubscribeEvent static void onLoad(final ModConfigEvent event) {
        maxWave = MAX_WAVE.get();
        mobsPerWave = MOB_PER_WAVE.get();
    }
}
