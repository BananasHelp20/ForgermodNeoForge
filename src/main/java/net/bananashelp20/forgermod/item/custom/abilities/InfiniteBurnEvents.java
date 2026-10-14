package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
/** Persistent burn flag; duration stays within vanilla's saved short and keeps its damage cadence. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class InfiniteBurnEvents {
    private static final String FLAG="forgermod_infinite_burn";
    private InfiniteBurnEvents() {}
    public static boolean active(LivingEntity entity) {return entity.getPersistentData().getBoolean(FLAG);}
    public static void ignite(LivingEntity entity) {
        if(entity.level().isClientSide || !entity.isAlive() || entity.fireImmune())return;
        entity.getPersistentData().putBoolean(FLAG,true);
        entity.setRemainingFireTicks(30000+Math.floorMod(Math.max(0,entity.getRemainingFireTicks()),20));
    }
    /** Called by the vanilla fire setter so extinguishing and same-tick reignition stay distinct. */
    public static void extinguished(Entity entity) {
        if(!entity.level().isClientSide && entity instanceof LivingEntity)entity.getPersistentData().remove(FLAG);
    }
    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if(event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide && active(entity)) {
            if(!entity.isAlive() || entity.fireImmune() || entity.getRemainingFireTicks()<=0)extinguished(entity);
            else entity.setRemainingFireTicks(30000+Math.floorMod(entity.getRemainingFireTicks(),20));
        }
    }
}
