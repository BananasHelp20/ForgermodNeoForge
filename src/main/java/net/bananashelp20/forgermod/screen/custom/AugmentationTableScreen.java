package net.bananashelp20.forgermod.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bananashelp20.forgermod.augmentation.AugmentationAnimation;
import net.bananashelp20.forgermod.item.custom.WeaponTooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import java.util.List;

public final class AugmentationTableScreen extends AbstractContainerScreen<AugmentationTableMenu> {
    private static final String DIRECTORY="textures/gui/augementation_table/";
    private static final ResourceLocation BASE=texture("augmentation_table_gui.png");
    private static final ResourceLocation[] OVERLAYS={null,texture("augmentation_table_analyze_progress.png"),
            texture("augmentation_table_analyze_to_selection_transtion_1.png"),
            texture("augmentation_table_analyze_to_selection_transition_2.png"),texture("augmentation_table_selection_finalisation.png")};
    private static final int CARD_Y=15,CARD_WIDTH=39,CARD_HEIGHT=54;
    private static final int[] CARD_X={12,125};
    private static ResourceLocation texture(String name) { return ResourceLocation.fromNamespaceAndPath("forgermod",DIRECTORY+name); }
    public AugmentationTableScreen(AugmentationTableMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title); imageWidth=176; imageHeight=166; titleLabelY=4; inventoryLabelX=8; inventoryLabelY=73;
    }
    @Override protected void renderBg(GuiGraphics gui,float delta,int mouseX,int mouseY) {
        gui.blit(BASE,leftPos,topPos,0,0,imageWidth,imageHeight);
    }
    @Override protected void renderLabels(GuiGraphics gui,int mouseX,int mouseY) {
        super.renderLabels(gui,mouseX,mouseY);
        if(menu.phase()==AugmentationAnimation.IDLE) return;
        // Slot icons render at z=250 and their counts at z=300. Flush deferred slot
        // decorations, then cover both below floating cursor items (z=382) and tooltips.
        gui.flush();
        gui.pose().pushPose();
        gui.pose().translate(0,0,350);
        try {
            renderAugmentationLayers(gui,mouseX,mouseY);
        } finally {
            gui.flush();
            gui.pose().popPose();
        }
    }
    private void renderAugmentationLayers(GuiGraphics gui,int mouseX,int mouseY) {
        RenderSystem.enableBlend();
        for(int stage=1;stage<=4;stage++) {
            if(menu.phase()<stage) break;
            int width=AugmentationAnimation.WIDTH[stage];
            if(menu.phase()>stage) crop(gui,stage,0,width);
            else {
                int pixels=AugmentationAnimation.revealedPixels(stage,menu.progress(),menu.duration());
                if(stage<=2) {
                    crop(gui,stage,0,pixels);
                    int right=Math.min(pixels,width-pixels);
                    crop(gui,stage,width-right,right);
                } else {
                    int middle=width/2;
                    crop(gui,stage,middle-pixels,pixels); crop(gui,stage,middle,pixels);
                }
            }
        }
        if(menu.phase()>=AugmentationAnimation.CHOOSE) for(int i=0;i<2;i++) renderCard(gui,i,mouseX-leftPos,mouseY-topPos);
    }
    private void crop(GuiGraphics gui,int stage,int start,int width) {
        if(width<=0) return;
        gui.blit(OVERLAYS[stage],AugmentationAnimation.X[stage]+start,AugmentationAnimation.Y[stage],0,
                (float)start,0,width,AugmentationAnimation.HEIGHT[stage],AugmentationAnimation.WIDTH[stage],AugmentationAnimation.HEIGHT[stage]);
    }
    private boolean overCard(int card,double x,double y) {
        return x>=CARD_X[card] && x<CARD_X[card]+CARD_WIDTH && y>=CARD_Y && y<CARD_Y+CARD_HEIGHT;
    }
    private void renderCard(GuiGraphics gui,int card,int mouseX,int mouseY) {
        var offer=menu.offer(card); if(offer==null) return;
        int x=CARD_X[card],y=CARD_Y;
        boolean completed=menu.phase()==AugmentationAnimation.COMPLETE;
        boolean rejected=completed && menu.selected()!=card;
        boolean hovered=!completed && overCard(card,mouseX,mouseY);
        int edge=rejected?0xff686868:completed?0xff78df96:hovered?0xffe2ffff:0xff51d8f5;
        gui.fill(x,y,x+CARD_WIDTH,y+CARD_HEIGHT,edge);
        gui.fill(x+1,y+1,x+CARD_WIDTH-1,y+CARD_HEIGHT-1,rejected?0xff777777:hovered?0xffd3eced:0xffb5c6c8);
        gui.fill(x+2,y+11,x+CARD_WIDTH-2,y+12,rejected?0xff999999:0xff549ca6);
        // Code-rendered cards adapt to every translated ability and need no separate PNG per ability.
        gui.pose().pushPose(); gui.pose().translate(x+3,y+3,0); gui.pose().scale(.5F,.5F,1);
        int textColor=rejected?0xff555555:0xff20353b;
        Component kind=Component.translatable(menu.offerRank(card)==0?"augmentation.forgermod.card.learn":"augmentation.forgermod.card.upgrade");
        gui.drawString(font,kind,0,0,textColor,false);
        int lineY=20;
        Component name=offer.name().copy().append(" "+WeaponTooltips.romanNumeral(menu.offerRank(card)+1));
        for(var line:font.split(name,66)) { if(lineY>47) break; gui.drawString(font,line,0,lineY,textColor,false); lineY+=9; }
        lineY+=4;
        List<FormattedCharSequence> description=font.split(Component.translatable(offer.id()),66);
        for(var line:description) { if(lineY>89) break; gui.drawString(font,line,0,lineY,textColor,false); lineY+=9; }
        gui.pose().popPose();
        if(rejected) gui.fill(x+1,y+1,x+CARD_WIDTH-1,y+CARD_HEIGHT-1,0x55808080);
    }
    @Override public boolean mouseClicked(double x,double y,int button) {
        if(button==0 && menu.phase()==AugmentationAnimation.CHOOSE && menu.getCarried().isEmpty()) {
            for(int i=0;i<2;i++) if(menu.offer(i)!=null && overCard(i,x-leftPos,y-topPos)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,i); return true;
            }
        }
        return super.mouseClicked(x,y,button);
    }
    @Override public void render(GuiGraphics gui,int mouseX,int mouseY,float delta) {
        super.render(gui,mouseX,mouseY,delta); renderTooltip(gui,mouseX,mouseY);
        if(menu.phase()>=AugmentationAnimation.CHOOSE && menu.getCarried().isEmpty()) {
            for(int i=0;i<2;i++) {
                var offer=menu.offer(i);
                if(offer!=null && overCard(i,mouseX-leftPos,mouseY-topPos)) {
                    Component name=offer.name().copy().append(" "+WeaponTooltips.romanNumeral(menu.offerRank(i)+1));
                    var lines=new java.util.ArrayList<FormattedCharSequence>();
                    lines.add(name.getVisualOrderText());
                    lines.addAll(font.split(Component.translatable(offer.id()).withStyle(ChatFormatting.GRAY),180));
                    if(menu.phase()==AugmentationAnimation.COMPLETE) lines.add(Component.translatable(menu.selected()==i?
                            "augmentation.forgermod.card.selected":"augmentation.forgermod.card.rejected").withStyle(ChatFormatting.GRAY).getVisualOrderText());
                    gui.renderTooltip(font,lines,mouseX,mouseY);
                }
            }
        }
        if(hoveredSlot!=null && hoveredSlot.index<4 && !hoveredSlot.hasItem()) {
            String key=hoveredSlot.index==0?"gear":hoveredSlot.index==3?"output":"ingredient";
            gui.renderTooltip(font,Component.translatable("augmentation.forgermod.slot."+key),mouseX,mouseY);
        }
    }
}
