package net.bananashelp20.forgermod.screen.custom;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class GraveVaultScreen extends AbstractContainerScreen<GraveVaultMenu> {
 public GraveVaultScreen(GraveVaultMenu menu,Inventory inventory,Component title) { super(menu,inventory,title); imageHeight=132; inventoryLabelY=38; }
 @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY) {
  var texture=net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
  graphics.blit(texture,leftPos,topPos,0,0,imageWidth,35);
  graphics.blit(texture,leftPos,topPos+35,0,126,imageWidth,96);
 }
 @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial) { super.render(graphics,mouseX,mouseY,partial); renderTooltip(graphics,mouseX,mouseY); }
}
