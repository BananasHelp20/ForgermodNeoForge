package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.*;
import java.util.function.*;

/** All wave and chain selections are immutable and globally unique from the moment of activation. */
public final class ZeusStrikePlan {
    public record Strike<T>(T target,T previous,int delayTicks,float damageMultiplier) {}
    private ZeusStrikePlan(){}
    public static boolean startsChain(float roll){return roll<.5F;}
    public static <T,K> List<Strike<T>> select(Iterable<T> initial,int waves,Predicate<T> eligible,
            ToDoubleFunction<T> distanceFromOwner,Function<T,? extends Iterable<T>> neighbors,
            ToDoubleBiFunction<T,T> distanceBetween,Function<T,K> key,BooleanSupplier startChain){
        waves=Math.clamp(waves,1,4);
        var groups=new ArrayList<List<T>>();for(int wave=0;wave<waves;wave++)groups.add(new ArrayList<>());
        Set<K> marked=new HashSet<>();double maximum=waves*10.0;
        for(T target:initial){
            double distance=distanceFromOwner.applyAsDouble(target);
            if(eligible.test(target) && distance<=maximum*maximum && marked.add(key.apply(target)))
                groups.get(Math.clamp((int)Math.ceil(Math.sqrt(distance)/10)-1,0,waves-1)).add(target);
        }
        List<Strike<T>> strikes=new ArrayList<>();int nonemptyWave=0;
        for(var group:groups){
            if(group.isEmpty())continue;
            for(T target:group)strikes.add(new Strike<>(target,null,nonemptyWave*10,2));
            nonemptyWave++;
        }
        // Reserve every initial target before looking for chains, including later waves.
        for(var initialStrike:List.copyOf(strikes)){
            if(!startChain.getAsBoolean())continue;T previous=initialStrike.target();
            for(int jump=1;jump<=5;jump++){
                T next=null;double nearest=Double.POSITIVE_INFINITY;
                for(T candidate:neighbors.apply(previous)){
                    if(!eligible.test(candidate) || marked.contains(key.apply(candidate)))continue;
                    double distance=distanceBetween.applyAsDouble(previous,candidate);
                    if(distance<=100 && distance<nearest){next=candidate;nearest=distance;}
                }
                if(next==null)break;
                marked.add(key.apply(next));strikes.add(new Strike<>(next,previous,initialStrike.delayTicks()+jump*4,.5F));previous=next;
            }
        }
        strikes.sort(Comparator.comparingInt(Strike::delayTicks));
        return List.copyOf(strikes);
    }
}
