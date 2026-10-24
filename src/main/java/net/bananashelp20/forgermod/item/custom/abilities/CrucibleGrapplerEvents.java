package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.*;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import java.util.*;

/** Block hooks accelerate ordinary player movement; releasing never edits the accumulated velocity. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class CrucibleGrapplerEvents {
    private record Grapple(ItemStack stack,int slot,ServerLevel level,BlockPos block,Vec3 destination,boolean autoRelease){}
    private static final Map<UUID,Grapple> GRAPPLES=new HashMap<>();
    private static final double PULL_ACCELERATION=.45;
    private CrucibleGrapplerEvents(){}
    public static boolean activate(ServerPlayer player,ItemStack stack,boolean autoRelease){
        int rank=Augmentations.level(stack,RecommendedAbilities.CRUCIBLE_HOOK);int range=ReviewedRanks.grappleRange(rank);
        if(rank<5 || active(player) || stack!=player.getMainHandItem() || !player.isAlive() || player.isSpectator() || WeaponControlLocks.frozen(player))return false;
        Vec3 start=player.getEyePosition(),end=start.add(player.getLookAngle().scale(range));
        if(!player.serverLevel().hasChunksAt(BlockPos.containing(start),BlockPos.containing(end)))return false;
        var hit=player.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        if(hit.getType()!=HitResult.Type.BLOCK)return false;
        var destination=hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(.8)).add(0,-player.getBbHeight()*.5,0);
        GRAPPLES.put(player.getUUID(),new Grapple(stack,player.getInventory().selected,player.serverLevel(),hit.getBlockPos().immutable(),destination,autoRelease));
        CrucibleHookRope.beginBlock(player,hit.getLocation());tick(player);return true;
    }
    public static boolean active(ServerPlayer player){
        var grapple=GRAPPLES.get(player.getUUID());if(grapple==null)return false;
        if(player.isAlive() && !player.isSpectator() && player.getMainHandItem()==grapple.stack && player.getInventory().selected==grapple.slot
                && player.serverLevel()==grapple.level && Augmentations.level(grapple.stack,RecommendedAbilities.CRUCIBLE_HOOK)>=5
                && Augmentations.ids(grapple.stack,true).contains(RecommendedAbilities.CRUCIBLE_HOOK) && grapple.level.hasChunkAt(grapple.block)
                && !grapple.level.getBlockState(grapple.block).getCollisionShape(grapple.level,grapple.block).isEmpty())return true;
        cancel(player.getUUID());return false;
    }
    public static void cancel(UUID owner){if(GRAPPLES.remove(owner)!=null)CrucibleHookRope.end(owner);}
    public static void tick(ServerPlayer player){
        if(!active(player))return;var grapple=GRAPPLES.get(player.getUUID());Vec3 direction=grapple.destination.subtract(player.position());
        Vec3 velocity=player.getDeltaMovement();
        double length=velocity.lengthSqr();double t=length<.000001?0:Math.clamp(direction.dot(velocity)/length,0,1);
        boolean reaching=direction.lengthSqr()<=1 || direction.subtract(velocity.scale(t)).lengthSqr()<=.64;
        if(grapple.autoRelease && reaching){cancel(player.getUUID());return;}
        if(direction.lengthSqr()>.000001){
            velocity=velocity.add(direction.normalize().scale(PULL_ACCELERATION));if(velocity.lengthSqr()>6.25)velocity=velocity.normalize().scale(2.5);
            player.setDeltaMovement(velocity);player.hasImpulse=true;player.hurtMarked=true;player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }
    @SubscribeEvent public static void release(PlayerInteractEvent.RightClickItem event){
        if(event.getHand()==InteractionHand.MAIN_HAND && event.getEntity() instanceof ServerPlayer player && active(player)){
            cancel(player.getUUID());event.setCancellationResult(InteractionResult.SUCCESS);event.setCanceled(true);
        }
    }
    @SubscribeEvent public static void releaseOnBlock(PlayerInteractEvent.RightClickBlock event){
        if(event.getHand()==InteractionHand.MAIN_HAND && event.getEntity() instanceof ServerPlayer player && active(player)){
            cancel(player.getUUID());event.setCancellationResult(InteractionResult.SUCCESS);event.setCanceled(true);
        }
    }
    @SubscribeEvent public static void releaseOnEntity(PlayerInteractEvent.EntityInteract event){
        if(event.getHand()==InteractionHand.MAIN_HAND && event.getEntity() instanceof ServerPlayer player && active(player)){
            cancel(player.getUUID());event.setCancellationResult(InteractionResult.SUCCESS);event.setCanceled(true);
        }
    }
    @SubscribeEvent public static void releaseOnEntityPart(PlayerInteractEvent.EntityInteractSpecific event){
        if(event.getHand()==InteractionHand.MAIN_HAND && event.getEntity() instanceof ServerPlayer player && active(player)){
            cancel(player.getUUID());event.setCancellationResult(InteractionResult.SUCCESS);event.setCanceled(true);
        }
    }
}
