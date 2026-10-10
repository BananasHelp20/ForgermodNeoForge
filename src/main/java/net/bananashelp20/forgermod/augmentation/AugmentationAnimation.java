package net.bananashelp20.forgermod.augmentation;

/** Shared server timing and client pixel geometry for the authored overlays. */
public final class AugmentationAnimation {
    public static final int IDLE=0, ANALYZE=1, COVER=2, EXPAND=3, FINALIZE=4, CHOOSE=5, COMPLETE=6;
    public static final int[] WIDTH={0,51,94,156,156};
    public static final int[] HEIGHT={0,30,30,58,58};
    public static final int[] X={0,63,41,10,10};
    public static final int[] Y={0,27,27,13,13};
    private static final int[] SPEED={0,1,2,3,1};
    private AugmentationAnimation() {}
    public record InwardReveal(int leftWidth,int rightStart,int rightWidth) {}
    public static InwardReveal inwardReveal(int stage,int pixels) {
        int width=WIDTH[stage],extra=width%2;
        // The odd-width analyze texture has one extra column outside the right edge.
        // Advance both fronts around the weapon opening, revealing that column with the right edge.
        int front=Math.clamp(pixels,0,width/2);
        return new InwardReveal(front,width-extra-front,front==0?0:front+extra);
    }
    public static int stageTicks(int stage,int analysisTicks) {
        if(stage<ANALYZE || stage>FINALIZE) return 0;
        return (int)Math.max(1,((long)((WIDTH[stage]+1)/2)*analysisTicks+26L*SPEED[stage]-1)/(26L*SPEED[stage]));
    }
    public static int revealedPixels(int stage,int progress,int analysisTicks) {
        if(stage<ANALYZE || stage>FINALIZE) return 0;
        int half=(WIDTH[stage]+1)/2;
        if(progress>=stageTicks(stage,analysisTicks)) return half;
        return (int)Math.clamp((long)Math.max(0,progress)*26*SPEED[stage]/Math.max(1,analysisTicks),0,half);
    }
}
