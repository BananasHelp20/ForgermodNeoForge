package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.function.DoublePredicate;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Refines the first obstructed swept segment to its farthest safe point. */
public final class VoidStepPath {
    public static final int STEPS = 40;
    public static final int PAIRED_STEPS = 80;
    public static final double STEP_DISTANCE = 0.25;
    private VoidStepPath() {}

    /** Exact moving-body/obstacle intersection, preserving the aim direction instead of sliding on a surface. */
    public static double collisionFraction(AABB body,Vec3 step,AABB obstacle){
        double enter=0,exit=1;
        for(var axis:Direction.Axis.values()){
            double speed=step.get(axis);
            if(Math.abs(speed)<1.0E-12){
                if(body.max(axis)<=obstacle.min(axis)+1.0E-7 || body.min(axis)>=obstacle.max(axis)-1.0E-7)return 1;
                continue;
            }
            double near=(obstacle.min(axis)-body.max(axis))/speed,far=(obstacle.max(axis)-body.min(axis))/speed;
            if(near>far){double swap=near;near=far;far=swap;}
            enter=Math.max(enter,near);exit=Math.min(exit,far);
            if(enter+1.0E-7>=exit)return 1;
        }
        return exit<=0 || enter>=1?1:Math.clamp(enter-1.0E-7,0,1);
    }
    public static double clearFraction(DoublePredicate isSafe) {
        if (!isSafe.test(0)) return 0;
        if (isSafe.test(1)) return 1;
        double clear=0, blocked=1;
        for(int i=0;i<18;i++) {
            double middle=(clear+blocked)*.5;
            if(isSafe.test(middle)) clear=middle; else blocked=middle;
        }
        return clear;
    }
}
