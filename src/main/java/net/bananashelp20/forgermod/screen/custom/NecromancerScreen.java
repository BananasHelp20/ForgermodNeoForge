package net.bananashelp20.forgermod.screen.custom;

import net.bananashelp20.forgermod.item.custom.abilities.NecromancerCatalog;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.*;

/** Code-drawn inventory layout, with ten roster rows and the real selected armor/weapon slots. */
public final class NecromancerScreen extends AbstractContainerScreen<NecromancerMenu> {
    private final List<Button> roster=new ArrayList<>(),choices=new ArrayList<>();private Button add,remove;
    public NecromancerScreen(NecromancerMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=304;imageHeight=240;inventoryLabelX=128;inventoryLabelY=147;}
    private void action(int id){if(minecraft!=null && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override protected void init(){
        super.init();roster.clear();choices.clear();
        for(int i=0;i<10;i++){final int index=i;roster.add(addRenderableWidget(Button.builder(Component.empty(),button->action(10+index)).bounds(leftPos+8,topPos+34+14*i,104,14).build()));}
        for(int i=0;i<NecromancerCatalog.CHOICES.size();i++){final int type=i;var choice=NecromancerCatalog.CHOICES.get(i);choices.add(addRenderableWidget(Button.builder(choice.type().getDescription(),button->action(100+type)).bounds(leftPos+120+(i%3)*60,topPos+34+(i/3)*18,58,18).build()));}
        add=addRenderableWidget(Button.builder(Component.literal("Add"),button->action(1)).bounds(leftPos+8,topPos+181,50,18).build());remove=addRenderableWidget(Button.builder(Component.literal("Remove"),button->action(2)).bounds(leftPos+62,topPos+181,50,18).build());refresh();
    }
    private void refresh(){
        for(int i=0;i<roster.size();i++){int type=menu.type(i);roster.get(i).active=type>=0;roster.get(i).setMessage(type<0?Component.literal("—"):Component.literal((i==menu.selected()?"> ":"")+(i+1)+". "+font.plainSubstrByWidth(NecromancerCatalog.CHOICES.get(type).type().getDescription().getString(),76)));}
        int old=menu.type(menu.selected());for(int i=0;i<choices.size();i++){var choice=NecromancerCatalog.CHOICES.get(i);choices.get(i).active=menu.used()-(old<0?0:NecromancerCatalog.CHOICES.get(old).cost())+choice.cost()<=menu.capacity();choices.get(i).setMessage(Component.literal(font.plainSubstrByWidth(choice.type().getDescription().getString(),40)+" "+choice.cost()));}
        add.active=menu.used()<menu.capacity();remove.active=old>=0;
    }
    @Override protected void containerTick(){super.containerTick();refresh();}
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){
        graphics.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF202024);graphics.fill(leftPos+2,topPos+2,leftPos+imageWidth-2,topPos+imageHeight-2,0xFFD0CED3);
        for(var slot:menu.slots){int x=leftPos+slot.x,y=topPos+slot.y;graphics.fill(x-1,y-1,x+17,y+17,0xFF66646A);graphics.fill(x,y,x+16,y+16,0xFF939097);}
        graphics.drawString(font,"Capacity: "+menu.used()+" / "+menu.capacity(),leftPos+8,topPos+20,0x333038,false);
        graphics.drawString(font,"Weapon / armor",leftPos+128,topPos+114,0x333038,false);
        if(!menu.equipmentUnlocked())graphics.drawString(font,"Equipment unlocks at II",leftPos+128,topPos+20,0x554060,false);
        graphics.drawWordWrap(font,Component.literal("Changes are allowed only with no living reinforcements."),leftPos+8,topPos+206,104,0x443C48);
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){super.render(graphics,mouseX,mouseY,partial);renderTooltip(graphics,mouseX,mouseY);for(int i=0;i<choices.size();i++)if(choices.get(i).isHovered())graphics.renderTooltip(font,Component.literal(NecromancerCatalog.CHOICES.get(i).type().getDescription().getString()+" — capacity "+NecromancerCatalog.CHOICES.get(i).cost()+(i==10?" (includes Skeleton rider)":"")),mouseX,mouseY);}
}
