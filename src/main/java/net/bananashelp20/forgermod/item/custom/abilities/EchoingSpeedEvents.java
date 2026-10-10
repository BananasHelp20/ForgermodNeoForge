package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class EchoingSpeedEvents {
    private static final ResourceLocation MODIFIER=ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"echoing_speed");
    private record Fight(ItemStack stack,int selected,LivingEntity target,int hits,long expires) {}
    private static final Map<UUID,Fight> FIGHTS=new HashMap<>();
    private EchoingSpeedEvents() {}
    public static void clear(ServerPlayer player) {
        FIGHTS.remove(player.getUUID()); player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(MODIFIER);
    }
    private static boolean valid(ServerPlayer player,Fight fight) {
        return player.isAlive() && !player.isSpectator() && player.getMainHandItem()==fight.stack()
                && player.getInventory().selected==fight.selected() && fight.target().isAlive()
                && fight.target().level()==player.level() && player.level().getGameTime()<fight.expires()
                && Augmentations.hasPassive(fight.stack(),RecommendedAbilities.ECHOING_SPEED);
    }
    @SubscribeEvent
    public static void onHit(LivingDamageEvent.Post event) {
        if(event.getNewDamage()<=0 || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        var stack=player.getMainHandItem();
        if(!player.isAlive() || player.isSpectator() || !Augmentations.hasPassive(stack,RecommendedAbilities.ECHOING_SPEED)
                || !WeaponAbilityTargets.enemy(player,event.getEntity())) return;
        var fight=FIGHTS.get(player.getUUID());
        int hits=fight!=null && valid(player,fight) && fight.target()==event.getEntity()?fight.hits()+1:1;
        int rank=Augmentations.level(stack,RecommendedAbilities.ECHOING_SPEED);
        hits=Math.min(hits,2+rank*2);
        FIGHTS.put(player.getUUID(),new Fight(stack,player.getInventory().selected,event.getEntity(),hits,player.level().getGameTime()+160));
        var speed=player.getAttribute(Attributes.ATTACK_SPEED); speed.removeModifier(MODIFIER);
        speed.addTransientModifier(new AttributeModifier(MODIFIER,hits*.05,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Pre event) {
        if(event.getEntity() instanceof ServerPlayer player) {
            var fight=FIGHTS.get(player.getUUID()); if(fight!=null && !valid(player,fight)) clear(player);
        }
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) clear(player);
    }
}
