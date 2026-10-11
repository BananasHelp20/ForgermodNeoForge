package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class SoulHungerEvents {
    private static final ResourceLocation CAPACITY=ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"soul_hunger_absorption");
    private static final class Soul {
        final ItemStack stack;final int slot;final ServerLevel level;float remaining,potionFloor;
        Soul(ServerPlayer player) {stack=player.getMainHandItem();slot=player.getInventory().selected;level=player.serverLevel();}
    }
    private static final Map<UUID,Soul> SOULS=new HashMap<>();
    private SoulHungerEvents() {}
    private static void sync(ServerPlayer player,Soul soul) {
        soul.remaining=Math.min(soul.remaining,player.getAbsorptionAmount());
        // Vanilla potion refresh uses max(current, potion grant). Preserve our separate contribution.
        if(soul.potionFloor>0) {player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(),soul.remaining+soul.potionFloor));soul.potionFloor=0;}
    }
    public static void end(ServerPlayer player) {
        var soul=SOULS.remove(player.getUUID());if(soul==null) return;sync(player,soul);player.setAbsorptionAmount(Math.max(0,player.getAbsorptionAmount()-soul.remaining));player.getAttribute(Attributes.MAX_ABSORPTION).removeModifier(CAPACITY);
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public static void kill(LivingDeathEvent event) {
        if(event.isCanceled() || !(event.getSource().getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
            || !Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.SOUL_HUNGER) || !WeaponAbilityTargets.hostileTo(player,event.getEntity())) return;
        var soul=SOULS.get(player.getUUID());if(soul!=null && (soul.stack!=player.getMainHandItem() || soul.slot!=player.getInventory().selected || soul.level!=player.level())) {end(player);soul=null;}
        if(soul==null) {soul=new Soul(player);SOULS.put(player.getUUID(),soul);}
        var capacity=player.getAttribute(Attributes.MAX_ABSORPTION);if(!capacity.hasModifier(CAPACITY)) capacity.addTransientModifier(new AttributeModifier(CAPACITY,6,AttributeModifier.Operation.ADD_VALUE));sync(player,soul);
        float before=player.getAbsorptionAmount();player.setAbsorptionAmount(before+Math.min(2,6-soul.remaining));soul.remaining+=player.getAbsorptionAmount()-before;
    }
    @SubscribeEvent
    public static void absorbed(LivingDamageEvent.Post event) {var soul=SOULS.get(event.getEntity().getUUID());if(soul!=null) soul.remaining=Math.max(0,soul.remaining-event.getReduction(DamageContainer.Reduction.ABSORPTION));}
    @SubscribeEvent
    public static void potion(MobEffectEvent.Added event) {var soul=SOULS.get(event.getEntity().getUUID());if(soul!=null && event.getEffectInstance().getEffect().equals(MobEffects.ABSORPTION)) soul.potionFloor=Math.max(soul.potionFloor,4*(1+event.getEffectInstance().getAmplifier()));}
    @SubscribeEvent
    public static void removed(MobEffectEvent.Remove event) {var soul=SOULS.get(event.getEntity().getUUID());if(!event.isCanceled() && soul!=null && event.getEffect().equals(MobEffects.ABSORPTION)) soul.potionFloor=0;}
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;var soul=SOULS.get(player.getUUID());if(soul==null) return;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=soul.stack || player.getInventory().selected!=soul.slot || player.level()!=soul.level || !Augmentations.hasPassive(soul.stack,AdditionalAbilities.SOUL_HUNGER)) end(player);else sync(player,soul);
    }
    @SubscribeEvent
    public static void travel(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent event) {if(event.getEntity() instanceof ServerPlayer player) end(player);}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {if(event.getEntity() instanceof ServerPlayer player) end(player);}
}
