package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.DaggerCriticalEvents;
import net.bananashelp20.forgermod.item.custom.abilities.NightmareCritState;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class SomniumWeapon extends SwordItemWithEffect {
    private static final NightmareCritState NIGHTMARE = new NightmareCritState();
    public static Holder<MobEffect> effect = MobEffects.CONFUSION;
    public static int durationInTicks = 200;
    public static int effectAmplifier = 4;
    public static Properties pProperties = new Properties().rarity(Rarity.EPIC);
    public String gemstone;
    public String type;

    public SomniumWeapon(String gemstone, String type) {
        super(ModToolTiers.SOMNIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, pProperties, "somnium"),
                type, gemstone, effect, durationInTicks, effectAmplifier);
        this.gemstone = gemstone;
        this.type = type;
    }

    public void axeAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
    }

    public void claymoreAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget);
    }

    public void daggerAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        boolean nightmareHit = pAttacker instanceof ServerPlayer player
                && DaggerCriticalEvents.consume(player, pTarget, pStack)
                && NIGHTMARE.onCritical(player.getUUID());
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget);
        if (nightmareHit && !pTarget.isDeadOrDying()) {
            pTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
            pTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        }
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!isDagger() || slot != WeaponAbilitySlot.PRIMARY) return false;
        boolean enabled = NIGHTMARE.toggle(player.getUUID());
        player.displayClientMessage(Component.translatable(enabled
                ? "message.forgermod.nightmare.enabled"
                : "message.forgermod.nightmare.disabled"), true);
        return true;
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        return isDagger() && slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.absolute_nightmare" : null;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        NIGHTMARE.clear(event.getEntity().getUUID());
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
            case "axe" -> "tooltips.forgermod.dreamweaver_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.nightmare_dagger.tooltip";
            default -> "tooltips.forgermod.dreambound_claymore.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
