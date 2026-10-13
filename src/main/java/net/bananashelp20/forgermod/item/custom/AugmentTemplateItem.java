package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class AugmentTemplateItem extends Item {
    private final boolean superTemplate;
    public AugmentTemplateItem(Properties properties, boolean superTemplate) {
        super(properties);
        this.superTemplate = superTemplate;
    }
    public boolean isSuperTemplate() { return superTemplate; }

    @Override
    public boolean isFoil(ItemStack stack) { return !Augmentations.learned(stack).isEmpty(); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(superTemplate
                ? "tooltips.forgermod.super_augment_template.tooltip"
                : "tooltips.forgermod.augment_template.tooltip").withStyle(ChatFormatting.GRAY));
        boolean expanded = WeaponTooltips.descriptionsVisible();
        for (var entry : Augmentations.learned(stack).entrySet()) {
            String id = entry.getKey();
            Component name = Component.translatable("augmentation.forgermod.rank",
                    Augmentations.abilityName(stack, id, entry.getValue()),
                    WeaponTooltips.romanNumeral(entry.getValue()));
            tooltip.add(name.copy().withStyle(ChatFormatting.LIGHT_PURPLE));
            if (expanded) tooltip.add(Component.translatable(id.equals(Augmentations.MATERIAL_HIT)
                    ? "tooltips.forgermod.augment_template.material_hit" : id).withStyle(ChatFormatting.GRAY));
        }
        if (!expanded && !Augmentations.learned(stack).isEmpty()) WeaponTooltips.hint(tooltip);
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
