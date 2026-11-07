package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import java.util.List;

/** Crafting specializations are separate from transferable weapon augments. */
public final class AncientCraftingTemplateItem extends Item {
    private static final String KEY="forgermod_crafting_specialization";
    public static final List<Augmentations.Ability> SPECIALIZATIONS=List.of(
            new Augmentations.Ability("crafting.forgermod.claymore",false),
            new Augmentations.Ability("crafting.forgermod.battle_axe",false),
            new Augmentations.Ability("crafting.forgermod.dagger",false));
    public AncientCraftingTemplateItem(Properties properties){super(properties);}
    public static boolean isCraftingTemplate(ItemStack stack){return stack.getItem() instanceof AncientCraftingTemplateItem;}
    public static String specialization(ItemStack stack){
        if(!isCraftingTemplate(stack))return "";
        String value=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString(KEY);
        return SPECIALIZATIONS.stream().anyMatch(a->a.id().equals(value))?value:"";
    }
    public static ItemStack specialize(ItemStack stack,String id){
        if(!isCraftingTemplate(stack) || !specialization(stack).isEmpty()
                || SPECIALIZATIONS.stream().noneMatch(a->a.id().equals(id)))return ItemStack.EMPTY;
        ItemStack result=stack.copyWithCount(1);
        CompoundTag tag=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        tag.putString(KEY,id);result.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return result;
    }
    @Override public boolean isFoil(ItemStack stack){return !specialization(stack).isEmpty();}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> tooltip,TooltipFlag flag){
        tooltip.add(Component.translatable("tooltips.forgermod.ancient_upgrade_template.tooltip").withStyle(ChatFormatting.GRAY));
        String id=specialization(stack);
        if(!id.isEmpty())tooltip.add(Component.translatable(id+".name").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("crafting.forgermod.template_consumed").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack,context,tooltip,flag);
    }
}
