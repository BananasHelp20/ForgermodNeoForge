package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.WeaponAssemblerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class AssemblerAnimationGameTests {
    @GameTest(template="riftfang_test")
    public static void animationSyncStopsSmoothlyAndNeverCreatesCraftingJobs(GameTestHelper test){
        var pos=test.absolutePos(new BlockPos(1,2,1));
        test.getLevel().setBlockAndUpdate(pos,ModBlocks.WEAPON_ASSEMBLER.get().defaultBlockState());
        var server=(WeaponAssemblerBlockEntity)test.getLevel().getBlockEntity(pos);
        var client=new WeaponAssemblerBlockEntity(pos,server.getBlockState());
        client.inventory.setStackInSlot(0,new ItemStack(Items.DIAMOND));
        for(int i=0;i<30;i++)client.clientAnimationTick();
        test.assertTrue(client.visualActivity(1)==0 && client.animationTime(1)==0,"Idle arms move");
        server.setCrafting(true);
        var registry=test.getLevel().registryAccess();
        var update=server.getUpdateTag(registry);
        test.assertTrue(update.getAllKeys().size()==1 && !update.contains("inventory"),"Animation packets include inventory");
        client.handleUpdateTag(update,registry);
        for(int i=0;i<30;i++)client.clientAnimationTick();
        test.assertTrue(client.isCrafting() && client.visualActivity(1)==1 && client.animationTime(1)>0,"Start packet doesn't animate");
        server.setCrafting(false);
        client.onDataPacket(null,server.getUpdatePacket(),registry);
        client.clientAnimationTick();
        test.assertTrue(client.visualActivity(1)>0 && client.visualActivity(1)<1,"Cancel snaps arms to rest");
        for(int i=0;i<30;i++)client.clientAnimationTick();
        double stopped=client.animationTime(1);client.clientAnimationTick();
        test.assertTrue(!client.isCrafting() && client.visualActivity(1)==0 && client.animationTime(1)==stopped,"Arms do not settle at rest");
        test.assertTrue(client.inventory.getStackInSlot(0).is(Items.DIAMOND) && server.inventory.getStackInSlot(0).isEmpty(),"Animation changes items");
        server.setCrafting(true);
        var restored=new WeaponAssemblerBlockEntity(pos,server.getBlockState());
        restored.loadWithComponents(server.saveWithoutMetadata(registry),registry);
        test.assertFalse(restored.isCrafting(),"Reload starts a nonexistent crafting job");
        test.succeed();
    }
}
