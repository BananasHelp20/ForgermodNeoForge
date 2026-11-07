package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.custom.WeaponAssemblerBlock;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class AssemblerModelVisualChecks {
    private static int ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(!Boolean.getBoolean("forgermod.assembler.visualQA") || !mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith(java.nio.file.Path.of("build","card-client-run")))return;
        if(mc.player==null || mc.level==null || mc.getSingleplayerServer()==null)return;
        ticks++;
        if(ticks==1){
            mc.options.pauseOnLostFocus=false;mc.options.hideGui=true;mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.getSingleplayerServer().execute(()->{
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                player.setGameMode(GameType.CREATIVE);player.setNoGravity(true);
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                for(int x=-7;x<=17;x++)for(int z=-7;z<=12;z++){
                    player.serverLevel().setBlockAndUpdate(new BlockPos(x,69,z),Blocks.STONE_BRICKS.defaultBlockState());
                    for(int y=70;y<76;y++)player.serverLevel().setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
                }
                var facing=new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
                for(int i=0;i<4;i++)player.serverLevel().setBlockAndUpdate(new BlockPos(i*4,70,0),ModBlocks.WEAPON_ASSEMBLER.get().defaultBlockState().setValue(WeaponAssemblerBlock.FACING,facing[i]));
                player.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(-7,69,-7,18,76,13)).forEach(net.minecraft.world.entity.Entity::discard);
                player.serverLevel().setDayTime(6000);move(mc,0);
            });
        }
        if(ticks<225){mc.player.setYRot(30);mc.player.setXRot(19);}
        if(ticks==85 || ticks==160)mc.getSingleplayerServer().execute(()->{
            for(int i=0;i<4;i++)((net.bananashelp20.forgermod.block.entity.custom.WeaponAssemblerBlockEntity)mc.getSingleplayerServer().overworld().getBlockEntity(new BlockPos(i*4,70,0))).setCrafting(ticks<160);
        });
        if(ticks==65 || ticks==130 || ticks==145 || ticks==210)Screenshot.grab(mc.gameDirectory,"assembler-world-"+ticks+".png",mc.getMainRenderTarget(),message->ForgerMod.LOGGER.info("Assembler screenshot: {}",message.getString()));
        if(ticks==225){
            mc.options.hideGui=false;
            mc.setScreen(new Screen(Component.literal("Assembler item QA")){
                @Override public boolean isPauseScreen(){return false;}
                @Override public void render(GuiGraphics gui,int x,int y,float delta){
                    gui.fill(0,0,width,height,0xff737373);
                    gui.drawString(font,"Weapon Assembler",width/2-45,20,0xffffffff,false);
                    gui.pose().pushPose();gui.pose().translate(width/2-48,height/2-48,0);gui.pose().scale(6,6,6);
                    gui.renderItem(new ItemStack(ModBlocks.WEAPON_ASSEMBLER.get()),0,0);gui.pose().popPose();
                }
            });
        }
        if(ticks==250)Screenshot.grab(mc.gameDirectory,"assembler-inventory.png",mc.getMainRenderTarget(),message->ForgerMod.LOGGER.info("Assembler screenshot: {}",message.getString()));
        if(ticks==270){ForgerMod.LOGGER.info("Assembler visual checks complete");mc.stop();}
    }
    private static void move(Minecraft mc,int offset){
        var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
        player.connection.teleport(offset+3,71.5,-4.5,30,19);
    }
}
