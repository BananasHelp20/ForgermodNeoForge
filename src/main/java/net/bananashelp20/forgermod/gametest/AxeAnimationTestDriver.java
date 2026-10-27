package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.item.custom.attacks.axe.*;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Damage-only regression fixtures explicitly enter the impact phase; timing tests use real server ticks. */
final class AxeAnimationTestDriver {
    private AxeAnimationTestDriver(){}
    @SuppressWarnings("unchecked")
    static void finish(ServerPlayer player){
        try {
            var field=AxeAnimationImpacts.class.getDeclaredField("JOBS");field.setAccessible(true);
            var jobs=(Map<UUID,Map<String,Object>>)field.get(null);var own=jobs.remove(player.getUUID());
            if(own!=null){
                var contextField=AxeAnimationImpacts.class.getDeclaredField("IMPACT");contextField.setAccessible(true);var context=(ThreadLocal<AxeAnimationData.Clip>)contextField.get(null);var old=context.get();
                for(var job:own.values()){
                    var stack=job.getClass().getDeclaredMethod("stack");stack.setAccessible(true);if(stack.invoke(job)!=player.getMainHandItem())continue;
                    var clip=job.getClass().getDeclaredMethod("clip");clip.setAccessible(true);var action=job.getClass().getDeclaredMethod("action");action.setAccessible(true);
                    context.set((AxeAnimationData.Clip)clip.invoke(job));try{((Runnable)action.invoke(job)).run();}finally{if(old==null)context.remove();else context.set(old);}
                }
            }
            field=AxeHeavyNetwork.class.getDeclaredField("PENDING");field.setAccessible(true);var pending=(Map<UUID,Object>)field.get(null);var slam=pending.remove(player.getUUID());
            if(slam!=null){var source=slam.getClass().getDeclaredMethod("source");source.setAccessible(true);if(source.invoke(slam)==player.getMainHandItem()){
                var land=AxeHeavyNetwork.class.getDeclaredMethod("landSlam",ServerPlayer.class,slam.getClass());land.setAccessible(true);land.invoke(null,player,slam);
            }}
            field=AxeAnimationImpacts.class.getDeclaredField("NEXT_SWING");field.setAccessible(true);((Map<UUID,Long>)field.get(null)).remove(player.getUUID());
        }catch(ReflectiveOperationException e){throw new AssertionError("Cannot enter axe impact test phase",e);}
    }
}
