package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.*;
public final class ZeusStrikePlanTest {
    private record Target(int id,double x,boolean enemy){}
    public static void main(String[] args){
        var targets=List.of(new Target(1,5,true),new Target(2,12.5,true),new Target(3,19.5,true),new Target(4,28.5,true),new Target(5,37.5,true),new Target(6,46.5,true),new Target(7,55.5,true),new Target(8,64.5,true),new Target(9,73.5,true),new Target(10,20,false));
        var plan=ZeusStrikePlan.select(targets,4,Target::enemy,t->t.x()*t.x(),t->targets,(a,b)->Math.pow(a.x()-b.x(),2),Target::id,()->true);
        if(plan.size()!=8 || plan.stream().map(s->s.target().id()).distinct().count()!=8)throw new AssertionError("Targets repeat or chains exceed five");
        int[] times={0,4,8,10,12,16,20,20};
        int[] ids={3,4,5,2,6,7,1,8};
        for(int i=0;i<plan.size();i++){
            var strike=plan.get(i);
            if(strike.delayTicks()!=times[i] || strike.target().id()!=ids[i])throw new AssertionError("Empty wave not skipped or wrong wave/jump delay");
            if(strike.target().id()<=3 && (strike.previous()!=null || strike.damageMultiplier()!=2))throw new AssertionError("Initial strike");
            if(strike.target().id()>3 && (strike.previous().id()!=strike.target().id()-1 || strike.damageMultiplier()!=.5F))throw new AssertionError("Jump source or damage");
        }
        if(plan.getLast().target().x()<=20)throw new AssertionError("Chains cannot extend beyond initial radius");
        var single=ZeusStrikePlan.select(targets,1,Target::enemy,t->t.x()*t.x(),t->targets,(a,b)->Math.pow(a.x()-b.x(),2),Target::id,()->false);
        if(single.size()!=1 || single.getFirst().delayTicks()!=0)throw new AssertionError("Initial wave radius");
        var edges=List.of(new Target(11,5,true),new Target(12,5.001,true),new Target(13,10,true),new Target(14,10.001,true),new Target(15,15,true),new Target(16,15.001,true),new Target(17,20,true),new Target(18,20.001,true));
        for(int rank=1;rank<=4;rank++){
            int level=rank;var bounded=ZeusStrikePlan.select(edges,rank,Target::enemy,t->t.x()*t.x(),t->edges,(a,b)->Math.pow(a.x()-b.x(),2),Target::id,()->false);
            if(ZeusStrikePlan.waveRadius(rank)!=5*rank || bounded.size()!=2*rank-1 || bounded.stream().anyMatch(s->s.target().x()>5*level))throw new AssertionError("Halved wave boundary at level "+rank);
            for(var strike:bounded){int wave=(int)Math.ceil(strike.target().x()/5)-1;if(strike.delayTicks()!=(rank-1-wave)*10)throw new AssertionError("Wrong five-block band timing");}
        }
        if(!ZeusStrikePlan.startsChain(0) || !ZeusStrikePlan.startsChain(.49999F) || ZeusStrikePlan.startsChain(.5F) || ZeusStrikePlan.startsChain(.99F))throw new AssertionError("50 percent boundary");
    }
}
