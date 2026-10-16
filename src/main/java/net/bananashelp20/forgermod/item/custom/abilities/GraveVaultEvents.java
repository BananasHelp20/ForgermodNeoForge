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
 private static SimpleContainer storage(ServerPlayer player,ItemStack weapon) {
  return weapon==player.getMainHandItem() && player.containerMenu instanceof GraveVaultMenu menu && menu.stillValid(player)
    ? menu.storage : read(weapon,player.registryAccess());
 }
 /** The vault is finite even in Creative; vanilla placement otherwise skips stack consumption. */
 private static InteractionResult placeFlower(UseOnContext context) {
  var flower=context.getItemInHand();int beforeCount=flower.getCount();
  var block=((BlockItem)flower.getItem()).getBlock();
  var pos=new net.minecraft.world.item.context.BlockPlaceContext(context).getClickedPos();
  var before=context.getLevel().getBlockState(pos);
  var result=flower.useOn(context);
  var after=context.getLevel().getBlockState(pos);
  // Preserve placement hooks and cancelled/failed placements. Normalize rather than double-shrink Survival stacks.
  if(result.consumesAction() && after.is(block) && !after.equals(before)) flower.setCount(beforeCount-1);
  return result;
 }
 private static void save(ServerPlayer player,ItemStack weapon,SimpleContainer storage) {
  storage.setChanged();write(weapon,storage,player.registryAccess());
  player.getInventory().setChanged();player.containerMenu.broadcastChanges();
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
  var weapon=player.getMainHandItem(); var storage=storage(player,weapon);
  for(int i=0;i<9;i++) { var stack=storage.getItem(i); if(stack.isEmpty()) continue;
   // UseOnContext uses the held weapon by default; override only the placement item.
   var context=new UseOnContext(player,event.getHand(),event.getHitVec()) { @Override public ItemStack getItemInHand() { return stack; } };
   var placement=new net.minecraft.world.item.context.BlockPlaceContext(context);
   if(!player.level().mayInteract(player,placement.getClickedPos()) || !player.mayUseItemAt(placement.getClickedPos(),placement.getClickedFace(),stack)) continue;
   var result=placeFlower(context);
   if(result.consumesAction()) { save(player,weapon,storage); event.setCanceled(true); event.setCancellationResult(result); return; }
  }
 }
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
 public static void onKill(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
  if(event.isCanceled() || !(event.getSource().getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
    || event.getEntity().level()!=player.level()
    || !WeaponAbilityTargets.hostileTo(player,event.getEntity()))return;
  var weapon=player.getMainHandItem();var hand=InteractionHand.MAIN_HAND;
  if(!learned(weapon)) {
   if(!net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems.hasMatchingDaggers(player) || !learned(player.getOffhandItem()))return;
   weapon=player.getOffhandItem();hand=InteractionHand.OFF_HAND;
  }
  var storage=storage(player,weapon);
  var ground=event.getEntity().getOnPos();var destination=ground.above();
  var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(ground).add(0,.5,0),net.minecraft.core.Direction.UP,ground,false);
  for(int i=0;i<9;i++) {
   var flower=storage.getItem(i);if(flower.isEmpty())continue;
   var context=new UseOnContext(player,hand,hit) {@Override public ItemStack getItemInHand(){return flower;}};
   var placement=new net.minecraft.world.item.context.BlockPlaceContext(context);
   if(!placement.getClickedPos().equals(destination) || !player.level().mayInteract(player,destination)
     || !player.mayUseItemAt(destination,placement.getClickedFace(),flower))continue;
   if(placeFlower(context).consumesAction()) {save(player,weapon,storage);return;}
  }
 }

}
