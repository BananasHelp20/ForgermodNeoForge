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
    private static final ResourceLocation CARD_TEMPLATE=texture("ability_cards/template.png");
    private static final ResourceLocation[] OVERLAYS={null,texture("augmentation_table_analyze_progress.png"),
            texture("augmentation_table_analyze_to_selection_transtion_1.png"),
            texture("augmentation_table_analyze_to_selection_transition_2.png"),texture("augmentation_table_selection_finalisation.png")};
    private static final int CARD_Y=13,CARD_WIDTH=43,CARD_HEIGHT=58;
    private static final int[] CARD_X={10,123};
    private static ResourceLocation texture(String name) { return ResourceLocation.fromNamespaceAndPath("forgermod",DIRECTORY+name); }
    public AugmentationTableScreen(AugmentationTableMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title); imageWidth=176; imageHeight=166; titleLabelY=4; inventoryLabelX=8; inventoryLabelY=73;
    }
    @Override protected void renderBg(GuiGraphics gui,float delta,int mouseX,int mouseY) {
        gui.blit(BASE,leftPos,topPos,0,0,imageWidth,imageHeight);
    }
    @Override protected void renderLabels(GuiGraphics gui,int mouseX,int mouseY) {
        if(menu.phase()<AugmentationAnimation.CHOOSE) super.renderLabels(gui,mouseX,mouseY);
        else {
            // Leave space above both cards for the left-aligned eyebrows.
            gui.drawString(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,4210752,false);
            gui.pose().pushPose(); gui.pose().translate(titleLabelX,titleLabelY,0); gui.pose().scale(.5F,.5F,1);
            gui.drawString(font,title,0,0,4210752,false); gui.pose().popPose();
        }
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
                    var reveal=AugmentationAnimation.inwardReveal(stage,pixels);
                    crop(gui,stage,0,reveal.leftWidth());
                    crop(gui,stage,reveal.rightStart(),reveal.rightWidth());
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
        gui.blit(CARD_TEMPLATE,x,y,0,0,0,CARD_WIDTH,CARD_HEIGHT,CARD_WIDTH,CARD_HEIGHT);
        if(hovered || completed && !rejected) {
            gui.fill(x,y,x+CARD_WIDTH,y+1,edge); gui.fill(x,y+CARD_HEIGHT-1,x+CARD_WIDTH,y+CARD_HEIGHT,edge);
            gui.fill(x,y,x+1,y+CARD_HEIGHT,edge); gui.fill(x+CARD_WIDTH-1,y,x+CARD_WIDTH,y+CARD_HEIGHT,edge);
        }
        Component eyebrow=Component.translatable(menu.offerRank(card)==0?"augmentation.forgermod.card.new":"augmentation.forgermod.card.upgrade");
        gui.pose().pushPose(); gui.pose().translate(x+2,y-4,0); gui.pose().scale(.5F,.5F,1);
        gui.drawString(font,eyebrow,0,0,0xff586672,false); gui.pose().popPose();
        // Fit and center the title in the template's upper white box, without a rank suffix.
        float scale=.5F;
        List<FormattedCharSequence> titleLines=font.split(offer.name(),(int)(39/scale));
        while(titleLines.size()*font.lineHeight*scale>9 && scale>.3F) {
            scale=Math.max(.3F,scale-.025F); titleLines=font.split(offer.name(),(int)(39/scale));
        }
        int maxLines=Math.max(1,(int)(9/(font.lineHeight*scale)));
        if(titleLines.size()>maxLines) titleLines=titleLines.subList(0,maxLines);
        gui.pose().pushPose();
        gui.pose().translate(x+CARD_WIDTH/2F,y+2+(9-titleLines.size()*font.lineHeight*scale)/2F,0);
        gui.pose().scale(scale,scale,1);
        for(int row=0;row<titleLines.size();row++) {
            var line=titleLines.get(row);
            gui.drawString(font,line,-font.width(line)/2,row*font.lineHeight,rejected?0xff666666:offer.active()?0xff244575:0xff006b7b,false);
        }
        gui.pose().popPose();
        // The lower white box contains a colored category followed by a compact description.
        gui.pose().pushPose(); gui.pose().translate(x+2,y+13,0); gui.pose().scale(.5F,.5F,1);
        Component category=Component.translatable(offer.active()?"augmentation.forgermod.card.active":"augmentation.forgermod.card.passive");
        gui.drawString(font,category,(78-font.width(category))/2,0,rejected?0xff666666:offer.active()?0xff2962d9:0xff00a9bd,false);
        Component description=Component.translatable(offer.id()+".card");
        if(!net.minecraft.client.resources.language.I18n.exists(offer.id()+".card")) description=Component.translatable(offer.id());
        var lines=font.split(description,78);
        for(int row=0;row<Math.min(lines.size(),7);row++) gui.drawString(font,lines.get(row),0,14+row*font.lineHeight,0xff929292,false);
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
                    Component name=offer.name();
                    var lines=new java.util.ArrayList<FormattedCharSequence>();
                    lines.add(name.getVisualOrderText());
                    lines.add(Component.translatable("augmentation.forgermod.card.rank",WeaponTooltips.romanNumeral(menu.offerRank(i)+1))
                            .withStyle(ChatFormatting.GRAY).getVisualOrderText());
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
