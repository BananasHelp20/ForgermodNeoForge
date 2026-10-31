package net.bananashelp20.forgermod.screen.custom;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import java.util.ArrayList;
import java.util.List;

/** Presentation only: dark ink on pale cards, full original components retained for hover tooltips. */
public final class AugmentationCardText {
    public static final int BODY=0x737373,HIGHLIGHT=0x40576A,JADE=0x25734E,DISABLED=0x777777;
    public record Layout(List<FormattedCharSequence> lines,float scale,boolean truncated) {}
    private AugmentationCardText() {}
    public static int ink(Integer color,boolean disabled) {
        if(disabled)return DISABLED;
        if(color==null || color==0xAAAAAA || color==0x555555)return BODY;
        if(color==0xFFFFFF)return HIGHLIGHT;
        if(color==0x55FF55 || color==0x00AA00)return JADE;
        return color;
    }
    public static FormattedCharSequence readable(FormattedCharSequence line,boolean disabled) {
        return sink->line.accept((index,style,codePoint)->sink.accept(index,style.withColor(ink(style.getColor()==null?null:style.getColor().getValue(),disabled)),codePoint));
    }
    public static Layout offer(Font font,net.minecraft.world.item.ItemStack stack,String id,int rank,boolean highlight) {
        var description=net.bananashelp20.forgermod.augmentation.AugmentUpgradeDescriptions.description(stack,id,rank,true,highlight);
        var full=fit(font,description,37,36,.5F,.45F);
        if(!full.truncated() || rank==0)return full;
        var changes=net.bananashelp20.forgermod.augmentation.AugmentUpgradeDescriptions.changes(stack,id,rank);
        for(int shown=changes.size()-1;shown>=1;shown--) {
            var preview=Component.empty();
            for(int i=0;i<shown;i++){if(i>0)preview.append(" ");preview.append(changes.get(i).sentence(highlight));}
            preview.append(" ").append(Component.translatableWithFallback("augmentation.forgermod.card.more_changes","+%s more changes",changes.size()-shown));
            var layout=fit(font,preview,37,36,.5F,.45F);if(!layout.truncated())return layout;
        }
        return full;
    }
    public static Layout fit(Font font,Component text,int width,int height,float largest,float smallest) {
        float scale=largest;
        var lines=font.split(text,(int)Math.floor(width/scale));
        while(lines.size()*font.lineHeight*scale>height && scale>smallest) {
            scale=Math.max(smallest,scale-.025F);
            lines=font.split(text,(int)Math.floor(width/scale));
        }
        int count=Math.max(1,(int)Math.floor(height/(font.lineHeight*scale)));
        boolean clipped=lines.size()>count;
        if(clipped) {
            var kept=new ArrayList<>(lines.subList(0,count));
            var dots=FormattedCharSequence.forward("...",Style.EMPTY);
            int limit=(int)Math.floor(width/scale)-font.width(dots);
            var glyphs=new ArrayList<FormattedCharSequence>();int[] used={0};
            kept.getLast().accept((index,style,codePoint)->{
                var glyph=FormattedCharSequence.codepoint(codePoint,style);int advance=font.width(glyph);
                if(used[0]+advance>limit)return false;
                used[0]+=advance;glyphs.add(glyph);return true;
            });
            glyphs.add(dots);kept.set(kept.size()-1,FormattedCharSequence.composite(glyphs));lines=kept;
        }
        return new Layout(List.copyOf(lines),scale,clipped);
    }
}
