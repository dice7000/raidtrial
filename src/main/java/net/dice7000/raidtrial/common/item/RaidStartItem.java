package net.dice7000.raidtrial.common.item;

import net.dice7000.raidtrial.common.ctrl.BattleManager;
import net.dice7000.raidtrial.common.ctrl.MobBattleController;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RaidStartItem extends Item {

    public RaidStartItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ServerLevel server = (ServerLevel) level;

            MobBattleController controller = BattleManager.get(server);
            if (!controller.isRunning()) {
                controller.start(player);
                player.displayClientMessage(Component.literal("raid started"), true);
                player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
            } else {
                player.displayClientMessage(Component.literal("already raid"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}

