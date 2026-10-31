package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.screen.custom.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Isolated, opt-in real-client font/layout checks; never included in the normal mod. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class AugmentationCardVisualChecks {
    private static int ticks,waiting;
    private record Example(ItemStack stack,String first,String second,int rank,boolean complete,boolean hover) {}
    private static Example[] examples;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(!Boolean.getBoolean("forgermod.card.visualQA") || !mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith(java.nio.file.Path.of("build","card-client-run")))return;
        if(mc.player==null || mc.level==null){if(++waiting>600)mc.stop();return;}
        if(ticks++==0){
            mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(3);mc.resizeDisplay();
            int count=0,truncated=0;var clipped=new java.util.TreeMap<String,Integer>();
            for(var entry:ModItems.ITEMS.getEntries()) {
                var stack=new ItemStack(entry.get());if(!Augmentations.eligible(stack))continue;
                for(var ability:Augmentations.pool(stack))for(int rank=0;rank<Augmentations.maxRank(stack,ability.id());rank++) {
                    var text=AugmentUpgradeDescriptions.description(stack,ability.id(),rank,true,true);
                    var layout=AugmentationCardText.offer(mc.font,stack,ability.id(),rank,true);
                    if(layout.lines().size()*mc.font.lineHeight*layout.scale()>36.001)throw new AssertionError("Card text height overflow: "+ability.id());
                    for(var line:layout.lines())if(mc.font.width(line)*layout.scale()>37.001)throw new AssertionError("Card text width overflow: "+ability.id());
                    if(layout.scale()<.45F)throw new AssertionError("Card text unreadable scale");
                    for(var line:layout.lines())AugmentationCardText.readable(line,true).accept((i,style,cp)->{if(style.getColor().getValue()!=AugmentationCardText.DISABLED)throw new AssertionError("Disabled highlight leaks");return true;});
                    if(layout.truncated()){truncated++;clipped.merge(ability.id(),1,Integer::sum);}count++;
                }
            }
            ForgerMod.LOGGER.info("Card layout checks passed: {} offers, {} explicit ellipsis previews",count,truncated);
            ForgerMod.LOGGER.info("Card truncated preview counts: {}",clipped);
            examples=new Example[]{
                new Example(new ItemStack(ModItems.ECHOING_AXE.get()),RecommendedAbilities.ECHO_SALVAGE,RecommendedAbilities.WARDEN_TONE,0,false,false),
                new Example(new ItemStack(ModItems.INFERNAL_CLAYMORE_JADE.get()),RecommendedAbilities.FIRE_RESISTANCE,RecommendedAbilities.HEATING_UP,0,false,false),
                new Example(new ItemStack(ModItems.ASSASSIN_DAGGER.get()),"tooltips.forgermod.ability.leech",AdditionalAbilities.REVENGEFUL_EXIT,0,false,false),
                new Example(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.DIRECT_CURRENT,AdditionalAbilities.THUNDERCLAP,3,false,false),
                new Example(new ItemStack(ModItems.ECHOING_AXE.get()),RecommendedAbilities.ECHO_SALVAGE,RecommendedAbilities.WARDEN_TONE,0,true,false),
                new Example(new ItemStack(ModItems.ECHOING_AXE.get()),RecommendedAbilities.ECHO_SALVAGE,RecommendedAbilities.WARDEN_TONE,0,false,true)
            };
        }
        int index=(ticks-1)/35;
        if(index>=examples.length){ForgerMod.LOGGER.info("Card visual checks complete");mc.stop();return;}
        if((ticks-1)%35==0) {
            var example=examples[index];var stack=example.stack;
            if(example.rank>0)stack=Augmentations.setLevel(stack,example.first,example.rank);
            var table=new AugmentationTableBlockEntity(BlockPos.ZERO,ModBlocks.AUGMENTATION_TABLE.get().defaultBlockState());table.setLevel(mc.level);
            table.inventory.setStackInSlot(example.complete?3:0,stack);
            var data=new SimpleContainerData(AugmentationTableBlockEntity.DATA_SIZE);data.set(0,example.complete?AugmentationAnimation.COMPLETE:AugmentationAnimation.CHOOSE);data.set(5,0);data.set(6,example.rank);data.set(7,0);
            var catalog=Augmentations.catalog(stack);
            for(int i=0;i<catalog.size();i++){if(catalog.get(i).id().equals(example.first))data.set(3,i+1);if(catalog.get(i).id().equals(example.second))data.set(4,i+1);}
            if(data.get(3)==0 || data.get(4)==0)throw new AssertionError("Missing QA offer");
            var screen=new AugmentationTableScreen(new AugmentationTableMenu(1,mc.player.getInventory(),table,data),mc.player.getInventory(),Component.literal("Augmentation Table"));
            mc.setScreen(new Screen(Component.literal("Card QA")) {
                @Override protected void init(){screen.init(minecraft,width,height);}
                @Override public boolean isPauseScreen(){return false;}
                @Override public void render(GuiGraphics gui,int x,int y,float delta){screen.render(gui,example.hover?(width-176)/2+25:-100,example.hover?(height-166)/2+35:-100,delta);}
            });
        }
        if((ticks-1)%35==25)Screenshot.grab(mc.gameDirectory,"card-review-"+index+".png",mc.getMainRenderTarget(),message->ForgerMod.LOGGER.info("Card screenshot: {}",message.getString()));
    }
}
