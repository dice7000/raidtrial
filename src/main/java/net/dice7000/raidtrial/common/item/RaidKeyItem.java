package net.dice7000.raidtrial.common.item;

import net.dice7000.raidtrial.common.ctrl.BattleManager;
import net.dice7000.raidtrial.common.ctrl.MobBattleController;
import net.dice7000.raidtrial.common.registry.RTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class RaidKeyItem extends Item {
    public RaidKeyItem() {
        super(new Properties().stacksTo(1));
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel server = (ServerLevel) level;
        Player player = Objects.requireNonNull(context.getPlayer());
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(RTItems.EXAMPLE_BLOCK.get())) {
            MobBattleController controller = BattleManager.get(server);
            if (!controller.isRunning()) {
                controller.start(player, pos);
                player.displayClientMessage(Component.literal("raid started"), true);
            } else {
                player.displayClientMessage(Component.literal("already raid"), true);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}

