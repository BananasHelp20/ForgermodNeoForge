package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class AugmentTemplateItem extends Item {
    public AugmentTemplateItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltips.forgermod.augment_template.tooltip").withStyle(ChatFormatting.GRAY));
        boolean expanded = WeaponTooltips.descriptionsVisible();
        var actives = Augmentations.ids(stack, true);
        for (var entry : Augmentations.learned(stack).entrySet()) {
            String id = entry.getKey();
            Component name = Component.translatable("augmentation.forgermod.rank",
                    Augmentations.abilityName(stack, id, entry.getValue()),
                    WeaponTooltips.romanNumeral(entry.getValue()));
            int slot = actives.indexOf(id);
            tooltip.add(slot >= 0
                    ? Component.translatable("tooltips.forgermod.ability.tooltip", name,
                            Component.keybind(WeaponAbilitySlot.values()[slot].keyTranslation())).withStyle(ChatFormatting.LIGHT_PURPLE)
                    : name.copy().withStyle(ChatFormatting.LIGHT_PURPLE));
            if (expanded) tooltip.add(Component.translatable(id.equals(Augmentations.MATERIAL_HIT)
                    ? "tooltips.forgermod.augment_template.material_hit" : id).withStyle(ChatFormatting.GRAY));
        }
        if (!expanded && !Augmentations.learned(stack).isEmpty()) WeaponTooltips.hint(tooltip);
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
