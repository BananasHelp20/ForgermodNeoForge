package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
    public static void end(ServerPlayer player) {player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR);}
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        var armor=player.getAttribute(Attributes.ARMOR);
        if(player.isAlive() && !player.isSpectator() && player.getY()<0 && Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.STONE_SONG)) {
            if(!armor.hasModifier(ARMOR))armor.addTransientModifier(new AttributeModifier(ARMOR,2,AttributeModifier.Operation.ADD_VALUE));
        } else end(player);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {if(event.getEntity() instanceof ServerPlayer player)end(player);}
    @SubscribeEvent public static void travel(EntityTravelToDimensionEvent event) {if(event.getEntity() instanceof ServerPlayer player)end(player);}
    @SubscribeEvent public static void death(LivingDeathEvent event) {if(!event.isCanceled() && event.getEntity() instanceof ServerPlayer player)end(player);}
}
