package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
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
        if(event.isCanceled() || !(event.getSource().getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
            || !Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.SAPLING_HARVEST) || !WeaponAbilityTargets.hostileTo(player,event.getEntity())
            || !event.getEntity().level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
        var saplings=BuiltInRegistries.ITEM.getTag(ItemTags.SAPLINGS);if(saplings.isEmpty() || saplings.get().size()==0 || event.getEntity().getRandom().nextFloat()>=.15F) return;
        var candidates=saplings.get().stream().toList();var item=candidates.get(event.getEntity().getRandom().nextInt(candidates.size())).value();var target=event.getEntity();
        var drop=new ItemEntity(target.level(),target.getX(),target.getY(),target.getZ(),new ItemStack(item));drop.setDefaultPickUpDelay();event.getDrops().add(drop);
    }
}
