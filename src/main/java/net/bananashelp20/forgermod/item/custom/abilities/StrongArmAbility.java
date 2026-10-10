package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;

public final class StrongArmAbility {
    private StrongArmAbility() {}
    public static float chance(int rank) { return switch(Math.clamp(rank,1,4)) { case 1->.3F; case 2->.55F; case 3->.8F; default->1; }; }
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        var target=WeaponAbilityTargets.aimedEnemy(player,player.entityInteractionRange()); if(target==null) return false;
        var armor=new ArrayList<EquipmentSlot>();
        for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET,EquipmentSlot.BODY})
            if(target.getItemBySlot(slot).getItem() instanceof ArmorItem) armor.add(slot);
        if(armor.isEmpty()) return false;
        if(player.getRandom().nextFloat()<chance(Augmentations.level(stack,RecommendedAbilities.STRONG_ARM))) {
            var slot=armor.get(player.getRandom().nextInt(armor.size()));
            var piece=target.getItemBySlot(slot); var item=piece.getItem();
            target.setItemSlot(slot,piece.getCount()>1?piece.copyWithCount(piece.getCount()-1):ItemStack.EMPTY);
            target.onEquippedItemBroken(item,slot);
        }
        return true;
    }
}
