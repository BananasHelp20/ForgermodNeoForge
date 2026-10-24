package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.LivingEntity;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class SaplingHarvestEvents {
    private SaplingHarvestEvents() {}
    @SubscribeEvent
    public static void drops(LivingDropsEvent event) {
        if(event.isCanceled() || !(PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player) || !player.isAlive() || player.isSpectator()
            || !Augmentations.hasPassive(PassiveActors.weapon(player,AdditionalAbilities.SAPLING_HARVEST),AdditionalAbilities.SAPLING_HARVEST) || !WeaponAbilityTargets.hostileTo(player,event.getEntity())
            || !event.getEntity().level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
        var saplings=BuiltInRegistries.ITEM.getTag(ItemTags.SAPLINGS);if(saplings.isEmpty() || saplings.get().size()==0 || event.getEntity().getRandom().nextFloat()>=.15F) return;
        var candidates=saplings.get().stream().toList();var item=candidates.get(event.getEntity().getRandom().nextInt(candidates.size())).value();var target=event.getEntity();
        var drop=new ItemEntity(target.level(),target.getX(),target.getY(),target.getZ(),new ItemStack(item));drop.setDefaultPickUpDelay();event.getDrops().add(drop);
    }
}
