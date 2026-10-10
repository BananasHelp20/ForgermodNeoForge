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
    public static void tenVariantsGainExactlyTwentyPercentOnlyInNether(GameTestHelper test) {
        var overworldPlayer=TestPlayers.survival(test);
        var nether=test.getLevel().getServer().getLevel(Level.NETHER);
        test.assertTrue(nether!=null,"Test server has no Nether dimension");
        var cookie=CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"nether-combat-test"),false);
        var player=new ServerPlayer(test.getLevel().getServer(),nether,cookie.gameProfile(),cookie.clientInformation());
        for(Item item:new Item[]{ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),ModItems.INFERNAL_CLAYMORE_AMBER.get(),
                ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get(),ModItems.MOLTEN_AXE.get(),ModItems.MOLTEN_AXE_RUBY.get(),
                ModItems.MOLTEN_AXE_AMBER.get(),ModItems.MOLTEN_AXE_AMETHYST.get(),ModItems.MOLTEN_AXE_JADE.get()}) {
            var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.NETHER_BORN);
            test.assertFalse(stack.isEmpty(),"Nether Born cannot be learned");
            test.assertTrue(Augmentations.apply(stack,RecommendedAbilities.NETHER_BORN).isEmpty(),"Fixed 20% passive offers meaningless extra upgrades");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var cow=EntityType.COW.create(nether);
            cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20); cow.setHealth(20);
            cow.hurt(player.damageSources().playerAttack(player),10);
            test.assertTrue(cow.getHealth()==8,"Nether melee bonus is not exactly 20%");
            cow.setHealth(20); cow.invulnerableTime=0; cow.hurt(player.damageSources().explosion(player,player),10);
            test.assertTrue(cow.getHealth()==8,"Weapon explosion does not receive Nether bonus exactly once");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
            cow.setHealth(20); cow.invulnerableTime=0; cow.hurt(player.damageSources().playerAttack(player),10);
            test.assertTrue(cow.getHealth()==10,"Fresh Nether weapon has passive damage bonus");
            overworldPlayer.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); var other=EntityType.COW.create(test.getLevel());
            other.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20); other.setHealth(20);
            other.hurt(overworldPlayer.damageSources().playerAttack(overworldPlayer),10);
            test.assertTrue(other.getHealth()==10,"Nether bonus leaks to Overworld");
        }
        test.getLevel().getServer().getPlayerList().remove(overworldPlayer); test.succeed();
    }
}
