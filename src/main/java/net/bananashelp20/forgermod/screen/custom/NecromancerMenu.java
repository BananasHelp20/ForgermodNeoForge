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

/** All roster and real-item edits are server validated; only the selected reinforcement equipment slots are exposed. */
public final class NecromancerMenu extends AbstractContainerMenu {
    public final Player owner;private final int hotbar;private final ItemStack weapon;
    private final NecromancerLoadouts.Loadout loadout;private int selected;
    private boolean picking;
    private final int[] remote=new int[15];private final SimpleContainer clientEquipment=new SimpleContainer(NecromancerCatalog.EQUIPMENT_SLOTS);
    private final Container equipment=new View();
    public NecromancerMenu(int id,Inventory inventory,RegistryFriendlyByteBuf extra){this(id,inventory,extra.readVarInt());}
    public NecromancerMenu(int id,Inventory inventory,int hotbar){
        super(ModMenuTypes.NECROMANCER_MENU.get(),id);owner=inventory.player;this.hotbar=hotbar;weapon=hotbar>=0 && hotbar<9?inventory.getItem(hotbar):ItemStack.EMPTY;
        loadout=owner instanceof ServerPlayer player?NecromancerLoadouts.get(player,weapon):null;Arrays.fill(remote,-1);remote[10]=0;
        for(int index=0;index<15;index++){final int key=index;addDataSlot(new DataSlot(){@Override public int get(){return value(key);}@Override public void set(int value){remote[key]=value;}});}
        int[][] positions={{9,26},{33,8},{33,26},{33,44},{33,62},{57,26}};
        for(int i=0;i<NecromancerCatalog.EQUIPMENT_SLOTS;i++){final int column=i;addSlot(new Slot(equipment,i,positions[i][0],positions[i][1]){
            @Override public boolean isActive(){return !picking() && canEquip();}
            @Override public boolean mayPlace(ItemStack stack){return !picking() && canEquip() && stillValid(owner) && NecromancerEquipment.validItem(column,stack);}
            @Override public boolean mayPickup(Player player){return !picking() && canEquip() && stillValid(player);}
            @Override public int getMaxStackSize(){return 1;}
            @Override public com.mojang.datafixers.util.Pair<net.minecraft.resources.ResourceLocation,net.minecraft.resources.ResourceLocation> getNoItemIcon(){
                var icon=switch(column){case 1->InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;case 2->InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
                    case 3->InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;case 4->InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;case 5->InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;default->null;};
                return icon==null?null:com.mojang.datafixers.util.Pair.of(InventoryMenu.BLOCK_ATLAS,icon);
            }
        });}
        for(int row=0;row<3;row++)for(int column=0;column<9;column++)addSlot(new Slot(inventory,9+row*9+column,8+column*18,84+row*18){
            @Override public boolean isActive(){return !picking();}
        });
        for(int column=0;column<9;column++){final int inventorySlot=column;addSlot(new Slot(inventory,column,8+column*18,142){
            @Override public boolean isActive(){return !picking();}
            @Override public boolean mayPickup(Player player){return inventorySlot!=NecromancerMenu.this.hotbar;}
            @Override public boolean mayPlace(ItemStack stack){return inventorySlot!=NecromancerMenu.this.hotbar;}
        });}
    }
    private int value(int key){
        if(loadout==null)return remote[key];if(key<10)return key<loadout.types.size()?loadout.types.get(key):-1;
        return switch(key){case 10->selected;case 11->NecromancerCatalog.capacity(Augmentations.level(weapon,AdditionalAbilities.BONE_GUARD));case 12->Augmentations.level(weapon,AdditionalAbilities.BONE_GUARD)>=5?1:0;case 13->loadout.used();case 14->picking?1:0;default->0;};
    }
    public int type(int index){return index>=0 && index<10?value(index):-1;}
    public int selected(){return value(10);}
    public boolean picking(){return value(14)==1;}
    public int rosterSize(){int size=0;while(size<NecromancerCatalog.MAX_ENTRIES && type(size)>=0)size++;return size;}
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
        if(picking && button!=3 && !(button>=200 && button<200+NecromancerCatalog.CHOICES.size()))return false;
        if(button>=10 && button<20){if(picking || type(button-10)<0)return false;selected=button-10;}
        else if(button==1){if(used()>=capacity() || loadout.types.size()>=NecromancerCatalog.MAX_ENTRIES || !getCarried().isEmpty())return false;picking=true;}
        else if(button==3){picking=false;}
        else if(button>=200 && button<200+NecromancerCatalog.CHOICES.size()){
            if(!picking || !loadout.add(button-200,capacity()))return false;selected=loadout.types.size()-1;picking=false;
        }
        else if(button>=300 && button<310){
            if(picking || button-300>=loadout.types.size())return false;int row=button-300;loadout.remove(row,server);
            if(row<selected)selected--;selected=Math.max(0,Math.min(selected,loadout.types.size()-1));
        }
        else if(button==2){if(selected>=loadout.types.size())return false;loadout.remove(selected,server);selected=Math.max(0,Math.min(selected,loadout.types.size()-1));}
        else if(button>=100 && button<100+NecromancerCatalog.CHOICES.size()){
            int type=button-100;
            if(loadout.types.isEmpty()){if(!loadout.add(type,capacity()))return false;selected=0;}
            else if(!loadout.replace(selected,type,capacity(),server))return false;
        }else return false;
        loadout.publish(weapon);sendAllDataToRemote();return true;
    }
    @Override public void clicked(int slot,int button,ClickType type,Player player){if(!stillValid(player) || picking || type==ClickType.SWAP && (button==hotbar || button==40))return;super.clicked(slot,button,type,player);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player) || picking || index<0 || index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem() || !slot.mayPickup(player))return ItemStack.EMPTY;var stack=slot.getItem();var before=stack.copy();
        if(index<NecromancerCatalog.EQUIPMENT_SLOTS){if(!moveItemStackTo(stack,NecromancerCatalog.EQUIPMENT_SLOTS,slots.size(),true))return ItemStack.EMPTY;}
        else {boolean moved=false;int[] order=stack.getItem() instanceof net.minecraft.world.item.ShieldItem || stack.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING)?new int[]{5,1,2,3,4,0}:new int[]{1,2,3,4,0,5};for(int gear:order)if(slots.get(gear).mayPlace(stack) && moveItemStackTo(stack,gear,gear+1,false)){moved=true;break;}if(!moved)return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
    private final class View implements Container {
        @Override public int getContainerSize(){return NecromancerCatalog.EQUIPMENT_SLOTS;}
        @Override public int getMaxStackSize(){return 1;}
        @Override public boolean isEmpty(){for(int i=0;i<NecromancerCatalog.EQUIPMENT_SLOTS;i++)if(!getItem(i).isEmpty())return false;return true;}
        @Override public ItemStack getItem(int slot){return loadout==null?clientEquipment.getItem(slot):selected<loadout.types.size()?loadout.gear.getItem(selected*NecromancerCatalog.EQUIPMENT_SLOTS+slot):ItemStack.EMPTY;}
        @Override public ItemStack removeItem(int slot,int amount){var result=getItem(slot).split(amount);setChanged();return result;}
        @Override public ItemStack removeItemNoUpdate(int slot){var result=getItem(slot);setItem(slot,ItemStack.EMPTY);return result;}
        @Override public void setItem(int slot,ItemStack item){if(loadout==null)clientEquipment.setItem(slot,item);else if(selected<loadout.types.size())loadout.gear.setItem(selected*NecromancerCatalog.EQUIPMENT_SLOTS+slot,item);}
        @Override public void setChanged(){if(loadout!=null)loadout.gear.setChanged();}
        @Override public boolean stillValid(Player player){return NecromancerMenu.this.stillValid(player);}
        @Override public void clearContent(){for(int i=0;i<NecromancerCatalog.EQUIPMENT_SLOTS;i++)setItem(i,ItemStack.EMPTY);}
    }
}
