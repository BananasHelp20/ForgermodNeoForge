package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class SunfedEvents {
    private static final class Meal {
        final ItemStack stack;final int slot;final ServerLevel level;int ticks;
        Meal(ServerPlayer player) {stack=player.getMainHandItem();slot=player.getInventory().selected;level=player.serverLevel();}
    }
    private static final Map<UUID,Meal> MEALS=new HashMap<>();
    private SunfedEvents() {}
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Pre event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        var stack=player.getMainHandItem();
        if(!player.isAlive() || player.isSpectator() || !Augmentations.hasPassive(stack,AdditionalAbilities.SUNFED) || !player.level().dimensionType().hasSkyLight()
            || !player.level().isDay() || player.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,player.blockPosition().getX(),player.blockPosition().getZ())>player.blockPosition().getY() || player.getFoodData().getFoodLevel()>=20 && player.getFoodData().getSaturationLevel()>=20) {end(player.getUUID());return;}
        var meal=MEALS.get(player.getUUID());if(meal==null || meal.stack!=stack || meal.slot!=player.getInventory().selected || meal.level!=player.level()) {meal=new Meal(player);MEALS.put(player.getUUID(),meal);}
        if(++meal.ticks>=300) {meal.ticks=0;player.getFoodData().eat(1,.5F);}
    }
    public static void end(UUID owner) {MEALS.remove(owner);}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {end(event.getEntity().getUUID());}
}
