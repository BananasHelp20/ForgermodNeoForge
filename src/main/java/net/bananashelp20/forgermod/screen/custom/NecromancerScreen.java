package net.bananashelp20.forgermod.screen.custom;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.abilities.NecromancerCatalog;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Two pages of the same server menu: selected equipment/roster, then an explicit add picker. */
public final class NecromancerScreen extends AbstractContainerScreen<NecromancerMenu> {
    private static final ResourceLocation BACKGROUND=ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"textures/gui/necromancer-selection/necromancer_gui.png");
    private static final int ROWS=3,PICKER_HEIGHT=196;
    private int pickerLeft,pickerTop,pickerWidth;
    private static final class RemoveButton extends Button {
        private RemoveButton(Builder builder){super(builder);}
        @Override public void renderString(GuiGraphics graphics,Font font,int color){
            int x=getX()+(getWidth()-5)/2,y=getY()+(getHeight()-1)/2;
            graphics.fill(x,y,x+5,y+1,color);
        }
    }
    private final List<Button> roster=new ArrayList<>(),remove=new ArrayList<>(),choices=new ArrayList<>();
    private Button add,back;private int scroll,lastSelected=-1;private boolean dragging,lastPicking;
    public NecromancerScreen(NecromancerMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=176;imageHeight=166;}
    private static Component text(String key,Object... args){return Component.translatable("gui.forgermod.necromancer."+key,args);}
    private void action(int id){if(minecraft!=null && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override protected void init(){
        super.init();roster.clear();remove.clear();choices.clear();dragging=false;
        int longest=NecromancerCatalog.CHOICES.stream().mapToInt(choice->font.width(choice.type().getDescription())).max().orElse(0);
        pickerWidth=Math.min(Math.max(176,width-16),Math.max(280,2*longest+48));
        pickerLeft=(width-pickerWidth)/2;pickerTop=(height-PICKER_HEIGHT)/2;
        int choiceWidth=(pickerWidth-24)/2;
        for(int row=0;row<ROWS;row++){
            final int visible=row;
            roster.add(addRenderableWidget(Button.builder(Component.empty(),button->action(10+scroll+visible)).bounds(leftPos+83,topPos+9+18*row,61,17).build()));
            remove.add(addRenderableWidget(Button.builder(text("remove"),button->action(300+scroll+visible)).bounds(leftPos+145,topPos+9+18*row,13,17).tooltip(Tooltip.create(text("remove"))).build(RemoveButton::new)));
        }
        add=addRenderableWidget(Button.builder(text("add"),button->action(1)).bounds(leftPos+83,topPos+66,83,12).build());
        for(int i=0;i<NecromancerCatalog.CHOICES.size();i++){
            final int type=i;var choice=NecromancerCatalog.CHOICES.get(i);
            choices.add(addRenderableWidget(Button.builder(choice.type().getDescription(),button->action(200+type))
                    .bounds(pickerLeft+8+(i%2)*(choiceWidth+8),pickerTop+25+(i/2)*21,choiceWidth,20)
                    .tooltip(Tooltip.create(text("choice",choice.type().getDescription(),choice.cost()))).build()));
        }
        back=addRenderableWidget(Button.builder(Component.translatable("gui.back"),button->action(3)).bounds(pickerLeft+8,pickerTop+170,pickerWidth-16,20).build());refresh();
    }
    private int maxScroll(){return Math.max(0,menu.rosterSize()-ROWS);}
    private void refresh(){
        if(menu.selected()!=lastSelected){lastSelected=menu.selected();if(lastSelected<scroll)scroll=lastSelected;if(lastSelected>=scroll+ROWS)scroll=lastSelected-ROWS+1;}
        scroll=Math.clamp(scroll,0,maxScroll());boolean picking=menu.picking();
        for(int row=0;row<ROWS;row++){
            int index=scroll+row,type=menu.type(index);var button=roster.get(row);button.visible=!picking && type>=0;button.active=button.visible;
            remove.get(row).visible=button.visible;
            if(type>=0){var name=NecromancerCatalog.CHOICES.get(type).type().getDescription();button.setMessage(name);button.setTooltip(Tooltip.create(name));}
        }
        add.visible=!picking;add.active=menu.used()<menu.capacity() && menu.rosterSize()<NecromancerCatalog.MAX_ENTRIES && menu.getCarried().isEmpty();
        add.setTooltip(Tooltip.create(text("capacity",menu.used(),menu.capacity())));
        for(int i=0;i<choices.size();i++){choices.get(i).visible=picking;choices.get(i).active=menu.used()+NecromancerCatalog.CHOICES.get(i).cost()<=menu.capacity() && menu.rosterSize()<NecromancerCatalog.MAX_ENTRIES;}
        back.visible=picking;
        if(lastPicking!=picking){setFocused(null);dragging=false;lastPicking=picking;}
    }
    @Override protected void containerTick(){super.containerTick();refresh();}
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){}
    private static void panel(GuiGraphics graphics,int x,int y,int width,int height){
        if(width<=8){
            graphics.fill(x,y,x+width,y+height,0xFF555555);graphics.fill(x,y,x+width-1,y+height-1,0xFFFFFFFF);
            graphics.fill(x+1,y+1,x+width-1,y+height-1,0xFFC6C6C6);return;
        }
        // Vanilla inventory gray, white top/left bevel, dark bottom/right bevel.
        graphics.fill(x,y,x+width,y+height,0xFF373737);graphics.fill(x+1,y+1,x+width-1,y+height-1,0xFFFFFFFF);
        graphics.fill(x+3,y+3,x+width-1,y+height-1,0xFF555555);graphics.fill(x+3,y+3,x+width-3,y+height-3,0xFFC6C6C6);
    }
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){
        if(menu.picking()){
            panel(graphics,pickerLeft,pickerTop,pickerWidth,PICKER_HEIGHT);
            graphics.drawString(font,text("choose"),pickerLeft+(pickerWidth-font.width(text("choose")))/2,pickerTop+8,0x404040,false);
            graphics.drawString(font,text("capacity",menu.used(),menu.capacity()),pickerLeft+8,pickerTop+155,0x404040,false);
        }else{
            graphics.blit(BACKGROUND,leftPos,topPos,0,0,imageWidth,imageHeight,256,256);
            graphics.fill(leftPos+160,topPos+9,leftPos+166,topPos+63,0xFF373737);
            int thumb=topPos+9+(maxScroll()==0?0:42*scroll/maxScroll());
            panel(graphics,leftPos+160,thumb,6,12);
            if(menu.rosterSize()==0)graphics.drawWordWrap(font,text("empty"),leftPos+85,topPos+18,71,0xFFFFFFFF);
        }
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){
        refresh();super.render(graphics,mouseX,mouseY,partial);
        if(!menu.picking()){
            int visible=menu.selected()-scroll;
            if(visible>=0 && visible<ROWS && menu.type(menu.selected())>=0)graphics.renderOutline(leftPos+83,topPos+9+visible*18,61,17,0xFFFFFFFF);
            renderTooltip(graphics,mouseX,mouseY);
            for(int i=0;i<NecromancerCatalog.EQUIPMENT_SLOTS;i++){
                var slot=menu.getSlot(i);
                if(!slot.hasItem() && menu.getCarried().isEmpty() && isHovering(slot.x,slot.y,16,16,mouseX,mouseY))
                    graphics.renderTooltip(font,!menu.equipmentUnlocked()?text("locked"):menu.rosterSize()==0?text("select"):text("slot."+i),mouseX,mouseY);
            }
        }
    }
    @Override protected boolean hasClickedOutside(double x,double y,int left,int top,int button){
        if(menu.picking())return x<pickerLeft || y<pickerTop || x>=pickerLeft+pickerWidth || y>=pickerTop+PICKER_HEIGHT;
        return super.hasClickedOutside(x,y,left,top,button);
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(!menu.picking() && x>=leftPos+81 && x<leftPos+168 && y>=topPos+7 && y<topPos+65){scroll=Math.clamp(scroll-(int)Math.signum(vertical),0,maxScroll());refresh();return true;}
        return super.mouseScrolled(x,y,horizontal,vertical);
    }
    private void drag(double y){if(maxScroll()>0){scroll=Math.clamp((int)Math.round((y-(topPos+9)-6)/42*maxScroll()),0,maxScroll());refresh();}}
    @Override public boolean mouseClicked(double x,double y,int button){
        if(button==0 && !menu.picking() && maxScroll()>0 && x>=leftPos+160 && x<leftPos+166 && y>=topPos+9 && y<topPos+63){dragging=true;drag(y);return true;}
        return super.mouseClicked(x,y,button);
    }
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){if(dragging && button==0){drag(y);return true;}return super.mouseDragged(x,y,button,dx,dy);}
    @Override public boolean mouseReleased(double x,double y,int button){if(button==0 && dragging){dragging=false;return true;}return super.mouseReleased(x,y,button);}
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(menu.picking() && key==GLFW.GLFW_KEY_ESCAPE){action(3);return true;}return super.keyPressed(key,scan,modifiers);}
}
