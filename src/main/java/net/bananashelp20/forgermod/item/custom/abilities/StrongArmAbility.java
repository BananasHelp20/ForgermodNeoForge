package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class StrongArmAbility {
    private static final class Armed {
        final ItemStack stack;final int selected;final ServerLevel level;
        UUID target;long tick;DamageSource source;
        Armed(ServerPlayer player,ItemStack stack) {this.stack=stack;selected=player.getInventory().selected;level=player.serverLevel();}
        void clearHit() {target=null;source=null;}
    }
    private static final Map<UUID,Armed> ARMED=new HashMap<>();
    private StrongArmAbility() {}
    public static float chance(int rank) { return switch(Math.clamp(rank,1,4)) { case 1->.3F; case 2->.55F; case 3->.8F; default->1; }; }
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(!player.isAlive() || player.isSpectator() || WeaponControlLocks.frozen(player)
                || stack!=player.getMainHandItem() || !Augmentations.ids(stack,true).contains(RecommendedAbilities.STRONG_ARM)) return false;
        if(active(player))return true; // Already ready: re-pressing never requires a weapon switch.
        // Arm the next qualifying hit; target and armor checks belong to the actual attack.
        ARMED.put(player.getUUID(),new Armed(player,stack));return true;
    }
    public static boolean active(ServerPlayer player) {
        var armed=ARMED.get(player.getUUID());if(armed==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=armed.stack
                || player.getInventory().selected!=armed.selected || player.serverLevel()!=armed.level
                || !Augmentations.ids(armed.stack,true).contains(RecommendedAbilities.STRONG_ARM)) {cancel(player.getUUID());return false;}
        if(armed.tick!=player.level().getGameTime()) armed.clearHit();
        return true;
    }
    public static void cancel(UUID owner) {ARMED.remove(owner);}
    private static ArrayList<EquipmentSlot> armor(LivingEntity target) {
        var armor=new ArrayList<EquipmentSlot>();
        for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET,EquipmentSlot.BODY})
            if(target.getItemBySlot(slot).getItem() instanceof ArmorItem) armor.add(slot);
        return armor;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void attack(AttackEntityEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        var armed=ARMED.get(player.getUUID());if(armed!=null) armed.clearHit();
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void critical(CriticalHitEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;
        var armed=ARMED.get(player.getUUID());armed.clearHit();
        if(!event.isCriticalHit() || player.getAttackStrengthScale(0)<1 || WeaponControlLocks.frozen(player)
                || !(event.getTarget() instanceof LivingEntity target) || target==player || !target.isAlive() || target.isSpectator()
                || target.isInvulnerableTo(player.damageSources().playerAttack(player))) return;
        armed.target=target.getUUID();armed.tick=player.level().getGameTime();
    }
    private static Armed hit(ServerPlayer player,LivingEntity target) {
        if(!active(player)) return null;
        var armed=ARMED.get(player.getUUID());
        return target.getUUID().equals(armed.target) && armed.tick==player.level().getGameTime()?armed:null;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void incoming(LivingIncomingDamageEvent event) {
        if(!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        var armed=hit(player,event.getEntity());
        // Bind the actual direct attack, including rejected hits; later area damage cannot borrow its critical flag.
        if(armed!=null && armed.source==null) armed.source=event.getSource();
    }
    @SubscribeEvent
    public static void landed(LivingDamageEvent.Post event) {
        if(!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        var armed=hit(player,event.getEntity());
        if(armed==null || armed.source!=event.getSource()
                || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0) return;
        var target=event.getEntity();var armor=armor(target);if(armor.isEmpty()) return;
        cancel(player.getUUID());
        if(player.getRandom().nextFloat()<chance(Augmentations.level(armed.stack,RecommendedAbilities.STRONG_ARM))) {
            var slot=armor.get(player.getRandom().nextInt(armor.size()));
            var piece=target.getItemBySlot(slot); var item=piece.getItem();
            // Consume the real item, including bank-owned reinforcement equipment.
            piece.shrink(1);target.setItemSlot(slot,piece.isEmpty()?ItemStack.EMPTY:piece);
            if(target instanceof net.minecraft.world.entity.Mob mob)NecromancerEquipment.changed(mob);
            target.onEquippedItemBroken(item,slot);
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {if(event.getEntity() instanceof ServerPlayer player) active(player);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
