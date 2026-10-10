package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.AugmentationAnimation;

public final class AugmentationAnimationTest {
    public static void main(String[] args) throws Exception {
        // All revealed widths are whole texture pixels; the last front reaches every column.
        for(int base:new int[]{100,180,500,10000}) for(int stage=1;stage<=4;stage++) {
            int ticks=AugmentationAnimation.stageTicks(stage,base),last=0;
            for(int tick=0;tick<=ticks;tick++) {
                int pixels=AugmentationAnimation.revealedPixels(stage,tick,base);
                if(pixels<last || pixels>(AugmentationAnimation.WIDTH[stage]+1)/2) throw new AssertionError("Invalid reveal front");
                if(stage<=2) {
                    var reveal=AugmentationAnimation.inwardReveal(stage,pixels);
                    int center=stage==1?25:47;
                    if(center-reveal.leftWidth()!=reveal.rightStart()-center)
                        throw new AssertionError("Inward fronts are not symmetric about the weapon opening");
                    if(pixels==0 && (reveal.leftWidth()!=0 || reveal.rightWidth()!=0))
                        throw new AssertionError("Overlay appears before progress starts");
                    if(pixels>0 && reveal.rightStart()+reveal.rightWidth()!=AugmentationAnimation.WIDTH[stage])
                        throw new AssertionError("Right outer texture column was lost");
                    if(tick==ticks && reveal.leftWidth()!=reveal.rightStart())
                        throw new AssertionError("Completed inward reveal leaves a center gap");
                }
                last=pixels;
            }
            if(last!=(AugmentationAnimation.WIDTH[stage]+1)/2) throw new AssertionError("Overlay incomplete");
            if(ticks>32767) throw new AssertionError("Progress exceeds menu synchronization range");
        }
        if(AugmentationAnimation.stageTicks(1,100)!=100 || AugmentationAnimation.stageTicks(2,100)!=91
                || AugmentationAnimation.stageTicks(3,100)!=100 || AugmentationAnimation.stageTicks(4,100)!=300)
            throw new AssertionError("Incorrect 1x/2x/3x/1x pixel speeds");
        for(int tick:new int[]{0,10,25,50}) {
            int one=AugmentationAnimation.revealedPixels(1,tick,100);
            int two=AugmentationAnimation.revealedPixels(2,tick,100);
            int three=AugmentationAnimation.revealedPixels(3,tick,100);
            if(two<2*one || three<3*one) throw new AssertionError("Speed ratio is wrong");
        }
        // Every transparent central hole aligns with the authored weapon-slot frame (79,33).
        if(AugmentationAnimation.X[1]+16!=79 || AugmentationAnimation.Y[1]+6!=33
                || AugmentationAnimation.X[2]+33!=74 || AugmentationAnimation.Y[2]+1!=28
                || AugmentationAnimation.X[3]+69!=79 || AugmentationAnimation.Y[3]+20!=33
                || AugmentationAnimation.X[4]+69!=79 || AugmentationAnimation.Y[4]+20!=33)
            throw new AssertionError("Transparent slot alignment mismatch");
        String[] files={"augmentation_table_analyze_progress.png","augmentation_table_analyze_to_selection_transtion_1.png",
                "augmentation_table_analyze_to_selection_transition_2.png","augmentation_table_selection_finalisation.png"};
        for(int stage=1;stage<=4;stage++) {
            try(var resource=AugmentationAnimationTest.class.getResourceAsStream("/assets/forgermod/textures/gui/augementation_table/"+files[stage-1])) {
                if(resource==null) throw new AssertionError("Overlay not packaged");
                var image=javax.imageio.ImageIO.read(resource);
                if(image.getWidth()!=AugmentationAnimation.WIDTH[stage] || image.getHeight()!=AugmentationAnimation.HEIGHT[stage])
                    throw new AssertionError("Actual texture geometry differs from reveal geometry");
                int minX=image.getWidth(),minY=image.getHeight(),maxX=-1,maxY=-1;
                for(int y=0;y<image.getHeight();y++) for(int x=0;x<image.getWidth();x++) if((image.getRGB(x,y)>>>24)==0) {
                    minX=Math.min(minX,x); maxX=Math.max(maxX,x); minY=Math.min(minY,y); maxY=Math.max(maxY,y);
                }
                int expectedX=stage==2?74:79,expectedY=stage==2?28:33,size=stage==2?28:18;
                if(minX+AugmentationAnimation.X[stage]!=expectedX || minY+AugmentationAnimation.Y[stage]!=expectedY
                        || maxX-minX+1!=size || maxY-minY+1!=size) throw new AssertionError("Actual overlay hole does not align with slot");
            }
        }
    }
}
