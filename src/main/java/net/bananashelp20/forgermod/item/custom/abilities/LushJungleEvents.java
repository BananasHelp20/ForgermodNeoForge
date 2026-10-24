package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.LivingEntity;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class LushJungleEvents {
 private LushJungleEvents() {}
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void kill(LivingDeathEvent event) {
  if(event.isCanceled() || !(PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player) || !player.isAlive() || player.isSpectator()
    || event.getEntity().level()!=player.level()
    || !WeaponAbilityTargets.hostileTo(player,event.getEntity()))return;
  if(!Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.LUSH_JUNGLE)
    && !(DaggerItems.hasMatchingDaggers(player) && Augmentations.hasPassive(player.getOffhandItem(),AdditionalAbilities.LUSH_JUNGLE)))return;
  var pos=event.getEntity().blockPosition().below();var level=PassiveActors.level(player);
  if(!level.getWorldBorder().isWithinBounds(pos) || !level.mayInteract(PassiveActors.owner(player),pos)
    || !PassiveActors.owner(player).mayUseItemAt(pos,net.minecraft.core.Direction.UP,new ItemStack(Items.BONE_MEAL)))return;
  if(BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL),level,pos))level.levelEvent(1505,pos,15);
 }
}
