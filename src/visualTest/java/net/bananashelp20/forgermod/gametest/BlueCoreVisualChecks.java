package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class BlueCoreVisualChecks {
    private static int ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("forgermod.core.visualQA"))return;
        var mc=Minecraft.getInstance();if(mc.player==null || mc.level==null)return;
        ticks++;
        if(ticks==1) {
            mc.options.pauseOnLostFocus=false;
            mc.setScreen(new Screen(Component.literal("Weapon parts QA")) {
                final ItemStack[] items={new ItemStack(ModItems.AUGMENTED_HEAVY_CORE.get()),new ItemStack(Items.HEAVY_CORE),new ItemStack(Items.DIAMOND_SWORD),new ItemStack(ModItems.CLAYMORE_BLADE.get()),new ItemStack(ModItems.DAGGER_BLADE.get()),new ItemStack(ModItems.AXE_BLADE.get())};
                @Override public boolean isPauseScreen(){return false;}
                @Override public void render(GuiGraphics gui,int x,int y,float delta) {
                    items[2].set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);
                    gui.fill(0,0,width,height,0xff555555);
                    for(int i=0;i<items.length;i++) {
                        int px=25+(i%3)*100,py=20+(i/3)*110;
                        gui.drawString(font,Component.literal(i==0?"Augmented Core":items[i].getHoverName().getString()),px,py,0xffffffff,false);
                        gui.pose().pushPose();gui.pose().translate(px+10,py+15,0);gui.pose().scale(4,4,4);
                        gui.renderItem(items[i],0,0);gui.pose().popPose();
                    }
                }
            });
        }
        if(ticks==50 || ticks==65)Screenshot.grab(mc.gameDirectory,"blue-core-"+ticks+".png",mc.getMainRenderTarget(),c->ForgerMod.LOGGER.info("Blue core QA screenshot: {}",c.getString()));
        if(ticks==85){ForgerMod.LOGGER.info("Blue core render checks complete");mc.stop();}
    }
}
