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
        super(menu,inventory,title); imageWidth=272; imageHeight=224; inventoryLabelX=52; inventoryLabelY=128;
    }
    @Override protected void init() {
        super.init();
        for (int i=0;i<2;i++) {
            final int button=i;
            choices[i]=addRenderableWidget(Button.builder(Component.empty(),ignored ->
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId,button))
                    .bounds(leftPos+8+i*132,topPos+100,124,20).build());
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
        gui.fill(x,y,x+imageWidth,y+imageHeight,0xff171e2d);
        gui.fill(x+2,y+2,x+imageWidth-2,y+imageHeight-2,0xff29334a);
        for (var slot:menu.slots) {
            gui.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xff0d1423);
            gui.fill(x+slot.x,y+slot.y,x+slot.x+16,y+slot.y+16,0xff4a5870);
        }
        String[] names={"gear","template","sapphire","output"};
        int[] positions={36,96,156,232};
        for(int i=0;i<4;i++) gui.drawCenteredString(font,Component.translatable("augmentation.forgermod.slot."+names[i]),x+positions[i],y+30,0xffccd8ec);
        if(menu.phase()==1) {
            gui.fill(x+16,y+84,x+256,y+92,0xff101725);
            int width=240*menu.progress()/Math.max(1,menu.duration());
            gui.fill(x+16,y+84,x+16+width,y+92,0xff327be0);
            int glow=(int)(System.currentTimeMillis()/35%240);
            if(glow<width) gui.fill(x+16+glow,y+84,x+18+glow,y+92,0xffa2dfff);
            gui.drawCenteredString(font,Component.translatable("augmentation.forgermod.cooking",
                    String.format(java.util.Locale.ROOT,"%.1f",Math.max(0,menu.duration()-menu.progress())/20.0)),x+136,y+70,0xffc8deff);
        } else {
            String status=menu.phase()==2?"choose":!menu.getSlot(3).getItem().isEmpty()?"take":
                    Augmentations.eligible(menu.getSlot(0).getItem()) && Augmentations.available(menu.getSlot(0).getItem()).isEmpty()?"complete":"insert";
            gui.drawCenteredString(font,Component.translatable("augmentation.forgermod."+status),x+136,y+72,0xffc8deff);
        }
    }
    @Override public void render(GuiGraphics gui,int mouseX,int mouseY,float delta) {
        refresh(); super.render(gui,mouseX,mouseY,delta); renderTooltip(gui,mouseX,mouseY);
    }
}
