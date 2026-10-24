package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class LonelyDreamEvents {
    public static final ResourceLocation MODIFIER=ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"lonely_dream");
    private record Watch(ItemStack stack,int selected,long next) {}
    private static final Map<UUID,Watch> WATCHES=new HashMap<>();
    private LonelyDreamEvents() {}
    public static void clear(LivingEntity player) {WATCHES.remove(player.getUUID());player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(MODIFIER);}
    private static boolean eligible(LivingEntity player) {return player.isAlive() && !player.isSpectator() && Augmentations.hasPassive(PassiveActors.weapon(player,AdditionalAbilities.LONELY_DREAM),AdditionalAbilities.LONELY_DREAM);}
    public static void refresh(LivingEntity player) {
        if(!eligible(player)) {clear(player);return;}
        int enemies=player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(10),
                e->WeaponAbilityTargets.enemy(player,e) && player.distanceToSqr(e)<=100).size();
        var speed=player.getAttribute(Attributes.ATTACK_SPEED);var previous=speed.getModifier(MODIFIER);double amount=enemies*.05;
        if(previous==null || Math.abs(previous.amount()-amount)>.000001) {
            speed.removeModifier(MODIFIER);
            if(enemies>0) speed.addTransientModifier(new AttributeModifier(MODIFIER,amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        WATCHES.put(player.getUUID(),new Watch(PassiveActors.weapon(player,AdditionalAbilities.LONELY_DREAM),PassiveActors.selected(player),player.level().getGameTime()+10));
    }
    @SubscribeEvent
    public static void tick(PassiveActorTickEvent.Pre event) {
        if(!(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player)) return;
        if(!eligible(player)) {clear(player);return;}
        var watch=WATCHES.get(player.getUUID());
        if(watch==null || watch.stack()!=PassiveActors.weapon(player,AdditionalAbilities.LONELY_DREAM) || watch.selected()!=PassiveActors.selected(player) || player.level().getGameTime()>=watch.next()) refresh(player);
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {if(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player) clear(player);}
    public static void tick(PlayerTickEvent.Pre event){tick(new PassiveActorTickEvent.Pre(event.getEntity()));}
}
