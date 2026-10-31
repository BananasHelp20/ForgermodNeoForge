package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class NetherBornGameTests {
    @GameTest(template="riftfang_test")
    public static void tenVariantsScaleThroughFiveRanksOnlyInNether(GameTestHelper test) {
        var overworldPlayer=TestPlayers.survival(test);
        var nether=test.getLevel().getServer().getLevel(Level.NETHER);
        test.assertTrue(nether!=null,"Test server has no Nether dimension");
        var cookie=CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"nether-combat-test"),false);
        var player=new ServerPlayer(test.getLevel().getServer(),nether,cookie.gameProfile(),cookie.clientInformation());
        for(Item item:new Item[]{ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),ModItems.INFERNAL_CLAYMORE_AMBER.get(),
                ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get(),ModItems.MOLTEN_AXE.get(),ModItems.MOLTEN_AXE_RUBY.get(),
                ModItems.MOLTEN_AXE_AMBER.get(),ModItems.MOLTEN_AXE_AMETHYST.get(),ModItems.MOLTEN_AXE_JADE.get()}) {
            String id=RecommendedAbilities.NETHER_BORN;
            var stack=new ItemStack(item);
            for(int rank=1;rank<=5;rank++) {
            stack=Augmentations.apply(stack,id);
            test.assertTrue(!stack.isEmpty() && Augmentations.level(stack,id)==rank,"Cannot learn Nether Born rank "+rank);
            int percent=6*rank;
            for(boolean compact:new boolean[]{false,true}) {
                var description=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(stack,id,rank,compact);
                var white=new StringBuilder();
                description.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                test.assertTrue(description.getString().equals("Deal "+percent+"% more damage in the Nether.") && white.toString().equals(percent+"% more damage"),"Incorrect rank description or highlights");
            }
            var changes=net.bananashelp20.forgermod.augmentation.AugmentUpgradeDescriptions.changes(stack,id,rank);
            test.assertTrue(rank==5?changes.isEmpty():changes.size()==1 && changes.getFirst().property().equals("damage_bonus") && ((Number)changes.getFirst().after()).intValue()==percent+6,"Incorrect damage upgrade preview");
            test.assertFalse(stack.isEmpty(),"Nether Born cannot be learned");

            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var cow=EntityType.COW.create(nether);
            cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20); cow.setHealth(20);
            cow.hurt(player.damageSources().playerAttack(player),10);
            test.assertTrue(Math.abs(cow.getHealth()-(10-percent/10F))<.0001,"Wrong Nether melee bonus at rank "+rank);
            cow.setHealth(20); cow.invulnerableTime=0; cow.hurt(player.damageSources().explosion(player,player),10);
            test.assertTrue(Math.abs(cow.getHealth()-(10-percent/10F))<.0001,"Wrong Nether explosion bonus at rank "+rank);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
            cow.setHealth(20); cow.invulnerableTime=0; cow.hurt(player.damageSources().playerAttack(player),10);
            test.assertTrue(cow.getHealth()==10,"Fresh Nether weapon has passive damage bonus");
            overworldPlayer.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); var other=EntityType.COW.create(test.getLevel());
            other.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20); other.setHealth(20);
            other.hurt(overworldPlayer.damageSources().playerAttack(overworldPlayer),10);
            test.assertTrue(other.getHealth()==10,"Nether bonus leaks to Overworld");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            cow.setHealth(20);cow.invulnerableTime=0;cow.hurt(player.damageSources().indirectMagic(player,player),10);
            test.assertTrue(cow.getHealth()==10,"Nether bonus leaks to separate magic damage");
            cow.discard();other.discard();
            }
            test.assertTrue(Augmentations.apply(stack,id).isEmpty() && !Augmentations.hasSuperForm(stack,id),"Nether Born exceeds V or becomes super");
        }
        for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get())) {
            var template=new ItemStack(item);String id=RecommendedAbilities.NETHER_BORN;
            test.assertTrue(Augmentations.maxRank(template,id)==5,"Template bypasses V cap");
            test.assertTrue(!Augmentations.setLevel(template,id,5).isEmpty() && Augmentations.setLevel(template,id,6).isEmpty(),"Template rank bounds wrong");
        }
        test.getLevel().getServer().getPlayerList().remove(overworldPlayer); test.succeed();
    }
}
