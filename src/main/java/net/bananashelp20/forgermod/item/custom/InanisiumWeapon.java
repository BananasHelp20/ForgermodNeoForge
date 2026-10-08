package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.VoidStepPath;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

import java.util.List;

public class InanisiumWeapon extends SwordItemWithEffect {
    public static Holder<MobEffect> effect = MobEffects.BLINDNESS;
    public static int durationInTicks = 80;
    public static int effectAmplifier = 1;
    public static Properties pProperties = new Properties().rarity(Rarity.EPIC);
    public String gemstone;
    public String type;

    public InanisiumWeapon(String gemstone, String type) {
        super(ModToolTiers.INANISIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, pProperties, "inanisium"),
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
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget);
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!isDagger() || slot != WeaponAbilitySlot.PRIMARY || !player.isAlive()
                || player.isPassenger()) return false;

        Vec3 look = player.getLookAngle();
        double horizontalLength = Math.hypot(look.x, look.z);
        if (horizontalLength < 0.001) return false;
        Vec3 direction = new Vec3(look.x / horizontalLength, 0, look.z / horizontalLength);
        Vec3 start = player.position();
        AABB originalBox = player.getBoundingBox();
        int clearSteps = VoidStepPath.furthestClearStep(step -> {
            Vec3 offset = direction.scale(step * VoidStepPath.STEP_DISTANCE);
            AABB box = originalBox.move(offset);
            return player.serverLevel().getWorldBorder().isWithinBounds(box)
                    && player.serverLevel().noCollision(player, box);
        });
        if (clearSteps < 2) return false;

        Vec3 destination = start.add(direction.scale(clearSteps * VoidStepPath.STEP_DISTANCE));
        BlockPos departure = player.blockPosition();
        if (!player.teleportTo(player.serverLevel(), destination.x, destination.y, destination.z,
                Set.of(), player.getYRot(), player.getXRot())) return false;
        player.fallDistance = 0;
        player.serverLevel().playSound(null, departure, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 0.7F, 1.2F);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 0.7F, 1.2F);
        return true;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        return isDagger() && slot == WeaponAbilitySlot.PRIMARY ? 100 : 0;
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        return isDagger() && slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.void_step" : null;
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
            case "axe" -> "tooltips.forgermod.nullified_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.riftfang_dagger.tooltip";
            default -> "tooltips.forgermod.claymore_of_the_void.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
