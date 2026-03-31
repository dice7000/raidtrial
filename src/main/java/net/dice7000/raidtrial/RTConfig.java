package net.dice7000.raidtrial;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = RaidTrial.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RTConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue MAX_WAVE =
            BUILDER.comment("Raid's max wave. Default: 10")
                    .defineInRange("MaxWave", 10, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_PER_WAVE =
            BUILDER.comment("Number of mobs per wave1. Default: 12")
                    .defineInRange("MobsPerWave", 10, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue FIELD_RADIUS =
            BUILDER.comment("Raid field radius. Default: 30")
                    .defineInRange("FieldRadius", 50, 10, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int configMaxWave = 10;
    public static int configMobsPerWave = 10;
    public static int configFieldRadius = 50;

    @SubscribeEvent static void onLoad(final ModConfigEvent event) {
        configMaxWave = MAX_WAVE.get();
        configMobsPerWave = MOB_PER_WAVE.get();
        configFieldRadius = FIELD_RADIUS.get();
    }
}
