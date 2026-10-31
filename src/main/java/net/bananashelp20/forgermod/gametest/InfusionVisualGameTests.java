package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.custom.InfusionTableBlock;
import net.bananashelp20.forgermod.block.entity.custom.InfusionTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public final class InfusionVisualGameTests {
    private static InfusionTableBlockEntity table(GameTestHelper test,int x){
        var pos=test.absolutePos(new BlockPos(x,2,1));
        test.getLevel().setBlockAndUpdate(pos,ModBlocks.INFUSION_TABLE.get().defaultBlockState());
        return (InfusionTableBlockEntity)test.getLevel().getBlockEntity(pos);
    }
    private static ItemStack weapon(){return Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()),Augmentations.MATERIAL_HIT);}
    private static void fill(InfusionTableBlockEntity table,Item gem){
        table.itemHandler.setStackInSlot(0,new ItemStack(gem,7));
        table.itemHandler.setStackInSlot(1,weapon());
        table.itemHandler.setStackInSlot(2,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()));
    }
    private static void tick(InfusionTableBlockEntity table){table.tick(table.getLevel(),table.getBlockPos(),table.getBlockState());}
    @GameTest(template="riftfang_test",batch="infusion_display_lifecycle")
    public static void everyGemDisplaysImmediatelyAndKeepsIndependentInputsOnCancelAndCompletion(GameTestHelper test){
        var gems=new Item[]{ModItems.RUBY_GEMSTONE.get(),ModItems.AMBER_GEMSTONE.get(),ModItems.AMETHYST_GEMSTONE.get(),ModItems.JADE_GEMSTONE.get()};
        var outputs=new Item[]{ModItems.SHRIEKING_CLAYMORE_RUBY.get(),ModItems.SHRIEKING_CLAYMORE_AMBER.get(),ModItems.SHRIEKING_CLAYMORE_AMETHYST.get(),ModItems.SHRIEKING_CLAYMORE_JADE.get()};
        for(int i=0;i<gems.length;i++){
            var table=table(test,1+i*2);fill(table,gems[i]);
            test.assertTrue(table.displayGemstone().is(gems[i]) && ItemStack.matches(table.displayWeapon(),weapon()) && !table.infusing(),"Inputs are not immediately displayed before crafting starts");
            tick(table);var registry=test.getLevel().registryAccess();var tag=table.getUpdateTag(registry);
            test.assertTrue(tag.getAllKeys().size()==3 && !tag.contains("inventory"),"Visual packets include inventory or progress data");
            var mirror=new InfusionTableBlockEntity(table.getBlockPos(),table.getBlockState());
            mirror.itemHandler.setStackInSlot(1,new ItemStack(Items.DIAMOND_SWORD));
            mirror.handleUpdateTag(tag,registry);
            test.assertTrue(mirror.displayGemstone().is(gems[i]) && mirror.displayGemstone().getCount()==1,"Wrong or multiple gemstones displayed");
            test.assertTrue(ItemStack.matches(mirror.displayWeapon(),weapon()),"Displayed weapon loses its actual augments/components");
            test.assertTrue(mirror.itemHandler.getStackInSlot(1).is(Items.DIAMOND_SWORD),"Visual update overwrites GUI inventory");
            var saved=table.saveWithoutMetadata(registry);var restored=new InfusionTableBlockEntity(table.getBlockPos(),table.getBlockState());restored.loadWithComponents(saved,registry);
            mirror.handleUpdateTag(restored.getUpdateTag(registry),registry);
            test.assertTrue(mirror.displayGemstone().is(gems[i]) && !mirror.displayWeapon().isEmpty(),"Reload loses active displays");
            table.itemHandler.extractItem(1,1,false);
            mirror.onDataPacket(null,(ClientboundBlockEntityDataPacket)table.getUpdatePacket(),registry);
            test.assertTrue(mirror.displayGemstone().is(gems[i]) && mirror.displayWeapon().isEmpty() && !mirror.infusing() && table.itemHandler.getStackInSlot(0).getCount()==7,"Cancel hides remaining gem, retains removed weapon or consumes gemstone");
            tick(table);table.itemHandler.setStackInSlot(1,weapon());
            for(int t=0;t<400;t++)tick(table);
            mirror.onDataPacket(null,(ClientboundBlockEntityDataPacket)table.getUpdatePacket(),registry);
            test.assertTrue(table.itemHandler.getStackInSlot(3).is(outputs[i]) && Augmentations.level(table.itemHandler.getStackInSlot(3),Augmentations.MATERIAL_HIT)==1,"Infusion changed output or lost augments");
            test.assertTrue(table.itemHandler.getStackInSlot(0).getCount()==6 && table.itemHandler.getStackInSlot(2).getCount()==1,"Display changed crafting costs");
            test.assertTrue(mirror.displayGemstone().is(gems[i]) && mirror.displayWeapon().isEmpty() && !mirror.infusing(),"Completion hides remaining gem or retains consumed weapon/animation");
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="infusion_display_invalid")
    public static void independentSlotsBlockedRecipesAndSwappingUpdateImmediately(GameTestHelper test){
        var table=table(test,1);table.itemHandler.setStackInSlot(0,new ItemStack(ModItems.RUBY_GEMSTONE.get()));tick(table);
        test.assertTrue(table.displayGemstone().is(ModItems.RUBY_GEMSTONE.get()) && table.displayWeapon().isEmpty() && !table.infusing(),"Gemstone alone is not immediately displayed");
        fill(table,ModItems.RUBY_GEMSTONE.get());table.itemHandler.setStackInSlot(3,new ItemStack(Items.DIAMOND));tick(table);
        test.assertTrue(!table.displayGemstone().isEmpty() && !table.displayWeapon().isEmpty() && !table.infusing(),"Blocked output hides slot previews or animates invalid infusion");
        table.itemHandler.setStackInSlot(3,ItemStack.EMPTY);tick(table);
        table.itemHandler.setStackInSlot(0,new ItemStack(ModItems.AMETHYST_GEMSTONE.get()));
        var mirror=new InfusionTableBlockEntity(table.getBlockPos(),table.getBlockState());mirror.handleUpdateTag(table.getUpdateTag(test.getLevel().registryAccess()),test.getLevel().registryAccess());
        test.assertTrue(mirror.displayGemstone().is(ModItems.AMETHYST_GEMSTONE.get()),"Gem swap leaves old color displayed");
        table.itemHandler.extractItem(2,1,false);
        test.assertTrue(!table.displayGemstone().isEmpty() && !table.displayWeapon().isEmpty() && !table.infusing(),"Missing template hides inputs or leaves infusion animation active");
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="infusion_single_slots")
    public static void weaponAndGemSynchronizeSeparatelyWithoutTemplateOrTicks(GameTestHelper test){
        var table=table(test,1);var registry=test.getLevel().registryAccess();
        var mirror=new InfusionTableBlockEntity(table.getBlockPos(),table.getBlockState());
        table.itemHandler.setStackInSlot(1,weapon());
        mirror.handleUpdateTag(table.getUpdateTag(registry),registry);
        test.assertTrue(ItemStack.matches(mirror.displayWeapon(),weapon()) && mirror.displayGemstone().isEmpty() && !mirror.infusing(),"Weapon alone did not appear immediately");
        table.itemHandler.setStackInSlot(0,new ItemStack(ModItems.JADE_GEMSTONE.get(),7));
        mirror.handleUpdateTag(table.getUpdateTag(registry),registry);
        test.assertTrue(mirror.displayGemstone().getCount()==1 && mirror.displayGemstone().is(ModItems.JADE_GEMSTONE.get()) && !mirror.infusing(),"Gem requires a template or displays whole stack");
        table.itemHandler.setStackInSlot(1,ItemStack.EMPTY);
        mirror.handleUpdateTag(table.getUpdateTag(registry),registry);
        test.assertTrue(mirror.displayWeapon().isEmpty() && !mirror.displayGemstone().isEmpty(),"Removing weapon also hides gem");
        table.itemHandler.setStackInSlot(0,new ItemStack(Items.DIRT));
        table.itemHandler.setStackInSlot(1,new ItemStack(Items.DIRT));
        mirror.handleUpdateTag(table.getUpdateTag(registry),registry);
        test.assertTrue(mirror.displayWeapon().isEmpty() && mirror.displayGemstone().isEmpty(),"Invalid raw inventory items appear on table");
        fill(table,ModItems.RUBY_GEMSTONE.get());tick(table);
        mirror.handleUpdateTag(table.getUpdateTag(registry),registry);
        for(int t=0;t<20;t++)mirror.clientTick();
        test.assertTrue(mirror.infusing() && mirror.visualActivity(1)==1 && mirror.animationTime(1)>0,"Client animation did not rise during valid infusion");
        table.itemHandler.extractItem(2,1,false);
        mirror.handleUpdateTag(table.getUpdateTag(registry),registry);mirror.clientTick();
        test.assertTrue(!mirror.infusing() && mirror.visualActivity(.5F)>.95 && mirror.visualActivity(.5F)<1,"Cancellation jumps instead of settling smoothly");
        for(int t=0;t<21;t++)mirror.clientTick();
        double settled=mirror.animationTime(1);mirror.clientTick();
        test.assertTrue(mirror.visualActivity(1)==0 && mirror.animationTime(1)==settled && !mirror.displayGemstone().isEmpty() && !mirror.displayWeapon().isEmpty(),"Idle animation keeps rotating or hides remaining slot items");
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="infusion_display_independence")
    public static void simultaneousTablesKeepIndependentRecipesColorsAndDisplayStacks(GameTestHelper test){
        var ruby=table(test,1);var jade=table(test,4);fill(ruby,ModItems.RUBY_GEMSTONE.get());fill(jade,ModItems.JADE_GEMSTONE.get());
        for(int t=0;t<400;t++){
            tick(ruby);tick(jade);
            if(t==0)test.assertTrue(ruby.getCorrectGemstoneTexturePath().contains("ruby") && jade.getCorrectGemstoneTexturePath().contains("jade") && ruby.displayGemstone().is(ModItems.RUBY_GEMSTONE.get()) && jade.displayGemstone().is(ModItems.JADE_GEMSTONE.get()),"Tables share recipe/color/display state");
        }
        test.assertTrue(ruby.itemHandler.getStackInSlot(3).is(ModItems.SHRIEKING_CLAYMORE_RUBY.get()) && jade.itemHandler.getStackInSlot(3).is(ModItems.SHRIEKING_CLAYMORE_JADE.get()),"Concurrent tables produce another table's output");
        for(var facing:Direction.Plane.HORIZONTAL){
            var state=ruby.getBlockState().setValue(InfusionTableBlock.FACING,facing);
            test.assertTrue(Math.abs(state.getShape(test.getLevel(),ruby.getBlockPos()).max(Direction.Axis.Y)-19.5/16)<.0001,"Anvil shape misses model height for "+facing);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void editedModelHasOnlyAWoodenHammerHandleAndProportionalMetalUvs(GameTestHelper test)throws Exception{
        try(var stream=InfusionVisualGameTests.class.getResourceAsStream("/assets/forgermod/models/block/infusion_table.json");
            var reader=new java.io.InputStreamReader(java.util.Objects.requireNonNull(stream),java.nio.charset.StandardCharsets.UTF_8)){
            var model=com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();var textures=model.getAsJsonObject("textures");
            int wooden=0;
            test.assertTrue(model.getAsJsonArray("elements").size()==29,"Deleted objects returned or dynamic gem became static geometry");
            for(var value:model.getAsJsonArray("elements")){
                var cube=value.getAsJsonObject();var a=cube.getAsJsonArray("from");var b=cube.getAsJsonArray("to");
                double x=b.get(0).getAsDouble()-a.get(0).getAsDouble(),y=b.get(1).getAsDouble()-a.get(1).getAsDouble(),z=b.get(2).getAsDouble()-a.get(2).getAsDouble();
                for(var entry:cube.getAsJsonObject("faces").entrySet()){
                    var face=entry.getValue().getAsJsonObject();var uv=face.getAsJsonArray("uv");
                    String texture=textures.get(face.get("texture").getAsString().substring(1)).getAsString();
                    test.assertFalse(texture.contains("item/gemstones"),"Static model displays an unused gemstone");
                    if(texture.contains("spruce") || texture.contains("oak") || texture.contains("planks")){
                        wooden++;test.assertTrue(cube.get("name").getAsString().equals("slender_hammer_handle"),"Shared alias made another object wooden");
                    }
                    double width=switch(entry.getKey()){case "east","west"->z;default->x;};
                    double height=switch(entry.getKey()){case "up","down"->z;default->y;};
                    double u=Math.abs(uv.get(2).getAsDouble()-uv.get(0).getAsDouble()),v=Math.abs(uv.get(3).getAsDouble()-uv.get(1).getAsDouble());
                    test.assertTrue(Math.abs(u/width-v/height)<.0001,"Texture stretches on "+cube.get("name")+" "+entry.getKey());
                }
            }
            test.assertTrue(wooden==6,"Hammer wood is missing from one or more faces");
        }
        test.succeed();
    }

    @GameTest(template="riftfang_test",batch="infusion_inset_base_culling")
    public static void insetBaseNeverCullsTheVisibleFloorForAnyFacingOrCraftingState(GameTestHelper test){
        var pos=test.absolutePos(new BlockPos(2,3,2));var floor=pos.below();var level=test.getLevel();
        var stone=net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();level.setBlockAndUpdate(floor,stone);
        for(var facing:Direction.Plane.HORIZONTAL)for(boolean lit:new boolean[]{false,true}){
            var state=ModBlocks.INFUSION_TABLE.get().defaultBlockState().setValue(InfusionTableBlock.FACING,facing).setValue(InfusionTableBlock.LIT,lit);
            level.setBlockAndUpdate(pos,state);
            test.assertTrue(net.minecraft.world.level.block.Block.shouldRenderFace(stone,level,floor,Direction.UP,pos),"Table hides visible floor around inset base: "+facing+" lit="+lit);
            test.assertFalse(state.canOcclude(),"Inset model still advertises opaque neighbor-face occlusion");
            test.assertTrue(Math.abs(state.getCollisionShape(level,pos).max(Direction.Axis.Y)-19.5/16)<.0001,"Occlusion fix changed anvil collision height");
            test.assertFalse(state.getCollisionShape(level,pos).isEmpty(),"Occlusion fix removed table collision");
        }
        test.succeed();
    }

}
