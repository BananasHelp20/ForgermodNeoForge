package net.bananashelp20.forgermod.screen.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class WeaponAssemblerScreen extends AbstractContainerScreen<WeaponAssemblerMenu> {
    public WeaponAssemblerScreen(WeaponAssemblerMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);imageWidth=176;imageHeight=182;inventoryLabelY=89;
    }
    @Override protected void renderBg(GuiGraphics gui,float delta,int mouseX,int mouseY){
        gui.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xffc6c6c6);
        gui.fill(leftPos+1,topPos+1,leftPos+imageWidth-1,topPos+2,0xffffffff);
        for(var slot:menu.slots){
            gui.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff373737);
            gui.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+17,topPos+slot.y+17,0xff8b8b8b);
        }
    }
    @Override protected void renderLabels(GuiGraphics gui,int mouseX,int mouseY){
        super.renderLabels(gui,mouseX,mouseY);
        String id=menu.specialization();
        Component message=Component.translatable(id.isEmpty()?"crafting.forgermod.insert_template":id+".name");
        gui.drawWordWrap(font,message,8,57,160,0x404040);
        if(!id.isEmpty())gui.drawWordWrap(font,Component.translatable("crafting.forgermod.layout_pending"),8,71,160,0x404040);
    }
    @Override public void render(GuiGraphics gui,int mouseX,int mouseY,float delta){super.render(gui,mouseX,mouseY,delta);renderTooltip(gui,mouseX,mouseY);}
}
