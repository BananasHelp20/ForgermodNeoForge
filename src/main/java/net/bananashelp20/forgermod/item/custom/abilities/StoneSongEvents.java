package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.LivingEntity;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class StoneSongEvents {
    private static final ResourceLocation ARMOR=ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"stone_song_armor");
    private StoneSongEvents() {}
    public static void end(LivingEntity player) {player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR);}
    @SubscribeEvent public static void tick(PassiveActorTickEvent.Post event) {
        if(!(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player))return;
        var armor=player.getAttribute(Attributes.ARMOR);
        if(player.isAlive() && !player.isSpectator() && player.getY()<0 && Augmentations.hasPassive(PassiveActors.weapon(player,AdditionalAbilities.STONE_SONG),AdditionalAbilities.STONE_SONG)) {
            if(!armor.hasModifier(ARMOR))armor.addTransientModifier(new AttributeModifier(ARMOR,2,AttributeModifier.Operation.ADD_VALUE));
        } else end(player);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {if(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player)end(player);}
    @SubscribeEvent public static void travel(EntityTravelToDimensionEvent event) {if(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player)end(player);}
    @SubscribeEvent public static void death(LivingDeathEvent event) {if(!event.isCanceled() && PassiveActors.entity(event.getEntity()) instanceof LivingEntity player)end(player);}
    public static void tick(PlayerTickEvent.Post event){tick(new PassiveActorTickEvent.Post(event.getEntity()));}
}
