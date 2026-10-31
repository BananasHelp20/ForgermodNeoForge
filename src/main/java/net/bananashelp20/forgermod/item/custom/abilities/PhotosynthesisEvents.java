package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.LivingEntity;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class PhotosynthesisEvents {
    private static final class Repair {
        final ItemStack stack;final int slot;final ServerLevel level;int ticks;
        Repair(LivingEntity player) {stack=PassiveActors.weapon(player,AdditionalAbilities.PHOTOSYNTHESIS);slot=PassiveActors.selected(player);level=PassiveActors.level(player);}
    }
    private static final Map<UUID,Repair> REPAIRS=new HashMap<>();
    private PhotosynthesisEvents() {}
    public static int restoredDurability(int rank) { return ReviewedRanks.at(rank,1,2,3,5); }
    @SubscribeEvent
    public static void tick(PassiveActorTickEvent.Pre event) {
        if(!(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player)) return;
        var stack=PassiveActors.weapon(player,AdditionalAbilities.PHOTOSYNTHESIS);
        if(!player.isAlive() || player.isSpectator() || !Augmentations.hasPassive(stack,AdditionalAbilities.PHOTOSYNTHESIS) || !player.level().dimensionType().hasSkyLight()
            || !player.level().isDay() || !stack.isDamaged()) {end(player.getUUID());return;}
        var repair=REPAIRS.get(player.getUUID());if(repair==null || repair.stack!=stack || repair.slot!=PassiveActors.selected(player) || repair.level!=player.level()) {repair=new Repair(player);REPAIRS.put(player.getUUID(),repair);}
        if(++repair.ticks>=300) {repair.ticks=0;stack.setDamageValue(Math.max(0,stack.getDamageValue()-restoredDurability(Augmentations.level(stack,AdditionalAbilities.PHOTOSYNTHESIS))));if(player instanceof net.minecraft.world.entity.Mob mob)NecromancerEquipment.changed(mob);}
    }
    public static void end(UUID owner) {REPAIRS.remove(owner);}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {end(event.getEntity().getUUID());}
    public static void tick(PlayerTickEvent.Pre event){tick(new PassiveActorTickEvent.Pre(event.getEntity()));}
}
