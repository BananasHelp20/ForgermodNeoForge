package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class RingingSteelEvents {
    private record Ring(ItemStack stack,int slot,ServerLevel level) {}
    private static final Map<UUID,Ring> RINGS=new HashMap<>();
    private RingingSteelEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player))return false;
        RINGS.put(player.getUUID(),new Ring(stack,player.getInventory().selected,player.serverLevel()));return true;
    }
    public static boolean active(ServerPlayer player) {
        var ring=RINGS.get(player.getUUID());if(ring==null)return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=ring.stack() || player.getInventory().selected!=ring.slot()
                || player.serverLevel()!=ring.level()
                || !Augmentations.ids(ring.stack(),true).contains(AdditionalAbilities.RINGING_STEEL)) {cancel(player.getUUID());return false;}
        return true;
    }
    public static void cancel(UUID player) {RINGS.remove(player);}
    private static boolean weapon(ItemStack stack) {
        var item=stack.getItem();return stack.isDamageableItem() && (item instanceof SwordItem || item instanceof AxeItem
                || item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem || item instanceof MaceItem);
    }
    @SubscribeEvent public static void landed(LivingDamageEvent.Post event) {
        if(!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !active(player) || !WeaponAbilityTargets.hostileTo(player,event.getEntity())
                || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0)return;
        cancel(player.getUUID());
        for(var slot:List.of(EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND)) {
            var held=event.getEntity().getItemBySlot(slot);if(!weapon(held))continue;
            held.hurtAndBreak(15,event.getEntity(),slot);break;
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {if(event.getEntity() instanceof ServerPlayer player)active(player);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
