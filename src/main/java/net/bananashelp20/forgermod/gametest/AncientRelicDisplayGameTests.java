package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.custom.AncientRelicDisplayBlock;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.gametest.*;
import java.util.List;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class AncientRelicDisplayGameTests {
    private static final BlockPos POS=new BlockPos(2,1,2);
    private static List<Block> displays(){return List.of(ModBlocks.ANCIENT_DAGGER_PEDESTAL.get(),ModBlocks.ANCIENT_AXE_STAND.get());}
    private static Item reward(Block display){return display==ModBlocks.ANCIENT_AXE_STAND.get()?ModItems.RUSTY_AXE.get():ModItems.RUSTY_DAGGER.get();}
    @GameTest(template="empty")
    public static void eachDisplayAwardsExactlyOneCorrectRelic(GameTestHelper test){
        var first=test.makeMockPlayer(GameType.SURVIVAL);var second=test.makeMockPlayer(GameType.SURVIVAL);
        for(var block:displays()){
            test.setBlock(POS,block);first.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));
            test.useBlock(POS,first);test.useBlock(POS,second);test.useBlock(POS,first);
            test.assertTrue(first.getInventory().countItem(reward(block))==1 && second.getInventory().countItem(reward(block))==0,"Repeated/held-item clicks duplicated or lost relic");
            test.assertFalse(test.getBlockState(POS).getValue(AncientRelicDisplayBlock.FULL),"Claim did not empty display");
            test.assertTrue(test.getLevel().getBlockEntity(test.absolutePos(POS))==null,"Static display unnecessarily creates a ticking block entity");
        }test.succeed();
    }
    @GameTest(template="empty")
    public static void fullInventoryDropsOnlyOneRelic(GameTestHelper test){
        var player=test.makeMockPlayer(GameType.SURVIVAL);player.setPos(Vec3.atCenterOf(test.absolutePos(POS)));
        for(int i=0;i<player.getInventory().items.size();i++)player.getInventory().items.set(i,new ItemStack(Items.COBBLESTONE,64));
        for(var block:displays()){
            test.setBlock(POS,block);test.useBlock(POS,player);test.useBlock(POS,player);
            int count=test.getLevel().getEntitiesOfClass(ItemEntity.class,test.getBounds(),e->e.getItem().is(reward(block))).stream().mapToInt(e->e.getItem().getCount()).sum();
            test.assertTrue(count==1,"Inventory overflow lost or duplicated relic");
        }test.succeed();
    }
    @GameTest(template="empty")
    public static void dropsPreserveFullAndEmptyStatesAcrossPlacement(GameTestHelper test){
        var player=test.makeMockPlayer(GameType.SURVIVAL);
        for(var block:displays())for(boolean full:new boolean[]{true,false}){
            var state=block.defaultBlockState().setValue(AncientRelicDisplayBlock.FULL,full);test.setBlock(POS,state);
            var drops=Block.getDrops(state,test.getLevel(),test.absolutePos(POS),null);
            test.assertTrue(drops.size()==1 && drops.getFirst().is(block.asItem()) && drops.getFirst().get(DataComponents.BLOCK_STATE)!=null,"Stand loot lost its full/empty state");
            var item=drops.getFirst();test.setBlock(POS,Blocks.AIR);player.setPos(Vec3.atCenterOf(test.absolutePos(POS)).add(0,0,3));player.setItemInHand(InteractionHand.MAIN_HAND,item);
            var hit=new BlockHitResult(Vec3.atCenterOf(test.absolutePos(POS)),Direction.UP,test.absolutePos(POS),false);
            test.assertTrue(((BlockItem)item.getItem()).place(new BlockPlaceContext(player,InteractionHand.MAIN_HAND,item,hit)).consumesAction(),"Dropped display could not be placed");
            test.assertTrue(test.getBlockState(POS).getValue(AncientRelicDisplayBlock.FULL)==full,"Replaced display reset its claimed state");
        }test.succeed();
    }
    @GameTest(template="empty")
    public static void facingsRotationMirrorShapesAndMiningTags(GameTestHelper test){
        for(var block:displays())for(var direction:Direction.Plane.HORIZONTAL){
            var state=block.defaultBlockState().setValue(AncientRelicDisplayBlock.FACING,direction);boolean axe=block==ModBlocks.ANCIENT_AXE_STAND.get();
            test.assertTrue(state.rotate(Rotation.CLOCKWISE_90).getValue(AncientRelicDisplayBlock.FACING)==direction.getClockWise(),"Structure rotation ignored facing");
            test.assertTrue(state.mirror(Mirror.LEFT_RIGHT).getValue(AncientRelicDisplayBlock.FACING)==Mirror.LEFT_RIGHT.mirror(direction),"Mirroring ignored facing");
            for(boolean full:new boolean[]{false,true}){
                var pose=state.setValue(AncientRelicDisplayBlock.FULL,full);var shape=pose.getShape(test.getLevel(),test.absolutePos(POS),CollisionContext.empty());
                double height=axe?(full?27:18):(full?10.25:9.5);
                test.assertTrue(Math.abs(shape.max(Direction.Axis.Y)-height/16)<1e-6,"Collision height does not match model");
                test.assertTrue(shape==pose.getShape(test.getLevel(),test.absolutePos(POS),CollisionContext.empty()),"Collision shape is not cached");
            }
            test.assertTrue(state.is(BlockTags.MINEABLE_WITH_PICKAXE) && state.is(BlockTags.NEEDS_IRON_TOOL),"Display missing mining tags");
            test.assertTrue(state.requiresCorrectToolForDrops() && !new ItemStack(Items.WOODEN_PICKAXE).isCorrectToolForDrops(state) && new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(state),"Mining properties disagree with iron-tool requirement");
            var point=new Vec3(8/16.0,2.5/16,12/16.0);
            if(axe){
                for(var sample:new Vec3[]{point,new Vec3(1.05/16,25.9/16,10.2/16)}){
                    var rotated=switch(direction){case EAST->new Vec3(1-sample.z,sample.y,sample.x);case SOUTH->new Vec3(1-sample.x,sample.y,1-sample.z);case WEST->new Vec3(sample.z,sample.y,1-sample.x);default->sample;};
                    test.assertTrue(state.getShape(test.getLevel(),test.absolutePos(POS)).toAabbs().stream().anyMatch(box->box.contains(rotated)),"Axe edge or foot rim is outside collision shape");
                }
            }
        }test.succeed();
    }
}
