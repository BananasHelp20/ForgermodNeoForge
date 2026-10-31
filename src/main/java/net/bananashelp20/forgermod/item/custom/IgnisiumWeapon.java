package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.abilities.PassiveActors;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class IgnisiumWeapon extends SwordItemWithEffect {
    public static final String PYROMANIAC = "tooltips.forgermod.ability.pyromaniac";
    public static int pyromaniacHealing(int rank) { return net.bananashelp20.forgermod.augmentation.ReviewedRanks.at(rank,1,2,4,6); }
    public static Holder<MobEffect> effect = MobEffects.GLOWING;
    public static int durationInTicks = 2000;
    public static int effectAmplifier = 1;
    public String gemstone;
    public String type;

    public IgnisiumWeapon(String gemstone, String type) {
        super(ModToolTiers.IGNISIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, new Properties().rarity(Rarity.EPIC), "ignisium"),
                type, gemstone, effect, durationInTicks, effectAmplifier);
        this.gemstone = gemstone;
        this.type = type;
    }

    public void axeAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget, pStack);
    }

    public void claymoreAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget, pStack);
    }

    public void daggerAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget, pStack);
    }

    @Override
    public List<String> passiveDescriptionKeys() {
        return isDagger() ? List.of(PYROMANIAC) : List.of();
    }

    @SubscribeEvent
    public static void onDamageDealt(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0 || !(event.getEntity() instanceof net.minecraft.world.entity.Mob)
                || !PassiveActors.melee(event.getSource())
                || !(PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player)
                || !player.isAlive() || player.isSpectator() || player.getRemainingFireTicks() <= 0
                || !(player.getMainHandItem().getItem() instanceof IgnisiumWeapon weapon) || !weapon.isDagger()) return;
        int rank = Augmentations.hasPassive(player.getMainHandItem(), PYROMANIAC) ? Augmentations.level(player.getMainHandItem(), PYROMANIAC) : 0;
        if (net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems.hasMatchingDaggers(player)
                && Augmentations.hasPassive(player.getOffhandItem(), PYROMANIAC))
            rank = Math.max(rank, Augmentations.level(player.getOffhandItem(), PYROMANIAC));
        if (rank > 0) {
            player.clearFire();
            player.heal(pyromaniacHealing(rank));
        }
    }

    @Override
    public void postHurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        switch (this.type) {
            case "claymore":
                claymoreAttack(pStack, pTarget, pAttacker);
                break;
            case "axe":
                axeAttack(pStack, pTarget, pAttacker);
                break;
            case "dagger":
                daggerAttack(pStack, pTarget, pAttacker);
                break;
            default: claymoreAttack(pStack, pTarget, pAttacker);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        String loreKey = switch (this.type) {
            case "axe" -> "tooltips.forgermod.molten_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.emberfang_dagger.tooltip";
            default -> "tooltips.forgermod.infernal_claymore.tooltip";
        };
        appendWeaponTooltip(pStack, pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
