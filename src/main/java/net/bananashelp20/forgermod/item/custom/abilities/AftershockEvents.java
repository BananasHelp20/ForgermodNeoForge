package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.*;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class AftershockEvents {
    public static final ResourceKey<DamageType> DAMAGE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"aftershock"));
    private record Swing(ItemStack stack,long tick,Set<UUID> targets) {}
    private record Echo(LivingEntity target,long due) {}
    private record Pending(ItemStack stack,int slot,ServerLevel level,List<Echo> echoes) {}
    private static final Map<UUID,Swing> SWINGS=new HashMap<>();
    private static final Map<UUID,Pending> PENDING=new HashMap<>();
    private AftershockEvents() {}
    @SubscribeEvent public static void swing(CriticalHitEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        SWINGS.remove(player.getUUID());
        if(player.isAlive() && !player.isSpectator() && player.getAttackStrengthScale(.5F)>.9F
                && Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.AFTERSHOCK))
            SWINGS.put(player.getUUID(),new Swing(player.getMainHandItem(),player.level().getGameTime(),new HashSet<>()));
    }
    @SubscribeEvent public static void landed(LivingDamageEvent.Post event) {
        if(!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        var swing=SWINGS.get(player.getUUID());long now=player.level().getGameTime();
        if(swing==null || swing.tick()!=now || swing.stack()!=player.getMainHandItem()
                || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0
                || !WeaponAbilityTargets.enemy(player,event.getEntity()) || !swing.targets().add(event.getEntity().getUUID())) return;
        var pending=PENDING.get(player.getUUID());
        if(pending==null || !valid(player,pending)) {
            pending=new Pending(swing.stack(),player.getInventory().selected,player.serverLevel(),new ArrayList<>());PENDING.put(player.getUUID(),pending);
        }
        pending.echoes().add(new Echo(event.getEntity(),now+20));
    }
    private static boolean valid(ServerPlayer player,Pending pending) {
        return player.isAlive() && !player.isSpectator() && player.getMainHandItem()==pending.stack() && player.getInventory().selected==pending.slot()
                && player.serverLevel()==pending.level() && Augmentations.hasPassive(pending.stack(),AdditionalAbilities.AFTERSHOCK);
    }
    public static void cancel(UUID player) {PENDING.remove(player);SWINGS.remove(player);}
    @SubscribeEvent public static void tick(PlayerTickEvent.Pre event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        var pending=PENDING.get(player.getUUID());if(pending==null)return;
        if(!valid(player,pending)) {cancel(player.getUUID());return;}
        long now=player.level().getGameTime();
        pending.echoes().removeIf(e->{
            if(e.target().level()!=pending.level() || !WeaponAbilityTargets.enemy(player,e.target()))return true;
            if(now<e.due())return false;
            var source=new DamageSource(player.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE),player);
            e.target().hurt(source,1);
            player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,e.target().getX(),e.target().getY()+1,e.target().getZ(),1,0,0,0,0);
            return true;
        });
        if(pending.echoes().isEmpty())PENDING.remove(player.getUUID());
    }
    @SubscribeEvent public static void endTick(ServerTickEvent.Post event) {SWINGS.clear();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
