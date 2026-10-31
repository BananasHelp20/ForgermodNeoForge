package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Explicitly opted-in visual source set; never packaged in the normal mod. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class AugmentationForgeVisualChecks {
    private static int ticks,waiting;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(!Boolean.getBoolean("forgermod.table.visualQA") || !mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith(java.nio.file.Path.of("build","table-client-run")))return;
        if(mc.player==null || mc.level==null || mc.getSingleplayerServer()==null){
            if(++waiting==100){ForgerMod.LOGGER.info("Table visual startup screen: {}",mc.screen);Screenshot.grab(mc.gameDirectory,"startup.png",mc.getMainRenderTarget(),message->{});}
            if(waiting>240)mc.stop();return;
        }
        if(ticks++==0){
            mc.options.hideGui=true;mc.options.pauseOnLostFocus=false;mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(GameType.CREATIVE);p.setNoGravity(true);
                p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
                for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){
                    p.serverLevel().setBlockAndUpdate(new BlockPos(x,69,z),Blocks.STONE_BRICKS.defaultBlockState());
                    for(int y=70;y<=75;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
                }
                p.serverLevel().setDayTime(6000);p.serverLevel().setBlockAndUpdate(new BlockPos(0,70,0),ModBlocks.AUGMENTATION_TABLE.get().defaultBlockState());
                p.teleportTo(3,70.6,-3);p.setYRot(35.54F);p.setXRot(15.5F);
            });
        }
        mc.player.setYRot(35.54F);mc.player.setXRot(15.5F);
        if(ticks==100 || ticks==145 || ticks==210 || ticks==260 || ticks==310 || ticks==370){
            var model=mc.getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"block/augmentation_table_ring")));
            if(model==mc.getModelManager().getMissingModel())throw new IllegalStateException("Augmentation ring failed to bake");
            Screenshot.grab(mc.gameDirectory,"augmentation-forge-"+ticks+".png",mc.getMainRenderTarget(),message->ForgerMod.LOGGER.info("Augmentation forge visual: {}",message.getString()));
        }
        if(ticks==70)mc.getSingleplayerServer().execute(()->{
            var table=(AugmentationTableBlockEntity)mc.getSingleplayerServer().overworld().getBlockEntity(new BlockPos(0,70,0));
            table.inventory.setStackInSlot(0,new ItemStack(ModItems.STATIC_DAGGER_JADE.get()));
        });
        if(ticks==130)mc.getSingleplayerServer().execute(()->{
            var table=(AugmentationTableBlockEntity)mc.getSingleplayerServer().overworld().getBlockEntity(new BlockPos(0,70,0));
            table.inventory.setStackInSlot(0,new ItemStack(ModItems.STATIC_DAGGER_JADE.get()));
            table.inventory.setStackInSlot(1,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()));
            table.inventory.setStackInSlot(2,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get()));table.tick();
        });
        if(ticks==240)mc.getSingleplayerServer().execute(()->{
            var table=(AugmentationTableBlockEntity)mc.getSingleplayerServer().overworld().getBlockEntity(new BlockPos(0,70,0));
            table.inventory.extractItem(AugmentationTableBlockEntity.GEAR,1,false);
        });
        if(ticks==330)mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().overworld().setDayTime(14000));
        if(ticks>395){ForgerMod.LOGGER.info("Augmentation forge visual checks complete");mc.stop();}
    }
}
