package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.PhotosynthesisEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PhotosynthesisGameTests {
    private static void ticks(net.minecraft.server.level.ServerPlayer player,int ticks) {for(int i=0;i<ticks;i++) PhotosynthesisEvents.tick(new PlayerTickEvent.Pre(player));}
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsRepairOnlyLearnedHeldWeaponDuringDay(GameTestHelper test) {
        var player=TestPlayers.survival(test);long oldTime=test.getLevel().getDayTime();test.getLevel().setDayTime(1000);test.getLevel().updateSkyBrightness();int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof LushWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.PHOTOSYNTHESIS))==eligible,"Wrong Photosynthesis pool");if(!eligible) continue;count++;
            PhotosynthesisEvents.end(player.getUUID());fresh.setDamageValue(10);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);ticks(player,300);test.assertTrue(fresh.getDamageValue()==10,"Fresh weapon repairs");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.PHOTOSYNTHESIS);player.setItemInHand(InteractionHand.MAIN_HAND,stack);ticks(player,299);test.assertTrue(stack.getDamageValue()==10,"Repairs before interval");ticks(player,1);test.assertTrue(stack.getDamageValue()==9 && Augmentations.hasPassive(stack,AdditionalAbilities.PHOTOSYNTHESIS),"Durability or augmentation data damaged");
            ticks(player,3000);test.assertTrue(stack.getDamageValue()==0,"Repairs below zero");stack.setDamageValue(3);test.getLevel().setDayTime(18000);test.getLevel().updateSkyBrightness();ticks(player,300);test.assertTrue(stack.getDamageValue()==3,"Night repairs");
            test.getLevel().setDayTime(1000);test.getLevel().updateSkyBrightness();ticks(player,200);var replacement=stack.copy();player.setItemInHand(InteractionHand.MAIN_HAND,replacement);ticks(player,100);test.assertTrue(replacement.getDamageValue()==3,"Switch retains repair timer");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,replacement);ticks(player,300);test.assertTrue(replacement.getDamageValue()==3,"Offhand repairs");
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            String id=AdditionalAbilities.PHOTOSYNTHESIS;
            for(int rank=1;rank<=4;rank++) {
                PhotosynthesisEvents.end(player.getUUID());
                stack=Augmentations.setLevel(new ItemStack(entry.get()),id,rank);int restored=new int[]{1,2,3,5}[rank-1];
                test.assertTrue(!stack.isEmpty() && Augmentations.level(stack,id)==rank,"Cannot learn Photosynthesis rank");
                stack.setDamageValue(20);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                ticks(player,299);test.assertTrue(stack.getDamageValue()==20,"Rank repairs before15seconds");
                ticks(player,1);test.assertTrue(stack.getDamageValue()==20-restored,"Wrong rank repair amount");
                stack.setDamageValue(1);ticks(player,300);test.assertTrue(stack.getDamageValue()==0,"Rank repair goes below zero");
                for(boolean compact:new boolean[]{false,true}) {
                    var text=AbilityDescriptions.description(stack,id,rank,compact);var white=new StringBuilder();
                    text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                    test.assertTrue(text.getString().equals("Restore "+restored+" weapon durability every 15 seconds in daylight.") && white.toString().equals(restored+" weapon durability15 seconds"),"Wrong description or highlights");
                }
                var changes=AugmentUpgradeDescriptions.changes(stack,id,rank);
                test.assertTrue(rank==4?changes.isEmpty():changes.size()==1 && changes.getFirst().property().equals("durability_restored") && ((Number)changes.getFirst().after()).intValue()==new int[]{2,3,5}[rank-1],"Wrong upgrade preview");
            }
            test.assertTrue(Augmentations.apply(stack,id).isEmpty() && !Augmentations.hasSuperForm(stack,id),"Photosynthesis exceeds IV or becomes super");
        }} finally {PhotosynthesisEvents.end(player.getUUID());test.getLevel().setDayTime(oldTime);test.getLevel().updateSkyBrightness();player.server.getPlayerList().remove(player);}
        for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get())) {
            var template=new ItemStack(item);String id=AdditionalAbilities.PHOTOSYNTHESIS;
            test.assertTrue(Augmentations.maxRank(template,id)==4 && !Augmentations.setLevel(template,id,4).isEmpty() && Augmentations.setLevel(template,id,5).isEmpty(),"Template bypasses IV cap");
        }
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
