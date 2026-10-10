package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ExplosionKnockbackEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class NullReceiptEvents {
    private static final class Receipt {
        final ItemStack stack; final int selected; final long expires; final net.minecraft.server.level.ServerLevel level;
        int remaining=5; long hitTick=Long.MIN_VALUE; boolean capture; double stored;
        Receipt(ServerPlayer player,ItemStack stack) { this.stack=stack; selected=player.getInventory().selected; level=player.serverLevel(); expires=player.level().getGameTime()+600; }
    }
    private static final Map<UUID,Receipt> RECEIPTS=new HashMap<>();
    private NullReceiptEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        RECEIPTS.put(player.getUUID(),new Receipt(player,stack)); return true;
    }
    public static boolean active(ServerPlayer player) {
        var receipt=RECEIPTS.get(player.getUUID()); if(receipt==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=receipt.stack
                || player.getInventory().selected!=receipt.selected || player.level()!=receipt.level || player.level().getGameTime()>=receipt.expires
                || !Augmentations.ids(receipt.stack,true).contains(RecommendedAbilities.NULL_RECEIPT)) {
            cancel(player.getUUID()); return false;
        }
        return true;
    }
    public static void cancel(UUID player) { RECEIPTS.remove(player); }
    public static double release(ServerPlayer player) {
        if(!active(player)) return 0;
        var receipt=RECEIPTS.remove(player.getUUID()); return receipt.stored;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void onIncoming(LivingIncomingDamageEvent event) {
        if(event.getEntity() instanceof ServerPlayer player && active(player)) RECEIPTS.get(player.getUUID()).capture=false;
    }
    @SubscribeEvent
    public static void onHit(LivingDamageEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;
        var receipt=RECEIPTS.get(player.getUUID()); receipt.capture=false;
        if(receipt.remaining<=0 || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0
                || event.getSource().getEntity()==null && !event.getSource().is(DamageTypeTags.IS_EXPLOSION)) return;
        receipt.remaining--; receipt.hitTick=player.level().getGameTime(); receipt.capture=true;
    }
    private static Receipt capturing(ServerPlayer player) {
        if(!active(player)) return null;
        var receipt=RECEIPTS.get(player.getUUID());
        return receipt.capture && receipt.hitTick==player.level().getGameTime()?receipt:null;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onKnockback(LivingKnockBackEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || event.isCanceled()) return;
        var receipt=capturing(player); if(receipt==null) return;
        double impulse=Math.max(0,event.getStrength()*(1-player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
        receipt.stored=Math.min(5,receipt.stored+impulse); event.setCanceled(true);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onExplosion(ExplosionKnockbackEvent event) {
        if(!(event.getAffectedEntity() instanceof ServerPlayer player)) return;
        var receipt=capturing(player); if(receipt==null) return;
        receipt.stored=Math.min(5,receipt.stored+event.getKnockbackVelocity().length()); event.setKnockbackVelocity(Vec3.ZERO);
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { cancel(event.getEntity().getUUID()); }
}
