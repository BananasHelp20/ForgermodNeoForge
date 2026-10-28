package net.bananashelp20.forgermod.item.custom.attacks.axe;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponControlLocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

/** One real jump/landing cycle shared by every player Vertical Strike. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class AxeVerticalMotion {
    public static final double HOLD=.83333,IMPACT=1.25;
    public static final int RAISE_TICKS=6;
    private static final Map<UUID,Flight> FLIGHTS=new HashMap<>();
    private static final class Flight {
        final ServerPlayer player;final ItemStack stack;final int slot;final Object level;final long started;
        long landed=-1;
        Flight(ServerPlayer p){player=p;stack=p.getMainHandItem();slot=p.getInventory().selected;level=p.level();started=p.level().getGameTime();}
    }
    private AxeVerticalMotion(){}
    public static double sourceTime(int phase,double elapsed){
        elapsed=Math.max(0,elapsed);
        if(phase==0)return Math.min(HOLD,elapsed*20/RAISE_TICKS*HOLD);
        // A short visible downswing bridges the held pose into the landing impact.
        return elapsed<.1?HOLD+(IMPACT-HOLD)*elapsed/.1:IMPACT+elapsed-.1;
    }
    public static void begin(ServerPlayer player){
        var previous=FLIGHTS.get(player.getUUID());
        if(previous!=null && previous.started==player.level().getGameTime() && previous.stack==player.getMainHandItem())return;
        FLIGHTS.put(player.getUUID(),new Flight(player));
        if(grounded(player)){
            var movement=player.getDeltaMovement();player.jumpFromGround();
            // Jump normally, including Jump Boost, without adding forward/sprint impulse.
            player.setDeltaMovement(movement.x,player.getDeltaMovement().y,movement.z);
            player.setOnGround(false);player.hasImpulse=true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }
    private static boolean grounded(ServerPlayer player){
        if(!player.onGround())return false;var b=player.getBoundingBox();
        var feet=new net.minecraft.world.phys.AABB(b.minX+.001,b.minY-.06,b.minZ+.001,b.maxX-.001,b.minY+.001,b.maxZ-.001);
        return !player.level().noCollision(player,feet);
    }
    private static boolean valid(Flight f){var p=f.player;return p.isAlive() && !p.isSpectator() && p.level()==f.level
            && !f.stack.isEmpty() && AxeItems.isAxe(f.stack) && p.getMainHandItem()==f.stack && p.getInventory().selected==f.slot && p.getOffhandItem().isEmpty() && !WeaponControlLocks.frozen(p);}
    public static boolean airborne(ServerPlayer player){var f=FLIGHTS.get(player.getUUID());return f!=null && f.landed<0 && valid(f);}
    public static boolean ready(ServerPlayer player){
        var flight=FLIGHTS.get(player.getUUID());if(flight==null)return false;
        if(!valid(flight)){cancel(player);return false;}
        if(flight.landed>=0)return true;
        if(grounded(player) && player.level().getGameTime()>flight.started){
            flight.landed=player.level().getGameTime();AxeAnimationNetwork.verticalPhase(player,1);return true;
        }
        return false;
    }
    public static void cancel(ServerPlayer player){if(FLIGHTS.remove(player.getUUID())!=null)AxeAnimationNetwork.verticalPhase(player,2);}
    public static void cancelIfWaiting(UUID owner){var f=FLIGHTS.get(owner);if(f!=null && f.landed<0 && !AxeHeavyNetwork.hasPending(f.player))cancel(f.player);}
    public static void forget(UUID owner){FLIGHTS.remove(owner);}
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;var f=FLIGHTS.get(player.getUUID());if(f==null)return;
        if(!valid(f))cancel(player);else if(f.landed>=0 && player.level().getGameTime()-f.landed>20)FLIGHTS.remove(player.getUUID());
    }
}
