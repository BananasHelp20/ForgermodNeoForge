package net.bananashelp20.forgermod.item.custom.abilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
public final class GreenFeastAbility {
    private GreenFeastAbility() {}
    public static boolean activate(ServerPlayer player) {
        if(player.getHealth()>=player.getMaxHealth()) return false;
        for(int slot=0;slot<player.getInventory().getContainerSize();slot++) {
            var wheat=player.getInventory().getItem(slot);
            if(!wheat.is(Items.WHEAT) || wheat.isEmpty()) continue;
            wheat.shrink(1);player.getInventory().setChanged();player.heal(2);return true;
        }
        return false;
    }
}
