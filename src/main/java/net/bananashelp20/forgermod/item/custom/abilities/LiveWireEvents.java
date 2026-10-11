package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class LiveWireEvents {
    private record Wire(ItemStack stack,int selected,ServerLevel level,long expires) {}
    private static final Map<UUID,Wire> WIRES=new HashMap<>();
    private LiveWireEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        WIRES.put(player.getUUID(),new Wire(stack,player.getInventory().selected,player.serverLevel(),player.level().getGameTime()+160));return true;
    }
    private static boolean valid(ServerPlayer player,Wire wire) {
        return wire!=null && !player.isSpectator() && player.getMainHandItem()==wire.stack()
                && player.getInventory().selected==wire.selected() && player.serverLevel()==wire.level()
                && player.level().getGameTime()<wire.expires() && Augmentations.ids(wire.stack(),true).contains(AdditionalAbilities.LIVE_WIRE);
    }
    public static boolean active(ServerPlayer player) {
        if(!player.isAlive() || !valid(player,WIRES.get(player.getUUID()))) {cancel(player.getUUID());return false;}return true;
    }
    public static void cancel(UUID owner) {WIRES.remove(owner);}
    @SubscribeEvent
    public static void reflect(LivingDamageEvent.Post event) {
        // Validate without the alive check: a lethal melee hit still earns its one reflection.
        if(!(event.getEntity() instanceof ServerPlayer player) || !valid(player,WIRES.get(player.getUUID()))
                || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0
                || !(event.getSource().is(DamageTypes.MOB_ATTACK) || event.getSource().is(DamageTypes.PLAYER_ATTACK))
                || !(event.getSource().getDirectEntity() instanceof LivingEntity attacker) || !WeaponAbilityTargets.enemy(player,attacker)) return;
        int immunity=attacker.invulnerableTime;attacker.invulnerableTime=0;
        try {attacker.hurt(player.damageSources().indirectMagic(player,player),2);}
        finally {attacker.invulnerableTime=Math.max(immunity,attacker.invulnerableTime);}
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,attacker.getX(),attacker.getY()+1,attacker.getZ(),12,.3,.4,.3,.08);
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player && active(player) && player.tickCount%10==0)
            player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,player.getX(),player.getY()+1,player.getZ(),3,.3,.4,.3,.02);
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
