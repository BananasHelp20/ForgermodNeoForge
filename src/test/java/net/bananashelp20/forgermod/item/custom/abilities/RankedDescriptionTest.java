package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.RankedDescription;
import java.util.List;

public final class RankedDescriptionTest {
    public static void main(String[] args) {
        var text = RankedDescription.resolve("Radius 2/2.5/3/3.5 blocks; damage 30%; lasts 10 seconds.", 3);
        if (!text.values().equals(List.of("3 blocks", "30%", "10 seconds"))) throw new AssertionError(text);
        if (!text.format().equals("Radius %s; damage %s; lasts %s.")) throw new AssertionError(text);
        var superText = RankedDescription.resolve("Radius 3/4/5/6 blocks (7 at super rank).", 5);
        if (!superText.values().contains("7 blocks")) throw new AssertionError(superText);
        var one = RankedDescription.resolve("Restore 2 health; 50% less damage.", 1);
        if (one.values().size() != 2 || one.format().contains("50")) throw new AssertionError(one);
        if (!RankedDescription.resolve("No numbers here.", 4).values().isEmpty()) throw new AssertionError();
        if (!RankedDescription.resolve("Duration: 3/4 seconds.", 99).values().equals(List.of("4 seconds"))) throw new AssertionError();
    }
}
