package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.*;
public final class ZeusStrikePlanTest {
    private record Target(int id,double x,boolean enemy){}
    public static void main(String[] args){
        var targets=List.of(new Target(1,5,true),new Target(2,25,true),new Target(3,39,true),new Target(4,48,true),new Target(5,57,true),new Target(6,66,true),new Target(7,75,true),new Target(8,84,true),new Target(9,93,true),new Target(10,40,false));
        var plan=ZeusStrikePlan.select(targets,4,Target::enemy,t->t.x()*t.x(),t->targets,(a,b)->Math.pow(a.x()-b.x(),2),Target::id,()->true);
        if(plan.size()!=8 || plan.stream().map(s->s.target().id()).distinct().count()!=8)throw new AssertionError("Targets repeat or chains exceed five");
        int[] times={0,10,20,24,28,32,36,40};
        for(int i=0;i<plan.size();i++){
            var strike=plan.get(i);
            if(strike.delayTicks()!=times[i])throw new AssertionError("Empty wave not skipped or wrong wave/jump delay");
            if(i<3 && (strike.previous()!=null || strike.damageMultiplier()!=2))throw new AssertionError("Initial strike");
            if(i>=3 && (strike.previous().id()!=strike.target().id()-1 || strike.damageMultiplier()!=.5F))throw new AssertionError("Jump source or damage");
        }
        if(plan.getLast().target().x()<=40)throw new AssertionError("Chains cannot extend beyond initial radius");
        var single=ZeusStrikePlan.select(targets,1,Target::enemy,t->t.x()*t.x(),t->targets,(a,b)->Math.pow(a.x()-b.x(),2),Target::id,()->false);
        if(single.size()!=1 || single.getFirst().delayTicks()!=0)throw new AssertionError("Initial wave radius");
        if(!ZeusStrikePlan.startsChain(0) || !ZeusStrikePlan.startsChain(.49999F) || ZeusStrikePlan.startsChain(.5F) || ZeusStrikePlan.startsChain(.99F))throw new AssertionError("50 percent boundary");
    }
}
