package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import java.util.*;

/** Summon slots point at the actual bank-owned items, not copies. Only real supplied equipment wears. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class NecromancerEquipment {
    public static final EquipmentSlot[] SLOTS={EquipmentSlot.MAINHAND,EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET,EquipmentSlot.OFFHAND};
    private record Binding(NecromancerLoadouts.Loadout loadout,int row,Mob mob){}
    private static final Map<UUID,Binding> BINDINGS=new HashMap<>();
    private NecromancerEquipment(){}
    public static boolean validHands(ItemStack main,ItemStack off){
        return off.isEmpty() || !net.bananashelp20.forgermod.item.custom.attacks.axe.AxeItems.isAxe(main);
    }
    private static void refund(net.minecraft.server.level.ServerPlayer owner,ItemStack item){
        owner.getInventory().add(item);if(!item.isEmpty()){var drop=new net.minecraft.world.entity.item.ItemEntity(owner.level(),owner.getX(),owner.getY(),owner.getZ(),item);drop.setDefaultPickUpDelay();owner.level().addFreshEntity(drop);}
    }
    public static void normalizeHands(NecromancerLoadouts.Loadout loadout,int row,net.minecraft.server.level.ServerPlayer owner){
        int first=row*NecromancerCatalog.EQUIPMENT_SLOTS;
        if(!BoneGuardEvents.busy(loadout.id) && !validHands(loadout.gear.getItem(first),loadout.gear.getItem(first+5))){refund(owner,loadout.gear.removeItemNoUpdate(first+5));loadout.gear.setChanged();}
    }
    @SubscribeEvent public static void hands(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event){
        if(event.getEntity() instanceof Mob mob && mob instanceof BoneGuardEvents.FriendlyGuard guard && !mob.level().isClientSide && !validHands(mob.getMainHandItem(),mob.getOffhandItem())){
            var off=mob.getOffhandItem();var binding=BINDINGS.get(mob.getUUID());
            if(binding!=null && binding.loadout.gear.getItem(binding.row*NecromancerCatalog.EQUIPMENT_SLOTS+5)==off)binding.loadout.gear.removeItemNoUpdate(binding.row*NecromancerCatalog.EQUIPMENT_SLOTS+5);
            mob.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);refund(guard.owner(),off);changed(mob);
        }
    }
    public static void bind(Mob mob,NecromancerLoadouts.Loadout loadout,int row){
        if(mob instanceof BoneGuardEvents.FriendlyGuard guard)normalizeHands(loadout,row,guard.owner());
        BINDINGS.put(mob.getUUID(),new Binding(loadout,row,mob));
        for(int i=0;i<SLOTS.length;i++){var stack=loadout.gear.getItem(row*NecromancerCatalog.EQUIPMENT_SLOTS+i);if(!stack.isEmpty())mob.setItemSlot(SLOTS[i],stack);}
    }
    public static boolean supplied(Mob mob,EquipmentSlot slot){
        var binding=BINDINGS.get(mob.getUUID());if(binding==null)return false;
        for(int i=0;i<SLOTS.length;i++)if(SLOTS[i]==slot)return !binding.loadout.gear.getItem(binding.row*NecromancerCatalog.EQUIPMENT_SLOTS+i).isEmpty() && mob.getItemBySlot(slot)==binding.loadout.gear.getItem(binding.row*NecromancerCatalog.EQUIPMENT_SLOTS+i);return false;
    }
    public static void changed(Mob mob){var binding=BINDINGS.get(mob.getUUID());if(binding!=null)binding.loadout.gear.setChanged();}
    public static void wearWeapon(Mob mob){
        if(!supplied(mob,EquipmentSlot.MAINHAND))return;var stack=mob.getMainHandItem();
        int wear=stack.getItem() instanceof AxeItem?2:stack.getItem() instanceof SwordItem || stack.getItem() instanceof TridentItem?1:0;
        if(wear>0){stack.hurtAndBreak(wear,mob,EquipmentSlot.MAINHAND);changed(mob);}
    }
    public static void wearShield(Mob mob,float damage){
        if(damage<3 || !mob.isUsingItem() || !mob.getUseItem().canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK))return;
        var shield=mob.getUseItem();var slot=mob.getUsedItemHand()==net.minecraft.world.InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND;
        shield.hurtAndBreak(1+(int)Math.floor(damage),mob,slot);changed(mob);if(shield.isEmpty())mob.stopUsingItem();
    }
    public static void wearBow(Mob mob){if(supplied(mob,EquipmentSlot.MAINHAND) && mob.getMainHandItem().getItem() instanceof BowItem){mob.getMainHandItem().hurtAndBreak(1,mob,EquipmentSlot.MAINHAND);changed(mob);}}
    public static void release(Mob mob){
        var binding=BINDINGS.remove(mob.getUUID());if(binding==null)return;
        for(int i=0;i<SLOTS.length;i++)if(mob.getItemBySlot(SLOTS[i])==binding.loadout.gear.getItem(binding.row*NecromancerCatalog.EQUIPMENT_SLOTS+i))mob.setItemSlot(SLOTS[i],ItemStack.EMPTY);
        binding.loadout.gear.setChanged();
    }
    @SubscribeEvent public static void totem(net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent event){
        if(event.getEntity() instanceof Mob mob)changed(mob);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void death(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event){if(event.getEntity() instanceof Mob mob && !mob.isAlive())release(mob);}
    @SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event){if(!event.getLevel().isClientSide && event.getEntity() instanceof Mob mob)release(mob);}
    @SubscribeEvent public static void convert(net.neoforged.neoforge.event.entity.living.LivingConversionEvent.Pre event){if(event.getEntity() instanceof BoneGuardEvents.FriendlyGuard)event.setCanceled(true);}
    @SubscribeEvent public static void interact(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event){if(event.getTarget() instanceof BoneGuardEvents.FriendlyGuard)event.setCanceled(true);}
    @SubscribeEvent public static void interactSpecific(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific event){if(event.getTarget() instanceof BoneGuardEvents.FriendlyGuard)event.setCanceled(true);}
    @SubscribeEvent public static void weaponDestroyed(net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent event){
        if(!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player))return;
        var weapon=event.getOriginal();var id=NecromancerLoadouts.identity(weapon);if(id==null)return;
        BoneGuardEvents.cancelWeapon(id);var loadout=NecromancerLoadouts.get(player,weapon);
        for(int row=0;row<loadout.types.size();row++)loadout.returnGear(row,player);
    }
    /** Contact with XP repairs real supplied Mending gear; conserve every unused XP point, including merged orb counts. */
    @SubscribeEvent public static void mend(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof net.minecraft.server.level.ServerLevel level) || !mob.isAlive() || !BINDINGS.containsKey(mob.getUUID()))return;
        var chosen=net.minecraft.world.item.enchantment.EnchantmentHelper.getRandomItemWith(net.minecraft.world.item.enchantment.EnchantmentEffectComponents.REPAIR_WITH_XP,mob,stack->stack.isDamaged() && Arrays.stream(SLOTS).anyMatch(slot->supplied(mob,slot) && mob.getItemBySlot(slot)==stack));
        if(chosen.isEmpty())return;var stack=chosen.get().itemStack();
        var orbs=level.getEntitiesOfClass(ExperienceOrb.class,mob.getBoundingBox().inflate(.25),orb->!orb.isRemoved());if(orbs.isEmpty())return;
        var orb=orbs.getFirst();int value=orb.getValue();
        int repair=net.minecraft.world.item.enchantment.EnchantmentHelper.modifyDurabilityToRepairFromXp(level,stack,(int)(value*stack.getXpRepairRatio()));if(repair<=0)return;
        int restored=Math.min(repair,stack.getDamageValue());if(restored<=0)return;
        var saved=orb.saveWithoutId(new net.minecraft.nbt.CompoundTag());int count=Math.max(1,saved.getInt("Count"));int remaining=count*value-restored*value/repair;var point=orb.position();
        stack.setDamageValue(stack.getDamageValue()-restored);changed(mob);orb.discard();if(remaining>0)ExperienceOrb.award(level,point,remaining);
    }
    public static boolean validItem(int slot,ItemStack item){
        if(item.isEmpty())return true;if(slot==0 || slot==5)return true;
        return slot>=1 && slot<5 && item.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot()==SLOTS[slot];
    }
}
