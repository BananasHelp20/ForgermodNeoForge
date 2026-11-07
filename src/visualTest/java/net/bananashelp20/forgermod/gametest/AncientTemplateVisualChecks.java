package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in GPU check of blank, blue-specialized and ordinary foil rendering. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class AncientTemplateVisualChecks {
    private static int ticks;
    private static boolean started;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(!Boolean.getBoolean("forgermod.template.visualQA") || !mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith(java.nio.file.Path.of("build","template-client-run")))return;
        if(!started && mc.screen!=null && mc.getOverlay()==null){
            started=true;mc.options.guiScale().set(2);mc.resizeDisplay();
            var blank=new ItemStack(ModItems.ANCIENT_UPGRADE_TEMPLATE.get());
            var filled=Augmentations.apply(blank,"crafting.forgermod.claymore");
            var ordinary=Augmentations.apply(new ItemStack(ModItems.AUGMENT_TEMPLATE.get()),Augmentations.catalog(new ItemStack(ModItems.AUGMENT_TEMPLATE.get())).getFirst().id());
            mc.setScreen(new Screen(Component.literal("Ancient Template QA")){
                @Override public void render(GuiGraphics gui,int x,int y,float delta){
                    gui.fill(0,0,width,height,0xff30343b);
                    var stacks=java.util.List.of(blank,filled,ordinary);
                    String[] names={"Blank (no foil)","Claymore (blue foil)","Weapon augment (normal foil)"};
                    for(int i=0;i<3;i++){
                        int left=width/2-210+i*145;
                        gui.drawString(font,names[i],left-8,height/2-45,0xffffff);
                        gui.pose().pushPose();gui.pose().translate(left,height/2-20,0);gui.pose().scale(6,6,1);
                        gui.renderItem(stacks.get(i),0,0);gui.pose().popPose();
                    }
                }
            });
        }
        if(!started)return;
        if(++ticks==50)Screenshot.grab(mc.gameDirectory,"ancient-template-glint.png",mc.getMainRenderTarget(),message->ForgerMod.LOGGER.info("Template QA screenshot: {}",message.getString()));
        if(ticks==65){ForgerMod.LOGGER.info("Ancient template render checks complete");mc.stop();}
    }
}
