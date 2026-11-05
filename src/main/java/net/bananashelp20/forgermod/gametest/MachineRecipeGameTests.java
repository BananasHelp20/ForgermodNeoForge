package net.bananashelp20.forgermod.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.recipe.*;
import net.bananashelp20.forgermod.screen.custom.TableInputs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.items.ItemStackHandler;
import java.util.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class MachineRecipeGameTests {
    private static final ResourceLocation CUSTOM=ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"test/datapack_machine");
    private static MachineRecipe recipe(GameTestHelper test,boolean infusion,int time){
        String json="""
                {"ingredient1":{"tag":"minecraft:planks"},"ingredient2":{"item":"minecraft:cobblestone"},"template":{"item":"minecraft:stick"},
                 "result":{"id":"minecraft:diamond","count":3},"count1":3,"count2":2,"template_count":0,"processing_time":%d,"copy_components":false}
                """.formatted(time);
        return (infusion?ModRecipes.INFUSION_SERIALIZER.get():ModRecipes.FORGE_SERIALIZER.get()).codec().codec()
                .parse(test.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE),JsonParser.parseString(json)).getOrThrow();
    }
    private static void install(GameTestHelper test,List<RecipeHolder<?>> original,MachineRecipe recipe){
        var replacements=new ArrayList<>(original);if(recipe!=null)replacements.add(new RecipeHolder<>(CUSTOM,recipe));
        test.getLevel().getRecipeManager().replaceRecipes(replacements);
    }
    private static void fill(ItemStackHandler handler){handler.setStackInSlot(0,new ItemStack(Items.OAK_PLANKS,12));handler.setStackInSlot(1,new ItemStack(Items.COBBLESTONE,12));handler.setStackInSlot(2,new ItemStack(Items.STICK));}
    @GameTest(template="riftfang_test",batch="data_defined_machine_recipes")
    public static void jsonRecipesTagsCountsAndTemplatesDriveBothMachines(GameTestHelper test){
        var original=new ArrayList<>(test.getLevel().getRecipeManager().getRecipes());var pos=test.absolutePos(new BlockPos(2,2,2));
        try{
            for(boolean infusion:new boolean[]{false,true}){
                install(test,original,recipe(test,infusion,4));
                test.getLevel().setBlockAndUpdate(pos,(infusion?ModBlocks.INFUSION_TABLE.get():ModBlocks.FORGE.get()).defaultBlockState());
                ItemStackHandler handler=infusion?((InfusionTableBlockEntity)test.getLevel().getBlockEntity(pos)).itemHandler:((ForgeBlockEntity)test.getLevel().getBlockEntity(pos)).itemStackHandler;
                test.assertTrue(TableInputs.accepts(test.getLevel(),infusion,0,new ItemStack(Items.BIRCH_PLANKS)) && handler.isItemValid(2,new ItemStack(Items.STICK)),"Synced recipe ingredient/tag/template is blocked by old slot rules");
                fill(handler);
                for(int tick=0;tick<4;tick++){if(infusion)((InfusionTableBlockEntity)test.getLevel().getBlockEntity(pos)).tick(test.getLevel(),pos,test.getLevel().getBlockState(pos));else ((ForgeBlockEntity)test.getLevel().getBlockEntity(pos)).tick(test.getLevel(),pos,test.getLevel().getBlockState(pos));}
                test.assertTrue(handler.getStackInSlot(3).is(Items.DIAMOND) && handler.getStackInSlot(3).getCount()==3 && handler.getStackInSlot(0).getCount()==9 && handler.getStackInSlot(1).getCount()==10 && handler.getStackInSlot(2).getCount()==1,"JSON output/counts/time or retained template ignored");
                install(test,original,null);test.assertTrue(MachineCrafting.find(test.getLevel(),infusion?ModRecipes.INFUSION_TYPE.get():ModRecipes.FORGE_TYPE.get(),handler).isEmpty(),"Removed data recipe remains cached");
            }
        }finally{install(test,original,null);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="data_defined_machine_recipes")
    public static void changingRecipesInputsAndSavedProgressCannotCrossCraft(GameTestHelper test){
        var original=new ArrayList<>(test.getLevel().getRecipeManager().getRecipes());var pos=test.absolutePos(new BlockPos(2,2,2));
        try{
            install(test,original,recipe(test,false,4));test.getLevel().setBlockAndUpdate(pos,ModBlocks.FORGE.get().defaultBlockState());
            var forge=(ForgeBlockEntity)test.getLevel().getBlockEntity(pos);fill(forge.itemStackHandler);
            forge.tick(test.getLevel(),pos,forge.getBlockState());forge.tick(test.getLevel(),pos,forge.getBlockState());
            var saved=forge.saveWithoutMetadata(test.getLevel().registryAccess());test.assertTrue(saved.getInt("forge.progress")==2,"Progress did not advance");
            var restored=new ForgeBlockEntity(pos,forge.getBlockState());restored.loadWithComponents(saved,test.getLevel().registryAccess());test.getLevel().setBlockEntity(restored);
            restored.tick(test.getLevel(),pos,restored.getBlockState());test.assertTrue(restored.saveWithoutMetadata(test.getLevel().registryAccess()).getInt("forge.progress")==3,"Unchanged saved recipe lost progress");
            install(test,original,recipe(test,false,6));restored.tick(test.getLevel(),pos,restored.getBlockState());
            test.assertTrue(restored.itemStackHandler.getStackInSlot(3).isEmpty() && restored.saveWithoutMetadata(test.getLevel().registryAccess()).getInt("forge.progress")==1,"Changed recipe ID contents inherited old progress");
            var renamed=restored.itemStackHandler.getStackInSlot(1).copy();renamed.set(DataComponents.CUSTOM_NAME,Component.literal("Different input"));restored.itemStackHandler.setStackInSlot(1,renamed);
            restored.tick(test.getLevel(),pos,restored.getBlockState());test.assertTrue(restored.saveWithoutMetadata(test.getLevel().registryAccess()).getInt("forge.progress")==1,"Different input components inherited progress");
            install(test,original,null);restored.tick(test.getLevel(),pos,restored.getBlockState());test.assertTrue(restored.saveWithoutMetadata(test.getLevel().registryAccess()).getInt("forge.progress")==0,"Removed recipe kept active progress");
        }finally{install(test,original,null);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="data_defined_machine_recipes")
    public static void outputComponentsCapacityAndInsufficientCountsAreTransactional(GameTestHelper test){
        var handler=new ItemStackHandler(4);fill(handler);var recipe=recipe(test,false,1);recipe.output().set(DataComponents.CUSTOM_NAME,Component.literal("Named output"));
        handler.setStackInSlot(3,new ItemStack(Items.DIAMOND,3));
        test.assertFalse(MachineCrafting.craft(test.getLevel(),handler,recipe),"Different output components were merged/overwritten");
        handler.setStackInSlot(3,recipe.output().copyWithCount(62));test.assertFalse(MachineCrafting.craft(test.getLevel(),handler,recipe),"Full output overflowed");
        handler.setStackInSlot(3,recipe.output().copyWithCount(61));test.assertTrue(MachineCrafting.craft(test.getLevel(),handler,recipe) && handler.getStackInSlot(3).getCount()==64,"Matching component output did not merge exactly");
        test.assertTrue(handler.getStackInSlot(0).getCount()==9 && handler.getStackInSlot(1).getCount()==10,"Rejected craft consumed inputs");
        handler.setStackInSlot(3,ItemStack.EMPTY);handler.setStackInSlot(0,new ItemStack(Items.OAK_PLANKS,2));
        test.assertFalse(MachineCrafting.craft(test.getLevel(),handler,recipe) || !handler.getStackInSlot(3).isEmpty(),"Insufficient material crafted");test.succeed();
    }
    @GameTest(template="riftfang_test",batch="data_defined_machine_recipes")
    public static void allBuiltinsLoadAndCodecsPreserveRecipeBookData(GameTestHelper test){
        var manager=test.getLevel().getRecipeManager();test.assertTrue(manager.getAllRecipesFor(ModRecipes.FORGE_TYPE.get()).size()==39 && manager.getAllRecipesFor(ModRecipes.INFUSION_TYPE.get()).size()==108,"Built-in migration lost or duplicated recipes");
        for(var type:List.of(ModRecipes.FORGE_TYPE.get(),ModRecipes.INFUSION_TYPE.get()))for(var holder:manager.getAllRecipesFor(type)){
            var recipe=holder.value();var json=recipe.getSerializer().codec().codec().encodeStart(test.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE),recipe).getOrThrow();
            var decoded=recipe.getSerializer().codec().codec().parse(test.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE),json).getOrThrow();
            var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),test.getLevel().registryAccess());
            try{recipe.getSerializer().streamCodec().encode(buffer,decoded);var wire=recipe.getSerializer().streamCodec().decode(buffer);test.assertTrue(wire.signature(test.getLevel().registryAccess()).equals(recipe.signature(test.getLevel().registryAccess())) && wire.getIngredients().size()==3,"Recipe network/JSON round trip lost JEI ingredient counts, flags or components");}finally{buffer.release();}
        }test.succeed();
    }
}
