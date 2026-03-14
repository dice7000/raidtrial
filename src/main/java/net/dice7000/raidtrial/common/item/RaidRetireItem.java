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

public class RaidRetireItem extends Item {
    public RaidRetireItem() {
        super(new Properties());
    }
    @Override public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            ServerLevel server = (ServerLevel) pLevel;
            MobBattleController controller = BattleManager.get(server);
            if (controller.isRunning()) {
                controller.retire();
                pPlayer.displayClientMessage(Component.literal("raid stopped"), true);
            } else {
                pPlayer.displayClientMessage(Component.literal("raid isn't started"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(pPlayer.getItemInHand(pUsedHand), pLevel.isClientSide);
    }
}
