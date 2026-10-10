package net.bananashelp20.forgermod.screen.custom;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class GraveVaultScreen extends AbstractContainerScreen<GraveVaultMenu> {
 public GraveVaultScreen(GraveVaultMenu menu,Inventory inventory,Component title) { super(menu,inventory,title); imageHeight=138; inventoryLabelY=44; }
 @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY) {
  graphics.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xffc6c6c6);
  graphics.fill(leftPos+2,topPos+2,leftPos+imageWidth-2,topPos+imageHeight-2,0xffd7dccd);
  for(var slot:menu.slots) { int x=leftPos+slot.x,y=topPos+slot.y; graphics.fill(x-1,y-1,x+17,y+17,0xff53594e); graphics.fill(x,y,x+16,y+16,0xff9ba38e); }
 }
 @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial) { super.render(graphics,mouseX,mouseY,partial); renderTooltip(graphics,mouseX,mouseY); }
}
