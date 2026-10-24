package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.LivingEntity;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class FinalStrikeEvents {
    private record Last(ItemStack stack,int slot,ServerLevel level,UUID target) {}
    private static final Map<UUID,Last> LAST=new HashMap<>();
    private FinalStrikeEvents() {}
    private static boolean ready(LivingEntity player) {
        var last=LAST.get(player.getUUID());if(last==null) return false;
        if(!player.isAlive() || player.isSpectator() || PassiveActors.weapon(player,AdditionalAbilities.FINAL_STRIKE)!=last.stack || PassiveActors.selected(player)!=last.slot || PassiveActors.level(player)!=last.level || !Augmentations.hasPassive(last.stack,AdditionalAbilities.FINAL_STRIKE)) {LAST.remove(player.getUUID());return false;}return true;
    }
    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent event) {
        if(!event.isCanceled() && event.getAmount()>0 && PassiveActors.melee(event.getSource()) && PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player && ready(player)
            && LAST.get(player.getUUID()).target.equals(event.getEntity().getUUID()) && WeaponAbilityTargets.enemy(player,event.getEntity()) && event.getEntity().getHealth()<event.getEntity().getMaxHealth()*.1F)
            event.setAmount(event.getAmount()*1.5F);
    }
    @SubscribeEvent
    public static void hit(LivingDamageEvent.Post event) {
        if(PassiveActors.melee(event.getSource()) && PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player && player.isAlive() && !player.isSpectator()
            && Augmentations.hasPassive(PassiveActors.weapon(player,AdditionalAbilities.FINAL_STRIKE),AdditionalAbilities.FINAL_STRIKE) && WeaponAbilityTargets.hostileTo(player,event.getEntity())
            && event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)>0)
            LAST.put(player.getUUID(),new Last(PassiveActors.weapon(player,AdditionalAbilities.FINAL_STRIKE),PassiveActors.selected(player),PassiveActors.level(player),event.getEntity().getUUID()));
    }
    @SubscribeEvent
    public static void tick(PassiveActorTickEvent.Post event) {if(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player) ready(player);}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {LAST.remove(event.getEntity().getUUID());}
    public static void tick(PlayerTickEvent.Post event){tick(new PassiveActorTickEvent.Post(event.getEntity()));}
}
