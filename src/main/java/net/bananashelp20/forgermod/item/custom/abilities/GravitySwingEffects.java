package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.ReviewedRanks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Transient per-victim modifiers last through the landing's fall damage, never stack with themselves. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class GravitySwingEffects {
    private static final ResourceLocation MODIFIER=ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"gravity_swing");
    private static final Map<UUID,LivingEntity> TARGETS=new HashMap<>();
    private GravitySwingEffects() {}
    public static void apply(LivingEntity target,int rank) {
        var previous=TARGETS.get(target.getUUID());if(previous!=null)clear(previous);
        double bonus=ReviewedRanks.gravitySwingBonus(rank);
        for(var attribute:java.util.List.of(Attributes.GRAVITY,Attributes.FALL_DAMAGE_MULTIPLIER)) {
            var instance=target.getAttribute(attribute);
            if(instance!=null)instance.addTransientModifier(new AttributeModifier(MODIFIER,bonus,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        TARGETS.put(target.getUUID(),target);
    }
    public static void clear(LivingEntity target) {
        if(TARGETS.get(target.getUUID())!=target)return;
        TARGETS.remove(target.getUUID());
        for(var attribute:java.util.List.of(Attributes.GRAVITY,Attributes.FALL_DAMAGE_MULTIPLIER)) {
            var instance=target.getAttribute(attribute);if(instance!=null)instance.removeModifier(MODIFIER);
        }
    }
    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if(event.getEntity() instanceof LivingEntity target && !target.level().isClientSide
                && TARGETS.get(target.getUUID())==target
                && (!target.isAlive() || target.onGround() || target.isInWaterOrBubble() || target.isInLava() || target.onClimbable()))clear(target);
    }
    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent event) {
        if(event.getEntity() instanceof LivingEntity target && !event.getLevel().isClientSide)clear(target);
    }
}
