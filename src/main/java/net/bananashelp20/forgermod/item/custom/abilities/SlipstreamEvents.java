package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class SlipstreamEvents {
    private static final ResourceLocation SPEED=ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"slipstream_speed");
    private static final class Watch {
        final ItemStack stack;final int selected;final ServerLevel level;Vec3 previous;final Map<UUID,Vec3> allies=new HashMap<>();
        Watch(LivingEntity player) {stack=PassiveActors.weapon(player,AdditionalAbilities.SLIPSTREAM);selected=PassiveActors.selected(player);level=PassiveActors.level(player);previous=player.position();}
    }
    private static final Map<UUID,Watch> WATCHES=new HashMap<>();
    private SlipstreamEvents() {}
    private static Vec3 horizontal(Vec3 vector) {return vector.multiply(1,0,1);}
    public static void end(LivingEntity player) {WATCHES.remove(player.getUUID());player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED);}
    @SubscribeEvent
    public static void tick(PassiveActorTickEvent.Post event) {
        if(!(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player)) return;
        if(!player.isAlive() || player.isSpectator() || !Augmentations.hasPassive(PassiveActors.weapon(player,AdditionalAbilities.SLIPSTREAM),AdditionalAbilities.SLIPSTREAM)) {end(player);return;}
        var watch=WATCHES.get(player.getUUID());
        if(watch==null || watch.stack!=PassiveActors.weapon(player,AdditionalAbilities.SLIPSTREAM) || watch.selected!=PassiveActors.selected(player) || watch.level!=PassiveActors.level(player)) {end(player);watch=new Watch(player);WATCHES.put(player.getUUID(),watch);}
        var movement=horizontal(player.position().subtract(watch.previous));watch.previous=player.position();boolean following=false;var seen=new HashSet<UUID>();
        for(var ally:player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(3),e->WeaponAbilityTargets.ally(player,e) && player.distanceToSqr(e)<=9)) {
            var id=ally.getUUID();seen.add(id);var old=watch.allies.put(id,ally.position());var motion=horizontal(old==null?ally.getDeltaMovement():ally.position().subtract(old));
            if(!player.isSprinting() || movement.lengthSqr()<.0004 || motion.lengthSqr()<.0004 || !player.hasLineOfSight(ally)) continue;
            var ahead=horizontal(ally.position().subtract(player.position()));
            if(ahead.lengthSqr()>.01 && ahead.normalize().dot(motion.normalize())>.8 && movement.normalize().dot(motion.normalize())>.8) following=true;
        }
        watch.allies.keySet().retainAll(seen);var attribute=player.getAttribute(Attributes.MOVEMENT_SPEED);
        if(following) {if(!attribute.hasModifier(SPEED)) attribute.addTransientModifier(new AttributeModifier(SPEED,.1,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));}
        else attribute.removeModifier(SPEED);
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {if(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player) end(player);}
    public static void tick(PlayerTickEvent.Post event){tick(new PassiveActorTickEvent.Post(event.getEntity()));}
}
