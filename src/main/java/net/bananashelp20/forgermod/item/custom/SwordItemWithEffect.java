package net.bananashelp20.forgermod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;


import java.util.List;

public class SwordItemWithEffect extends SwordItem {
    private final String weaponType;
    @Nullable private final Holder<MobEffect> materialEffect;
    private final int materialEffectDuration;
    private final int materialEffectAmplifier;

    public SwordItemWithEffect(Tier pTier, Properties pProperties) {
        this(pTier, pProperties, "sword");
    }

    public SwordItemWithEffect(Tier pTier, Properties pProperties, String weaponType) {
        this(pTier, pProperties, weaponType, "no_gemstone", null, 0, 0);
    }

    public SwordItemWithEffect(Tier pTier, Properties pProperties, String weaponType, String gemstone,
                               @Nullable Holder<MobEffect> effect, int duration, int amplifier) {
        super(pTier, pProperties);
        this.weaponType = weaponType;
        this.materialEffect = effect;
        this.materialEffectDuration = duration + ("jade".equals(gemstone) ? 20 : 0);
        this.materialEffectAmplifier = amplifier + ("jade".equals(gemstone) ? 1 : 0);
    }

    public boolean isDagger() {
        return "dagger".equals(weaponType);
    }

    public boolean isAxe() {
        return "axe".equals(weaponType);
    }

    public final void applyMaterialEffect(LivingEntity target) {
        if (materialEffect != null && !target.isDeadOrDying()) {
            target.addEffect(new MobEffectInstance(materialEffect, materialEffectDuration, materialEffectAmplifier));
        }
    }

    /** Called after a dagger deals damage, including a matching offhand dagger. */
    public void onDaggerHit(LivingEntity target, LivingEntity attacker) {
    }

    /** Override in a material weapon to implement a key-activated ability. Return true only when it activates. */
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        return false;
    }

    /** Cooldown applied after a successful ability. Zero leaves cooldown management to the weapon. */
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        return 0;
    }

    /** Return a translation key for each implemented ability; null hides an unused slot. */
    @Nullable
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        return null;
    }

    protected final void appendWeaponTooltip(List<Component> tooltip, String loreKey, String gemstone) {
        tooltip.add(Component.translatable(loreKey));
        if (isAxe()) tooltip.add(Component.translatable("tooltips.forgermod.passive.axe"));
        if (isDagger()) tooltip.add(Component.translatable("tooltips.forgermod.passive.dagger"));
        for (WeaponAbilitySlot slot : WeaponAbilitySlot.values()) {
            String descriptionKey = abilityDescriptionKey(slot);
            if (descriptionKey != null) {
                String keyId = slot == WeaponAbilitySlot.PRIMARY
                        ? "key.forgermod.ability_primary" : "key.forgermod.ability_secondary";
                tooltip.add(Component.translatable("tooltips.forgermod.ability.tooltip",
                        Component.keybind(keyId), Component.translatable(descriptionKey)));
            }
        }
        tooltip.add(Component.translatable("tooltips.forgermod." + gemstone + ".tooltip_extra"));
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(Tool.Rule.minesAndDrops(List.of(Blocks.COBWEB), 15.0F), Tool.Rule.overrideSpeed(BlockTags.SWORD_EFFICIENT, 1.5F)), 1.0F, 2);
    }

    public static ItemAttributeModifiers createAttributes(Tier pTier, int pAttackDamage, float pAttackSpeed) {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)((float)pAttackDamage + pTier.getAttackDamageBonus()), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)pAttackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        if (!pTarget.isDeadOrDying()) pTarget.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 1)); //duration -> Ticks, AMPLIFIER
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility itemAbility) {
        return net.neoforged.neoforge.common.ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
    }
}
