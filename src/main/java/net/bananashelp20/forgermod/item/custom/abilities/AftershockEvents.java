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
        if(!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
        SWINGS.remove(player.getUUID());
        if(player.isAlive() && !player.isSpectator() && player.getAttackStrengthScale(.5F)>.9F
                && Augmentations.hasPassive(PassiveActors.weapon(player,AdditionalAbilities.AFTERSHOCK),AdditionalAbilities.AFTERSHOCK))
            SWINGS.put(player.getUUID(),new Swing(PassiveActors.weapon(player,AdditionalAbilities.AFTERSHOCK),player.level().getGameTime(),new HashSet<>()));
    }
    public static void prepare(LivingEntity actor){
        SWINGS.remove(actor.getUUID());
        if(actor.isAlive() && PassiveActors.has(actor,AdditionalAbilities.AFTERSHOCK))
            SWINGS.put(actor.getUUID(),new Swing(PassiveActors.weapon(actor,AdditionalAbilities.AFTERSHOCK),actor.level().getGameTime(),new HashSet<>()));
    }
    @SubscribeEvent public static void landed(LivingDamageEvent.Post event) {
        if(!PassiveActors.melee(event.getSource()) || !(PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player)) return;
        var swing=SWINGS.get(player.getUUID());long now=player.level().getGameTime();
        if(swing==null || swing.tick()!=now || swing.stack()!=PassiveActors.weapon(player,AdditionalAbilities.AFTERSHOCK)
                || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0
                || !WeaponAbilityTargets.enemy(player,event.getEntity()) || !swing.targets().add(event.getEntity().getUUID())) return;
        var pending=PENDING.get(player.getUUID());
        if(pending==null || !valid(player,pending)) {
            pending=new Pending(swing.stack(),PassiveActors.selected(player),PassiveActors.level(player),new ArrayList<>());PENDING.put(player.getUUID(),pending);
        }
        pending.echoes().add(new Echo(event.getEntity(),now+20));
    }
    private static boolean valid(LivingEntity player,Pending pending) {
        return player.isAlive() && !player.isSpectator() && PassiveActors.weapon(player,AdditionalAbilities.AFTERSHOCK)==pending.stack() && PassiveActors.selected(player)==pending.slot()
                && PassiveActors.level(player)==pending.level() && Augmentations.hasPassive(pending.stack(),AdditionalAbilities.AFTERSHOCK);
    }
    public static void cancel(UUID player) {PENDING.remove(player);SWINGS.remove(player);}
    @SubscribeEvent public static void tick(PassiveActorTickEvent.Pre event) {
        if(!(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player)) return;
        var pending=PENDING.get(player.getUUID());if(pending==null)return;
        if(!valid(player,pending)) {cancel(player.getUUID());return;}
        long now=player.level().getGameTime();
        pending.echoes().removeIf(e->{
            if(e.target().level()!=pending.level() || !WeaponAbilityTargets.enemy(player,e.target()))return true;
            if(now<e.due())return false;
            var source=new DamageSource(player.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE),player);
            e.target().hurt(source,1);
            PassiveActors.level(player).sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,e.target().getX(),e.target().getY()+1,e.target().getZ(),1,0,0,0,0);
            return true;
        });
        if(pending.echoes().isEmpty())PENDING.remove(player.getUUID());
    }
    @SubscribeEvent public static void endTick(ServerTickEvent.Post event) {SWINGS.clear();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
    public static void tick(PlayerTickEvent.Pre event){tick(new PassiveActorTickEvent.Pre(event.getEntity()));}
}
