package net.bananashelp20.forgermod.compat.jei;

import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.*;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.recipe.*;
import net.bananashelp20.forgermod.screen.ModMenuTypes;
import net.bananashelp20.forgermod.screen.custom.ForgeMenu;
import net.bananashelp20.forgermod.screen.custom.InfusionTableMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.*;
import java.util.*;

/** Optional JEI entry point: no common/server code refers to this class. */
@JeiPlugin
public final class ForgerJeiPlugin implements IModPlugin {
    public static final RecipeType<MachineRecipe> FORGE=RecipeType.create("forgermod","forge",MachineRecipe.class);
    public static final RecipeType<MachineRecipe> INFUSION=RecipeType.create("forgermod","infusion_table",MachineRecipe.class);
    @Override public ResourceLocation getPluginUid(){return ResourceLocation.fromNamespaceAndPath("forgermod","machines");}
    @Override public void registerCategories(IRecipeCategoryRegistration registration){
        var gui=registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new Category(gui,false),new Category(gui,true));
    }
    @Override public void registerRecipes(IRecipeRegistration registration){
        var level=Minecraft.getInstance().level;if(level==null)return;
        registration.addRecipes(FORGE,level.getRecipeManager().getAllRecipesFor(ModRecipes.FORGE_TYPE.get()).stream().map(holder->holder.value()).toList());
        registration.addRecipes(INFUSION,level.getRecipeManager().getAllRecipesFor(ModRecipes.INFUSION_TYPE.get()).stream().map(holder->holder.value()).toList());
    }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration){
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FORGE.get()),FORGE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INFUSION_TABLE.get()),INFUSION);
    }
    @Override public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration){
        registration.addRecipeTransferHandler(new Transfer<>(ForgeMenu.class,ModMenuTypes.FORGE_MENU.get(),FORGE,List.of(39,38,37)));
        registration.addRecipeTransferHandler(new Transfer<>(InfusionTableMenu.class,ModMenuTypes.INFUSION_TABLE_MENU.get(),INFUSION,List.of(39,37,38)));
    }
    private record Transfer<C extends AbstractContainerMenu>(Class<C> container,MenuType<C> menu,RecipeType<MachineRecipe> recipeType,List<Integer> recipeSlots) implements IRecipeTransferInfo<C,MachineRecipe>{
        @Override public Class<? extends C> getContainerClass(){return container;}
        @Override public Optional<MenuType<C>> getMenuType(){return Optional.of(menu);}
        @Override public RecipeType<MachineRecipe> getRecipeType(){return recipeType;}
        @Override public boolean canHandle(C menu,MachineRecipe recipe){return true;}
        @Override public List<Slot> getRecipeSlots(C menu,MachineRecipe recipe){return recipeSlots.stream().map(menu::getSlot).toList();}
        @Override public List<Slot> getInventorySlots(C menu,MachineRecipe recipe){return java.util.stream.IntStream.range(0,36).mapToObj(menu::getSlot).toList();}
    }
    public static final class Category implements IRecipeCategory<MachineRecipe> {
        private final boolean infusion;
        private final IDrawable background,icon,slot;
        Category(IGuiHelper gui,boolean infusion){
            this.infusion=infusion;background=gui.createBlankDrawable(160,84);slot=gui.getSlotDrawable();
            icon=gui.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(infusion?ModBlocks.INFUSION_TABLE.get():ModBlocks.FORGE.get()));
        }
        @Override public RecipeType<MachineRecipe> getRecipeType(){return infusion?INFUSION:FORGE;}
        @Override public Component getTitle(){return Component.translatable(infusion?"block.forgermod.infusion_table":"block.forgermod.forge");}
        @Override public IDrawable getBackground(){return background;}
        @Override public IDrawable getIcon(){return icon;}
        @Override public void setRecipe(IRecipeLayoutBuilder builder,MachineRecipe recipe,IFocusGroup focuses){
            for(int i=0;i<3;i++){
                var choices=new ArrayList<ItemStack>();for(var stack:recipe.ingredient(i).getItems())choices.add(stack.copyWithCount(Math.max(1,recipe.cost(i))));
                var input=builder.addSlot(RecipeIngredientRole.INPUT,2+28*i,21).setBackground(slot,-1,-1).addItemStacks(choices);
                if(recipe.cost(i)==0)input.addTooltipCallback((view,tooltip)->tooltip.add(Component.translatable("jei.forgermod.not_consumed")));
                if(i==1 && recipe.requiresJadeCompatible())input.addTooltipCallback((view,tooltip)->tooltip.add(Component.translatable("jei.forgermod.jade_required")));
            }
            builder.addSlot(RecipeIngredientRole.OUTPUT,132,21).setBackground(slot,-1,-1).addItemStack(recipe.output().copy());
        }
        @Override public void draw(MachineRecipe recipe,IRecipeSlotsView slots,GuiGraphics graphics,double mouseX,double mouseY){
            var font=Minecraft.getInstance().font;graphics.drawString(font,"→",101,25,0x555555,false);
            graphics.drawString(font,Component.translatable("jei.forgermod.processing_time",recipe.processingTime()/20.0),2,49,0x555555,false);
            graphics.drawString(font,Component.translatable(recipe.templateCount()==0?"jei.forgermod.template_retained":"jei.forgermod.template_cost",recipe.templateCount()),2,64,0x555555,false);
        }
    }
}
