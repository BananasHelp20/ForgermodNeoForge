package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class AugmentationItemVisualChecks {
    private static int ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("forgermod.augmentationItem.visualQA"))return;
        var mc=Minecraft.getInstance();if(mc.player==null)return;
        if(++ticks==1){
            mc.options.pauseOnLostFocus=false;
            mc.setScreen(new Screen(Component.literal("Augmentation Table icon")){
                @Override public boolean isPauseScreen(){return false;}
                @Override public void render(GuiGraphics gui,int mouseX,int mouseY,float delta){
                    gui.fill(0,0,width,height,0xff737373);
                    int x=width/2-54,y=height/2-54;
                    gui.pose().pushPose();gui.pose().translate(x,y,0);gui.pose().scale(6,6,6);
                    gui.fill(0,0,18,18,0xff333333);gui.fill(1,1,17,17,0xffc6c6c6);
                    gui.renderItem(new ItemStack(ModBlocks.AUGMENTATION_TABLE.get()),1,1);
                    gui.pose().popPose();
                    for(int i=0;i<9;i++){
                        int left=width/2-81+i*18;gui.fill(left,height-28,left+18,height-10,0xff333333);
                        gui.fill(left+1,height-27,left+17,height-11,0xffc6c6c6);
                        gui.renderItem(new ItemStack(ModBlocks.AUGMENTATION_TABLE.get()),left+1,height-27);
                    }
                }
            });
        }
        if(ticks==45)Screenshot.grab(mc.gameDirectory,"augmentation-item-centered.png",mc.getMainRenderTarget(),c->ForgerMod.LOGGER.info("Centered item screenshot: {}",c.getString()));
        if(ticks==65)mc.stop();
    }
}
