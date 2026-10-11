package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class BloodSacrificeEvents {
    private record Sacrifice(ItemStack stack,int slot,ServerLevel level,long expires) {}
    private static final Map<UUID,Sacrifice> SACRIFICES=new HashMap<>();
    private BloodSacrificeEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player) || player.getHealth()<=0) return false;
        player.setHealth(player.getHealth()*.5F);player.setAbsorptionAmount(player.getAbsorptionAmount()*.5F);
        SACRIFICES.put(player.getUUID(),new Sacrifice(stack,player.getInventory().selected,player.serverLevel(),player.level().getGameTime()+400));return true;
    }
    public static boolean active(ServerPlayer player) {
        var state=SACRIFICES.get(player.getUUID());if(state==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=state.stack || player.getInventory().selected!=state.slot || player.serverLevel()!=state.level || player.level().getGameTime()>=state.expires || !Augmentations.ids(state.stack,true).contains(AdditionalAbilities.BLOOD_SACRAFICE)) {cancel(player.getUUID());return false;}return true;
    }
    public static void cancel(UUID owner) {SACRIFICES.remove(owner);}
    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent event) {
        if(!event.isCanceled() && event.getAmount()>0 && event.getSource().getEntity() instanceof ServerPlayer player && active(player) && event.getEntity()!=player && !event.getEntity().isAlliedTo(player)) event.setAmount(event.getAmount()*2);
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {if(event.getEntity() instanceof ServerPlayer player) active(player);}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
