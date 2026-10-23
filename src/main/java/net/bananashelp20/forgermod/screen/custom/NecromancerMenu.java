package net.bananashelp20.forgermod.screen.custom;

import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.bananashelp20.forgermod.screen.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;

/** All roster and real-item edits are server validated; only five selected equipment slots are exposed. */
public final class NecromancerMenu extends AbstractContainerMenu {
    public final Player owner;private final int hotbar;private final ItemStack weapon;
    private final NecromancerLoadouts.Loadout loadout;private int selected;
    private final int[] remote=new int[14];private final SimpleContainer clientEquipment=new SimpleContainer(5);
    private final Container equipment=new View();
    public NecromancerMenu(int id,Inventory inventory,RegistryFriendlyByteBuf extra){this(id,inventory,extra.readVarInt());}
    public NecromancerMenu(int id,Inventory inventory,int hotbar){
        super(ModMenuTypes.NECROMANCER_MENU.get(),id);owner=inventory.player;this.hotbar=hotbar;weapon=hotbar>=0 && hotbar<9?inventory.getItem(hotbar):ItemStack.EMPTY;
        loadout=owner instanceof ServerPlayer player?NecromancerLoadouts.get(player,weapon):null;Arrays.fill(remote,-1);remote[10]=0;
        for(int index=0;index<14;index++){final int key=index;addDataSlot(new DataSlot(){@Override public int get(){return value(key);}@Override public void set(int value){remote[key]=value;}});}
        for(int i=0;i<5;i++){final int column=i;addSlot(new Slot(equipment,i,i==0?128:142+18*i,126){
            @Override public boolean mayPlace(ItemStack stack){return canEquip() && stillValid(owner) && NecromancerEquipment.validItem(column,stack);}
            @Override public boolean mayPickup(Player player){return canEquip() && stillValid(player);}
            @Override public int getMaxStackSize(){return 1;}
        });}
        for(int row=0;row<3;row++)for(int column=0;column<9;column++)addSlot(new Slot(inventory,9+row*9+column,128+column*18,158+row*18));
        for(int column=0;column<9;column++){final int inventorySlot=column;addSlot(new Slot(inventory,column,128+column*18,216){
            @Override public boolean mayPickup(Player player){return inventorySlot!=NecromancerMenu.this.hotbar;}
            @Override public boolean mayPlace(ItemStack stack){return inventorySlot!=NecromancerMenu.this.hotbar;}
        });}
    }
    private int value(int key){
        if(loadout==null)return remote[key];if(key<10)return key<loadout.types.size()?loadout.types.get(key):-1;
        return switch(key){case 10->selected;case 11->NecromancerCatalog.capacity(Augmentations.level(weapon,AdditionalAbilities.BONE_GUARD));case 12->Augmentations.level(weapon,AdditionalAbilities.BONE_GUARD)>=5?1:0;case 13->loadout.used();default->0;};
    }
    public int type(int index){return index>=0 && index<10?value(index):-1;}
    public int selected(){return value(10);}
    public int capacity(){return value(11);}
    public int used(){return value(13);}
    public boolean equipmentUnlocked(){return value(12)==1;}
    public boolean canEquip(){return equipmentUnlocked() && type(selected())>=0;}
    public static boolean learned(ItemStack weapon){return weapon.getItem() instanceof MorsiumWeapon && Augmentations.level(weapon,AdditionalAbilities.BONE_GUARD)>=4;}
    @Override public boolean stillValid(Player player){
        if(player!=owner || !player.isAlive() || player.isSpectator() || player.getInventory().selected!=hotbar || !learned(player.getMainHandItem()))return false;
        if(player.level().isClientSide)return player.getMainHandItem().is(weapon.getItem());
        return player.getMainHandItem()==weapon && !BoneGuardEvents.active((ServerPlayer)player) && !BoneGuardEvents.busy(loadout.id);
    }
    @Override public boolean clickMenuButton(Player player,int button){
        if(!(player instanceof ServerPlayer server) || !stillValid(player))return false;
        if(button>=10 && button<20){if(type(button-10)<0)return false;selected=button-10;}
        else if(button==1){if(!loadout.add(0,capacity()))return false;selected=loadout.types.size()-1;}
        else if(button==2){if(selected>=loadout.types.size())return false;loadout.remove(selected,server);selected=Math.max(0,Math.min(selected,loadout.types.size()-1));}
        else if(button>=100 && button<100+NecromancerCatalog.CHOICES.size()){
            int type=button-100;
            if(loadout.types.isEmpty()){if(!loadout.add(type,capacity()))return false;selected=0;}
            else if(!loadout.replace(selected,type,capacity(),server))return false;
        }else return false;
        loadout.publish(weapon);sendAllDataToRemote();return true;
    }
    @Override public void clicked(int slot,int button,ClickType type,Player player){if(!stillValid(player) || type==ClickType.SWAP && (button==hotbar || button==40))return;super.clicked(slot,button,type,player);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player) || index<0 || index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem() || !slot.mayPickup(player))return ItemStack.EMPTY;var stack=slot.getItem();var before=stack.copy();
        if(index<5){if(!moveItemStackTo(stack,5,41,true))return ItemStack.EMPTY;}
        else {boolean moved=false;for(int gear=0;gear<5;gear++)if(slots.get(gear).mayPlace(stack) && moveItemStackTo(stack,gear,gear+1,false)){moved=true;break;}if(!moved)return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
    private final class View implements Container {
        @Override public int getContainerSize(){return 5;}
        @Override public int getMaxStackSize(){return 1;}
        @Override public boolean isEmpty(){for(int i=0;i<5;i++)if(!getItem(i).isEmpty())return false;return true;}
        @Override public ItemStack getItem(int slot){return loadout==null?clientEquipment.getItem(slot):selected<loadout.types.size()?loadout.gear.getItem(selected*5+slot):ItemStack.EMPTY;}
        @Override public ItemStack removeItem(int slot,int amount){var result=getItem(slot).split(amount);setChanged();return result;}
        @Override public ItemStack removeItemNoUpdate(int slot){var result=getItem(slot);setItem(slot,ItemStack.EMPTY);return result;}
        @Override public void setItem(int slot,ItemStack item){if(loadout==null)clientEquipment.setItem(slot,item);else if(selected<loadout.types.size())loadout.gear.setItem(selected*5+slot,item);}
        @Override public void setChanged(){if(loadout!=null)loadout.gear.setChanged();}
        @Override public boolean stillValid(Player player){return NecromancerMenu.this.stillValid(player);}
        @Override public void clearContent(){for(int i=0;i<5;i++)setItem(i,ItemStack.EMPTY);}
    }
}
