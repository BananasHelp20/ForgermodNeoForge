package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class LightningRodEvents {
    private static final ResourceLocation ABSORPTION=ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"lightning_rod_absorption");
    private static final class Grant {
        final ItemStack stack;final int selected;float remaining;long expires,lastStrike;
        MobEffectInstance owned,previous;
        Grant(ServerPlayer player) {stack=player.getMainHandItem();selected=player.getInventory().selected;previous=copy(player.getEffect(MobEffects.DAMAGE_BOOST));lastStrike=Long.MIN_VALUE/2;}
    }
    // Vanilla visual-only lightning still powers rods and cleans copper on its first tick.
    // Clients render the ordinary lightning entity type; the server only expires this cosmetic bolt.
    private static final class CosmeticBolt extends net.minecraft.world.entity.LightningBolt {
        CosmeticBolt(net.minecraft.server.level.ServerLevel level) {super(EntityType.LIGHTNING_BOLT,level);setVisualOnly(true);}
        @Override public void tick() {baseTick();if(tickCount>=20) discard();}
    }
    private static final Map<UUID,Grant> GRANTS=new HashMap<>();
    private static final Set<UUID> INTERNAL=new HashSet<>();
    private LightningRodEvents() {}
    private static MobEffectInstance copy(MobEffectInstance effect) {return effect==null?null:MobEffectInstance.load((net.minecraft.nbt.CompoundTag)effect.save());}
    private static boolean eligible(ServerPlayer player) {return player.isAlive() && !player.isSpectator() && Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.LIGHTNING_ROD);}
    private static void add(ServerPlayer player,MobEffectInstance effect) {INTERNAL.add(player.getUUID());try {player.addEffect(effect);}finally {INTERNAL.remove(player.getUUID());}}
    public static void end(ServerPlayer player) {
        var grant=GRANTS.remove(player.getUUID());if(grant==null) return;
        player.setAbsorptionAmount(Math.max(0,player.getAbsorptionAmount()-grant.remaining));player.getAttribute(Attributes.MAX_ABSORPTION).removeModifier(ABSORPTION);
        if(player.getEffect(MobEffects.DAMAGE_BOOST)==grant.owned) {
            player.removeEffect(MobEffects.DAMAGE_BOOST);if(player.isAlive() && grant.previous!=null) add(player,grant.previous);
        }
    }
    public static boolean grant(ServerPlayer player) {
        if(!eligible(player)) return false;var grant=GRANTS.get(player.getUUID());long now=player.level().getGameTime();
        if(grant!=null && (grant.stack!=player.getMainHandItem() || grant.selected!=player.getInventory().selected || now>=grant.expires)) {end(player);grant=null;}
        if(grant==null) {grant=new Grant(player);GRANTS.put(player.getUUID(),grant);}
        if(now-grant.lastStrike<20) return true;grant.lastStrike=now;grant.expires=now+JadeAugments.duration(grant.stack,AdditionalAbilities.LIGHTNING_ROD,600);
        var attribute=player.getAttribute(Attributes.MAX_ABSORPTION);
        if(!attribute.hasModifier(ABSORPTION)) attribute.addTransientModifier(new AttributeModifier(ABSORPTION,10,AttributeModifier.Operation.ADD_VALUE));
        grant.remaining=Math.min(grant.remaining,player.getAbsorptionAmount());player.setAbsorptionAmount(player.getAbsorptionAmount()+10-grant.remaining);grant.remaining=10;
        add(player,JadeAugments.reinforce(grant.stack,AdditionalAbilities.LIGHTNING_ROD,
                new MobEffectInstance(MobEffects.DAMAGE_BOOST,600,0)));grant.owned=player.getEffect(MobEffects.DAMAGE_BOOST);return true;
    }
    public static boolean extraStrike(ServerPlayer player) {
        if(!eligible(player) || !player.level().dimensionType().hasSkyLight() || !player.level().canSeeSky(player.blockPosition())
            || player.level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,player.blockPosition()).getY()>player.blockPosition().getY()) return false;
        return chargeStrike(player);
    }
    /** A fully charged weapon supplies its own lightning, including indoors and in skyless dimensions. */
    public static boolean chargeStrike(ServerPlayer player) {
        if(!eligible(player))return false;
        visualStrike(player,player.position());grant(player);return true;
    }
    public static net.minecraft.world.entity.LightningBolt visualStrike(ServerPlayer player,net.minecraft.world.phys.Vec3 position) {
        var bolt=new CosmeticBolt(player.serverLevel());bolt.setPos(position);bolt.setCause(player);
        return player.serverLevel().addFreshEntity(bolt)?bolt:null;
    }
    @SubscribeEvent
    public static void struck(EntityStruckByLightningEvent event) {
        if(event.getEntity() instanceof ServerPlayer player && eligible(player)) {event.setCanceled(true);grant(player);}
    }
    @SubscribeEvent
    public static void lightningDamage(LivingIncomingDamageEvent event) {
        if(event.getSource().is(DamageTypes.LIGHTNING_BOLT) && event.getEntity() instanceof ServerPlayer player && eligible(player)) {event.setCanceled(true);grant(player);}
    }
    @SubscribeEvent
    public static void absorbed(LivingDamageEvent.Post event) {
        var grant=GRANTS.get(event.getEntity().getUUID());if(grant!=null) grant.remaining=Math.max(0,grant.remaining-event.getReduction(DamageContainer.Reduction.ABSORPTION));
    }
    @SubscribeEvent
    public static void added(MobEffectEvent.Added event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || INTERNAL.contains(player.getUUID()) || !event.getEffectInstance().getEffect().equals(MobEffects.DAMAGE_BOOST)) return;
        var grant=GRANTS.get(player.getUUID());if(grant!=null) {var effect=copy(event.getEffectInstance());if(grant.previous==null) grant.previous=effect;else grant.previous.update(effect);}
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        var grant=GRANTS.get(player.getUUID());
        if(grant!=null) {
            if(!eligible(player) || player.getMainHandItem()!=grant.stack || player.getInventory().selected!=grant.selected || player.level().getGameTime()>=grant.expires) end(player);
            else {
                grant.remaining=Math.min(grant.remaining,player.getAbsorptionAmount());
                if(player.getEffect(MobEffects.DAMAGE_BOOST)!=grant.owned) {grant.owned=null;grant.previous=null;}
                if(grant.previous!=null && !grant.previous.tick(player,()->{})) grant.previous=null;
            }
        }
        if(eligible(player) && player.tickCount%20==0) {
            int seconds = Augmentations.level(player.getMainHandItem(),AdditionalAbilities.LIGHTNING_ROD)==1 ? 600 : 60;
            if(player.getRandom().nextInt(seconds)==0) extraStrike(player);
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {if(event.getEntity() instanceof ServerPlayer player) end(player);}
}
