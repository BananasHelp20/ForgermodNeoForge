package net.bananashelp20.forgermod.screen.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.custom.WeaponTooltips;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AugmentationTableScreen extends AbstractContainerScreen<AugmentationTableMenu> {
    private final Button[] choices=new Button[2];
    public AugmentationTableScreen(AugmentationTableMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title); imageWidth=176; imageHeight=218; inventoryLabelX=8; inventoryLabelY=122;
    }
    @Override protected void init() {
        super.init();
        for (int i=0;i<2;i++) {
            final int button=i;
            choices[i]=addRenderableWidget(Button.builder(Component.empty(),ignored ->
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId,button))
                    .bounds(leftPos+8,topPos+78+i*21,160,20).build());
        }
        refresh();
    }
    private void refresh() {
        for (int i=0;i<2;i++) {
            var offer=menu.offer(i); choices[i].visible=menu.phase()==2 && offer!=null;
            if (offer==null) continue;
            int level=Augmentations.level(menu.getSlot(0).getItem(),offer.id());
            Component label=Component.translatable(level==0?"augmentation.forgermod.get":"augmentation.forgermod.upgrade",
                    offer.name(),WeaponTooltips.romanNumeral(level+1));
            choices[i].setMessage(label); choices[i].setTooltip(Tooltip.create(label.copy().append("\n")
                    .append(Component.translatable(offer.id())).append("\n")
                    .append(Component.translatable(offer.active()?"augmentation.forgermod.active_hint":"augmentation.forgermod.passive_hint"))));
        }
    }
    @Override protected void renderBg(GuiGraphics gui,float delta,int mouseX,int mouseY) {
        int x=leftPos,y=topPos;
        // Vanilla-style raised panel and inset slots, matching the existing Forge/Infusion screens.
        gui.fill(x,y,x+imageWidth,y+imageHeight,0xff000000);
        gui.fill(x+1,y+1,x+imageWidth-1,y+imageHeight-1,0xff555555);
        gui.fill(x+1,y+1,x+imageWidth-2,y+2,0xffffffff);
        gui.fill(x+1,y+1,x+2,y+imageHeight-2,0xffffffff);
        gui.fill(x+3,y+3,x+imageWidth-3,y+imageHeight-3,0xffc6c6c6);
        for (var slot:menu.slots) {
            gui.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xffffffff);
            gui.fill(x+slot.x-1,y+slot.y-1,x+slot.x+16,y+slot.y+16,0xff373737);
            gui.fill(x+slot.x,y+slot.y,x+slot.x+16,y+slot.y+16,0xff8b8b8b);
        }
        gui.drawString(font,Component.literal(">"),x+118,y+39,0xff555555,false);
        if(menu.phase()==1) {
            gui.fill(x+8,y+66,x+168,y+72,0xff555555);
            int width=158*menu.progress()/Math.max(1,menu.duration());
            gui.fill(x+9,y+67,x+9+width,y+71,0xff58a7e8);
            int glow=(int)(System.currentTimeMillis()/35%158);
            if(glow<width) gui.fill(x+9+glow,y+67,x+11+glow,y+71,0xffc9eeff);
            gui.drawCenteredString(font,Component.translatable("augmentation.forgermod.cooking",
                    String.format(java.util.Locale.ROOT,"%.1f",Math.max(0,menu.duration()-menu.progress())/20.0)),x+88,y+85,0xff404040);
        } else {
            String status=menu.phase()==2?"choose":!menu.getSlot(3).getItem().isEmpty()?"take":
                    Augmentations.eligible(menu.getSlot(0).getItem()) && Augmentations.available(menu.getSlot(0).getItem()).isEmpty()?"complete":"insert";
            int lineY=y+62;
            for(var line:font.split(Component.translatable("augmentation.forgermod."+status),160)) {
                gui.drawString(font,line,x+8,lineY,0xff404040,false); lineY+=10;
            }
        }
    }
    @Override public void render(GuiGraphics gui,int mouseX,int mouseY,float delta) {
        refresh(); super.render(gui,mouseX,mouseY,delta); renderTooltip(gui,mouseX,mouseY);
        if (hoveredSlot != null && hoveredSlot.index < 4 && !hoveredSlot.hasItem()) {
            String[] names={"gear","template","sapphire","output"};
            gui.renderTooltip(font,Component.translatable("augmentation.forgermod.slot."+names[hoveredSlot.index]),mouseX,mouseY);
        }
    }
}
