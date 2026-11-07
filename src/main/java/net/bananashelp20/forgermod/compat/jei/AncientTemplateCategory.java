package net.bananashelp20.forgermod.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.AncientCraftingTemplateItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Shows the consumed pair, distinct subject and each possible specialization. */
final class AncientTemplateCategory implements IRecipeCategory<AncientTemplateCategory.Specialization> {
    record Specialization(String id){}
    static final RecipeType<Specialization> TYPE=RecipeType.create("forgermod","ancient_template_specialization",Specialization.class);
    private final IDrawable background,icon,slot;
    AncientTemplateCategory(IGuiHelper gui){
        background=gui.createBlankDrawable(160,88);slot=gui.getSlotDrawable();
        icon=gui.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModItems.ANCIENT_UPGRADE_TEMPLATE.get()));
    }
    @Override public RecipeType<Specialization> getRecipeType(){return TYPE;}
    @Override public Component getTitle(){return Component.translatable("jei.forgermod.crafting_specializations");}
    @Override public IDrawable getBackground(){return background;}
    @Override public IDrawable getIcon(){return icon;}
    @Override public void setRecipe(IRecipeLayoutBuilder builder,Specialization recipe,IFocusGroup focuses){
        var blank=new ItemStack(ModItems.ANCIENT_UPGRADE_TEMPLATE.get());
        builder.addSlot(RecipeIngredientRole.INPUT,2,2).setBackground(slot,-1,-1).addItemStack(blank);
        builder.addSlot(RecipeIngredientRole.INPUT,30,2).setBackground(slot,-1,-1).addItemStack(new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()));
        builder.addSlot(RecipeIngredientRole.INPUT,58,2).setBackground(slot,-1,-1).addItemStack(new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get()));
        builder.addSlot(RecipeIngredientRole.OUTPUT,132,2).setBackground(slot,-1,-1).addItemStack(AncientCraftingTemplateItem.specialize(blank,recipe.id()));
    }
    @Override public void draw(Specialization recipe,IRecipeSlotsView slots,GuiGraphics gui,double x,double y){
        gui.drawString(Minecraft.getInstance().font,"->",100,6,0x555555,false);
        gui.drawWordWrap(Minecraft.getInstance().font,Component.translatable("jei.forgermod.specialization_choice"),2,27,156,0x555555);
        gui.drawWordWrap(Minecraft.getInstance().font,Component.translatable("crafting.forgermod.template_consumed"),2,64,156,0x555555);
    }
}
