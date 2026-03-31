package net.dice7000.raidtrial.common.ctrl;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class BattleManager {
    private static final Map<ResourceKey<Level>, MobBattleController> CONTROLLERS = new HashMap<>();
    public static MobBattleController get(ServerLevel level) {
        return CONTROLLERS.computeIfAbsent(level.dimension(), dim -> new MobBattleController(level));
    }
}
