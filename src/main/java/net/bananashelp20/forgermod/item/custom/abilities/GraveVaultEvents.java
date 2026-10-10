package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.screen.custom.GraveVaultMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class GraveVaultEvents {
 private static final String STORAGE="forgermod_grave_vault";
 private GraveVaultEvents() {}
 public static boolean learned(ItemStack stack) { return Augmentations.level(stack,RecommendedAbilities.GRAVE_VAULT)>0; }
 public static boolean flower(ItemStack stack) { return stack.getItem() instanceof BlockItem item && item.getBlock().defaultBlockState().is(BlockTags.FLOWERS); }
 public static SimpleContainer read(ItemStack weapon,net.minecraft.core.HolderLookup.Provider registry) {
  var container=new SimpleContainer(9); var tag=weapon.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
  var slots=tag.getList(STORAGE,Tag.TAG_COMPOUND);
  for(int i=0;i<Math.min(9,slots.size());i++) { var entry=slots.getCompound(i); int slot=entry.getInt("Slot");
   if(slot>=0 && slot<9) { var flower=ItemStack.parse(registry,entry.getCompound("Item")).orElse(ItemStack.EMPTY); if(flower(flower)) { flower.setCount(Math.min(flower.getCount(),flower.getMaxStackSize())); container.setItem(slot,flower); } }
  }
  return container;
 }
 public static void write(ItemStack weapon,Container container,net.minecraft.core.HolderLookup.Provider registry) {
  var list=new ListTag(); for(int i=0;i<9;i++) { var stack=container.getItem(i); if(stack.isEmpty() || !flower(stack)) continue;
   var entry=new CompoundTag(); entry.putInt("Slot",i); entry.put("Item",stack.save(registry)); list.add(entry);
  }
  CustomData.update(DataComponents.CUSTOM_DATA,weapon,tag->{ if(list.isEmpty()) tag.remove(STORAGE); else tag.put(STORAGE,list); });
 }
 private static void open(ServerPlayer player) {
  int selected=player.getInventory().selected;
  player.openMenu(new SimpleMenuProvider((id,inventory,ignored)->new GraveVaultMenu(id,inventory,selected),Component.translatable("menu.forgermod.grave_vault")),buf->buf.writeVarInt(selected));
 }
 @SubscribeEvent public static void onAir(PlayerInteractEvent.RightClickItem event) {
  if(event.getHand()!=InteractionHand.MAIN_HAND || !event.getEntity().isShiftKeyDown() || !learned(event.getItemStack())) return;
  if(event.getEntity() instanceof ServerPlayer player) open(player);
  event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
 }
 @SubscribeEvent public static void onBlock(PlayerInteractEvent.RightClickBlock event) {
  if(event.getHand()!=InteractionHand.MAIN_HAND || !learned(event.getItemStack())) return;
  if(event.getEntity().isShiftKeyDown()) {
   if(event.getEntity() instanceof ServerPlayer player) open(player);
   event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS); return;
  }
  if(!(event.getEntity() instanceof ServerPlayer player)) return;
  var weapon=player.getMainHandItem(); var storage=read(weapon,player.registryAccess());
  for(int i=0;i<9;i++) { var stack=storage.getItem(i); if(stack.isEmpty()) continue;
   // UseOnContext uses the held weapon by default; override only the placement item.
   var context=new UseOnContext(player,event.getHand(),event.getHitVec()) { @Override public ItemStack getItemInHand() { return stack; } };
   var placement=new net.minecraft.world.item.context.BlockPlaceContext(context);
   if(!player.level().mayInteract(player,placement.getClickedPos()) || !player.mayUseItemAt(placement.getClickedPos(),placement.getClickedFace(),stack)) continue;
   var result=stack.useOn(context);
   if(result.consumesAction()) { write(weapon,storage,player.registryAccess()); event.setCanceled(true); event.setCancellationResult(result); return; }
  }
 }
}
