package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class FalseAwakeningEvents {
    private record Attention(Mob mob,LivingEntity previous) {}
    private record Dream(ServerPlayer owner,ItemStack stack,int selected,LivingEntity real,DreamEchoEntity echo,
                         long expires,int rank,Map<UUID,Attention> targets) {}
    private static final Map<UUID,Dream> DREAMS=new HashMap<>();
    private FalseAwakeningEvents() {}
    private static ItemStack armor(Item item) {
        var stack=new ItemStack(item); stack.set(DataComponents.DYED_COLOR,new DyedItemColor(0x9873c5,false)); return stack;
    }
    private static ItemStack head(LivingEntity real) {
        if(real instanceof Player player) {
            var head=new ItemStack(Items.PLAYER_HEAD); head.set(DataComponents.PROFILE,new ResolvableProfile(player.getGameProfile())); return head;
        }
        if(real.getType()==EntityType.ZOMBIE || real.getType()==EntityType.HUSK || real.getType()==EntityType.DROWNED) return new ItemStack(Items.ZOMBIE_HEAD);
        if(real.getType()==EntityType.SKELETON || real.getType()==EntityType.STRAY) return new ItemStack(Items.SKELETON_SKULL);
        if(real.getType()==EntityType.WITHER_SKELETON) return new ItemStack(Items.WITHER_SKELETON_SKULL);
        if(real.getType()==EntityType.CREEPER) return new ItemStack(Items.CREEPER_HEAD);
        return armor(Items.LEATHER_HELMET);
    }
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        var real=WeaponAbilityTargets.aimedEnemy(player,20); if(real==null) return false;
        var right=new Vec3(player.getLookAngle().z,0,-player.getLookAngle().x).normalize();
        if(right.lengthSqr()<.1) right=new Vec3(1,0,0);
        DreamEchoEntity echo=null;
        for(double offset:new double[]{1.5,-1.5,2.5,-2.5}) {
            var candidate=new DreamEchoEntity(player,real.position().add(right.scale(offset)));
            var box=candidate.getBoundingBox();
            if(box.minY>=player.level().getMinBuildHeight() && box.maxY<=player.level().getMaxBuildHeight()
                    && player.level().getWorldBorder().isWithinBounds(box)
                    && player.serverLevel().hasChunksAt(net.minecraft.core.BlockPos.containing(box.minX,box.minY,box.minZ),
                        net.minecraft.core.BlockPos.containing(Math.nextDown(box.maxX),Math.nextDown(box.maxY),Math.nextDown(box.maxZ)))
                    && player.level().noCollision(candidate,box) && player.hasLineOfSight(candidate)) { echo=candidate; break; }
        }
        if(echo==null) return false;
        echo.setNoGravity(true); echo.setSilent(true); echo.setNoBasePlate(true); echo.setShowArms(true); echo.setInvisible(true);
        echo.setYRot(real.getYRot()); echo.setItemSlot(EquipmentSlot.HEAD,head(real));
        echo.setItemSlot(EquipmentSlot.CHEST,armor(Items.LEATHER_CHESTPLATE)); echo.setItemSlot(EquipmentSlot.LEGS,armor(Items.LEATHER_LEGGINGS)); echo.setItemSlot(EquipmentSlot.FEET,armor(Items.LEATHER_BOOTS));
        if(!player.serverLevel().addFreshEntity(echo)) return false;
        int rank=Augmentations.level(stack,RecommendedAbilities.FALSE_AWAKENING);
        var dream=new Dream(player,stack,player.getInventory().selected,real,echo,player.level().getGameTime()+60+20L*rank,rank,new HashMap<>());
        DREAMS.put(player.getUUID(),dream); attract(dream); return true;
    }
    private static void attract(Dream dream) {
        for(var mob:dream.owner().level().getEntitiesOfClass(Mob.class,dream.echo().getBoundingBox().inflate(8),
                mob->WeaponAbilityTargets.enemy(dream.owner(),mob) && mob.hasLineOfSight(dream.echo()))) {
            if(mob.getTarget()==null || mob.getTarget()==dream.owner()) {
                dream.targets().putIfAbsent(mob.getUUID(),new Attention(mob,mob.getTarget())); mob.setTarget(dream.echo());
            }
        }
    }
    public static boolean active(ServerPlayer player) {
        var dream=DREAMS.get(player.getUUID()); if(dream==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=dream.stack()
                || player.getInventory().selected!=dream.selected() || player.level()!=dream.echo().level()
                || dream.echo().isRemoved() || !dream.real().isAlive() || dream.real().level()!=player.level() || dream.real().isAlliedTo(player)
                || player.distanceToSqr(dream.real())>32*32 || player.level().getGameTime()>=dream.expires()
                || !Augmentations.ids(dream.stack(),true).contains(RecommendedAbilities.FALSE_AWAKENING)) {
            cancel(player.getUUID()); return false;
        }
        return true;
    }
    public static void cancel(UUID owner) {
        var dream=DREAMS.remove(owner); if(dream==null) return;
        for(var attention:dream.targets().values()) if(attention.mob().getTarget()==dream.echo())
            attention.mob().setTarget(attention.previous()!=null && attention.previous().isAlive()
                    && !attention.previous().isRemoved() && attention.previous().level()==attention.mob().level()?attention.previous():null);
        dream.echo().discard();
    }
    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent event) {
        if(event.getAmount()>0 && event.getSource().is(DamageTypes.PLAYER_ATTACK)
                && event.getSource().getEntity() instanceof ServerPlayer player && active(player)) {
            var dream=DREAMS.get(player.getUUID()); if(event.getEntity()==dream.real()) event.setAmount(event.getAmount()*(1.05F+.05F*dream.rank()));
        }
    }
    @SubscribeEvent
    public static void onHit(LivingDamageEvent.Post event) {
        if(event.getNewDamage()>0 && event.getSource().is(DamageTypes.PLAYER_ATTACK)
                && event.getSource().getEntity() instanceof ServerPlayer player) {
            // A lethal hit must still remove the echo, even though active() now sees a dead target.
            var dream=DREAMS.get(player.getUUID());
            if(dream!=null && event.getEntity()==dream.real()) cancel(player.getUUID());
        }
    }
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;
        var dream=DREAMS.get(player.getUUID());
        if(player.level().getGameTime()%5==0) {
            attract(dream);
            var silhouette=(player.level().getGameTime()/10)%2==0?dream.real():dream.echo();
            // Alternate sparse outlines, keeping real entity rendering/shadow intact for player-readable tells.
            for(double height:new double[]{.2,.8,1.4})
                player.serverLevel().sendParticles(ParticleTypes.END_ROD,silhouette.getX(),silhouette.getY()+height,silhouette.getZ(),2,.35,.02,.35,0);
        }
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { cancel(event.getEntity().getUUID()); }
}
