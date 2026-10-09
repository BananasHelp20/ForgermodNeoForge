package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
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
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;


import java.util.List;

public class SwordItemWithEffect extends SwordItem {
    private final String weaponType;
    private final String gemstoneName;
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
        this.gemstoneName = gemstone;
        this.materialEffect = effect;
        this.materialEffectDuration = ModSpecialRegistry.materialEffectDuration(
                duration + ("jade".equals(gemstone) ? 20 : 0), gemstone, weaponType);
        this.materialEffectAmplifier = amplifier + ("jade".equals(gemstone) ? 1 : 0);
    }

    public String gemstoneName() { return gemstoneName; }
    public boolean hasMaterialEffect() { return materialEffect != null; }

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

    public final void applyMaterialEffect(LivingEntity target, ItemStack stack) {
        if (materialEffect != null && !target.isDeadOrDying()) {
            int rank = Augmentations.level(stack, Augmentations.EMPOWERED_HIT);
            target.addEffect(new MobEffectInstance(materialEffect,
                    Math.round(materialEffectDuration * (1 + .25F * rank)), materialEffectAmplifier));
        }
    }

    protected final void appendWeaponTooltip(ItemStack stack, List<Component> tooltip, String loreKey, String gemstone) {
        if (Augmentations.count(stack) == 0) {
            appendWeaponTooltip(tooltip, loreKey, gemstone);
            return;
        }
        boolean expanded = WeaponTooltips.descriptionsVisible();
        tooltip.add(Component.translatable(loreKey));
        tooltip.add(Component.translatable("augmentation.forgermod.heading", Augmentations.count(stack)).withStyle(ChatFormatting.GRAY));
        if (expanded) tooltip.add(Component.translatable("augmentation.forgermod.core_hint").withStyle(ChatFormatting.GRAY));
        if (isAxe()) WeaponTooltips.passive(tooltip, "tooltips.forgermod.passive.axe", expanded);
        if (isDagger()) WeaponTooltips.passive(tooltip, "tooltips.forgermod.passive.dagger", expanded);
        if (materialEffect != null) {
            tooltip.add(Component.translatable("tooltips.forgermod.passive.material_effect.name", materialEffect.value().getDisplayName()).withStyle(ChatFormatting.GRAY));
            if (expanded) tooltip.add(Component.translatable("tooltips.forgermod.passive.material_effect",
                    materialEffect.value().getDisplayName(), WeaponTooltips.romanNumeral(materialEffectAmplifier + 1),
                    String.format(java.util.Locale.ROOT, "%.2f", materialEffectDuration * (1 + .25 * Augmentations.level(stack, Augmentations.EMPOWERED_HIT)) / 20.0)).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltips.forgermod.passive.heading").withStyle(ChatFormatting.GRAY));
        for (String id : Augmentations.ids(stack, false)) {
            tooltip.add(Component.translatable("augmentation.forgermod.rank", Component.translatable(id + ".name"),
                    WeaponTooltips.romanNumeral(Augmentations.level(stack, id))).withStyle(ChatFormatting.GRAY));
            if (expanded) {
                tooltip.add(Component.translatable(id).withStyle(ChatFormatting.GRAY));
                if (id.startsWith("tooltips.forgermod.passive.")) tooltip.add(Component.translatable(id.replace("tooltips.", "augmentation.") + ".rank").withStyle(ChatFormatting.GRAY));
            }
        }
        tooltip.add(Component.translatable("tooltips.forgermod.ability.heading").withStyle(ChatFormatting.GRAY));
        for (WeaponAbilitySlot slot : WeaponAbilitySlot.values()) {
            String id = Augmentations.activeId(stack, slot);
            if (id == null) continue;
            Component name = Component.translatable("augmentation.forgermod.rank", Component.translatable(id + ".name"),
                    WeaponTooltips.romanNumeral(Augmentations.level(stack, id)));
            tooltip.add(Component.translatable("tooltips.forgermod.ability.tooltip", name,
                    Component.keybind(slot == WeaponAbilitySlot.PRIMARY ? "key.forgermod.ability_primary" : "key.forgermod.ability_secondary")).withStyle(ChatFormatting.GRAY));
            if (expanded) {
                tooltip.add(Component.translatable(id).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("augmentation.forgermod.cooldown",
                        String.format(java.util.Locale.ROOT, "%.2f", Augmentations.cooldown(stack, slot, Augmentations.canonical(stack)) / 20.0)).withStyle(ChatFormatting.GRAY));
            }
        }
        tooltip.add(Component.translatable("tooltips.forgermod." + gemstone + ".tooltip_extra"));
        if (!expanded) WeaponTooltips.hint(tooltip);
    }

    /** Called after a dagger deals damage, including a matching offhand dagger. */
    public void onDaggerHit(LivingEntity target, LivingEntity attacker) {
    }

    /** Override in a material weapon to implement a key-activated ability. Return true only when it activates. */
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        return false;
    }

    /** Whether this slot still has an armed or ongoing effect for the player. */
    public boolean isAbilityActive(ServerPlayer player, WeaponAbilitySlot slot) {
        return false;
    }

    /** End an armed or ongoing effect when the equipped weapon changes. */
    public void cancelAbility(ServerPlayer player, WeaponAbilitySlot slot) {
    }

    /** Cooldown applied after a successful ability. Zero leaves cooldown management to the weapon. */
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        return 0;
    }

    /** Return a description key for each implemented ability; its name uses key + ".name". Null hides an unused slot. */
    @Nullable
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        return null;
    }

    /** Return a translation key for a material-specific passive, or null when absent. */
    @Nullable
    public String passiveDescriptionKey() {
        return null;
    }

    /** Materials with several passives may return several translation keys. */
    public List<String> passiveDescriptionKeys() {
        String key = passiveDescriptionKey();
        return key == null ? List.of() : List.of(key);
    }

    protected final void appendWeaponTooltip(List<Component> tooltip, String loreKey, String gemstone) {
        appendWeaponTooltip(tooltip, loreKey, gemstone, WeaponTooltips.descriptionsVisible());
    }

    protected final void appendWeaponTooltip(List<Component> tooltip, String loreKey, String gemstone, boolean expanded) {
        tooltip.add(Component.translatable(loreKey));
        boolean hasPassives = isAxe() || isDagger() || materialEffect != null || !passiveDescriptionKeys().isEmpty();
        if (hasPassives) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltips.forgermod.passive.heading").withStyle(ChatFormatting.GRAY));
            if (isAxe()) WeaponTooltips.passive(tooltip, "tooltips.forgermod.passive.axe", expanded);
            if (isDagger()) WeaponTooltips.passive(tooltip, "tooltips.forgermod.passive.dagger", expanded);
            if (materialEffect != null) {
                tooltip.add(Component.translatable("tooltips.forgermod.passive.material_effect.name", materialEffect.value().getDisplayName()).withStyle(ChatFormatting.GRAY));
                if (expanded) tooltip.add(Component.translatable("tooltips.forgermod.passive.material_effect",
                        materialEffect.value().getDisplayName(), WeaponTooltips.romanNumeral(materialEffectAmplifier + 1),
                        String.format(java.util.Locale.ROOT, "%.2f", materialEffectDuration / 20.0)).withStyle(ChatFormatting.GRAY));
            }
            for (String passiveKey : passiveDescriptionKeys()) WeaponTooltips.passive(tooltip, passiveKey, expanded);
        }
        boolean hasAbilities = false;
        for (WeaponAbilitySlot slot : WeaponAbilitySlot.values()) {
            String descriptionKey = abilityDescriptionKey(slot);
            if (descriptionKey == null) continue;
            if (!hasAbilities) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("tooltips.forgermod.ability.heading").withStyle(ChatFormatting.GRAY));
                hasAbilities = true;
            }
            String keyId = slot == WeaponAbilitySlot.PRIMARY
                    ? "key.forgermod.ability_primary" : "key.forgermod.ability_secondary";
            tooltip.add(Component.translatable("tooltips.forgermod.ability.tooltip",
                    Component.translatable(descriptionKey + ".name"), Component.keybind(keyId)).withStyle(ChatFormatting.GRAY));
            if (expanded) tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltips.forgermod." + gemstone + ".tooltip_extra"));
        if (!expanded && (hasPassives || hasAbilities)) WeaponTooltips.hint(tooltip);
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
