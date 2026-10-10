package net.bananashelp20.forgermod.screen.custom;
import net.bananashelp20.forgermod.screen.ModMenuTypes;
import net.bananashelp20.forgermod.item.custom.abilities.GraveVaultEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class GraveVaultMenu extends AbstractContainerMenu {
 private final Player owner; private final ItemStack weapon; private final int selected;
 public final SimpleContainer storage;
 public GraveVaultMenu(int id,Inventory inventory,RegistryFriendlyByteBuf extra) { this(id,inventory,extra.readVarInt()); }
 public GraveVaultMenu(int id,Inventory inventory,int selected) {
  super(ModMenuTypes.GRAVE_VAULT_MENU.get(),id); owner=inventory.player; this.selected=selected;
  weapon=selected>=0 && selected<9?inventory.getItem(selected):ItemStack.EMPTY;
  storage=GraveVaultEvents.read(weapon,owner.registryAccess());
  storage.addListener(ignored->{ if(!owner.level().isClientSide && stillValid(owner)) GraveVaultEvents.write(weapon,storage,owner.registryAccess()); });
  for(int i=0;i<9;i++) addSlot(new Slot(storage,i,8+i*18,25) { @Override public boolean mayPlace(ItemStack stack) { return GraveVaultEvents.flower(stack); } });
  for(int row=0;row<3;row++) for(int col=0;col<9;col++) addSlot(new Slot(inventory,9+row*9+col,8+col*18,56+row*18));
  for(int col=0;col<9;col++) { final int slot=col; addSlot(new Slot(inventory,col,8+col*18,114) {
   @Override public boolean mayPickup(Player player) { return slot!=GraveVaultMenu.this.selected; }
   @Override public boolean mayPlace(ItemStack stack) { return slot!=GraveVaultMenu.this.selected; }
  }); }
 }
 @Override public boolean stillValid(Player player) { return player==owner && player.isAlive() && !player.isSpectator() && player.getInventory().selected==selected && (player.level().isClientSide?player.getMainHandItem().is(weapon.getItem()):player.getMainHandItem()==weapon) && GraveVaultEvents.learned(player.getMainHandItem()); }
 @Override public void clicked(int slot,int button,ClickType type,Player player) {
  if(!stillValid(player) || type==ClickType.SWAP && (button==selected || button==40)) return;
  super.clicked(slot,button,type,player);
 }
 @Override public ItemStack quickMoveStack(Player player,int index) {
  if(!stillValid(player) || index<0 || index>=slots.size()) return ItemStack.EMPTY;
  var slot=slots.get(index); if(!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
  var stack=slot.getItem(); var before=stack.copy();
  if(index<9) { if(!moveItemStackTo(stack,9,45,true)) return ItemStack.EMPTY; }
  else if(!GraveVaultEvents.flower(stack) || !moveItemStackTo(stack,0,9,false)) return ItemStack.EMPTY;
  if(stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged(); slot.onTake(player,stack); return before;
 }
}
