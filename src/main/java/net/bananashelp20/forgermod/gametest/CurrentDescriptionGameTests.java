package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AbilityDescriptions;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Optional;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public final class CurrentDescriptionGameTests {
    @GameTest(template="riftfang_test")
    public static void deepWoundHardCapAndPacketCooldownRespectReviewedRanks(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);
        String id="tooltips.forgermod.ability.deep_wound";
        try {
            var fresh=new ItemStack(ModItems.ASSASSIN_DAGGER.get());
            test.assertTrue(net.bananashelp20.forgermod.augmentation.Augmentations.setLevel(fresh,id,4).isEmpty(),"Deep Wound exceeds III");
            var template=new ItemStack(ModItems.SUPER_AUGMENT_TEMPLATE.get());
            test.assertTrue(net.bananashelp20.forgermod.augmentation.Augmentations.maxRank(template,id)==3,"Template bypasses hard cap");
            for(int rank=1;rank<=3;rank++) {
                var stack=net.bananashelp20.forgermod.augmentation.Augmentations.setLevel(fresh,id,rank);
                player.setData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS,java.util.Map.of());
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);
                AbilityTestPackets.use(player,0);
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
                long expected=player.level().getGameTime()+net.bananashelp20.forgermod.augmentation.ReviewedRanks.cooldown(id,rank);
                test.assertTrue(player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS).containsValue(expected),"Packet/cancellation used old cooldown at "+rank);
            }
        } finally {
            net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
            player.server.getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void reviewedMagmaTossResolvesOnlyCurrentRankAndKeepsWhiteFullDuration(GameTestHelper test) {
        var stack = new ItemStack(ModItems.MOLTEN_AXE.get());
        var description = AbilityDescriptions.description(stack,AdditionalAbilities.MAGMA_TOSS,3,false);
        String text = description.getString();
        test.assertTrue(text.contains("3 blocks") && text.contains("5 seconds"), "Wrong current-rank values: " + text);
        test.assertTrue(net.bananashelp20.forgermod.augmentation.RecommendedAbilities.cooldown(AdditionalAbilities.MAGMA_TOSS,3)==500,"Wrong separately displayed cooldown");
        test.assertFalse(text.contains("/") || text.contains("%s"), "Unresolved rank list or argument");
        var white = new StringBuilder();
        description.visit((style,part) -> {
            if (style.getColor()!=null && style.getColor().getValue()==ChatFormatting.WHITE.getColor()) white.append(part);
            return Optional.<Void>empty();
        },Style.EMPTY);
        test.assertTrue(white.toString().contains("3 blocks") && white.toString().contains("5 seconds"), "Rank values lost their white style: " + white);
        test.assertTrue(description.getStyle().getColor().getValue()==ChatFormatting.GRAY.getColor(), "Prose must remain gray");
        test.succeed();
    }
}
