package net.bananashelp20.forgermod.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AugmentCommandGameTests {
    @GameTest(template="riftfang_test")
    public static void commandAugmentsCompatibleTargetsAndSkipsOthers(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var first=EntityType.ZOMBIE.create(test.getLevel());
        var second=EntityType.ZOMBIE.create(test.getLevel());
        var incompatible=EntityType.ZOMBIE.create(test.getLevel());
        first.setPos(player.position().add(1,0,0));
        second.setPos(player.position().add(-1,0,0));
        incompatible.setPos(player.position().add(0,0,2));
        for(var zombie:new net.minecraft.world.entity.monster.Zombie[]{first,second,incompatible}) {
            zombie.setNoAi(true);zombie.setNoGravity(true);test.getLevel().addFreshEntity(zombie);
        }
        first.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.EMBERFANG_DAGGER.get()));
        second.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.INFERNAL_CLAYMORE.get()));
        incompatible.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.RIFTFANG_DAGGER.get()));
        try {
            var dispatcher=test.getLevel().getServer().getCommands().getDispatcher();
            var source=player.createCommandSourceStack().withPermission(4);
            test.assertTrue(dispatcher.execute("augment @e[type=minecraft:zombie,distance=..3] forgermod:flame_breath 2",source)==2,
                    "Command did not return the number of compatible targets");
            test.assertTrue(Augmentations.level(first.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==2
                    && Augmentations.level(second.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==2
                    && Augmentations.level(incompatible.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==0,
                    "Multi-target command changed the wrong held items");
        } finally {
            first.discard();second.discard();incompatible.discard();test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void commandSetsExactRankAndSuggestsRegisteredAugments(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);
        try {
            var dispatcher=test.getLevel().getServer().getCommands().getDispatcher();
            var source=player.createCommandSourceStack().withPermission(4);
            test.assertTrue(dispatcher.getRoot().getChild("augment")!=null,"/augment was not registered");
            test.assertFalse(dispatcher.getRoot().getChild("augment").canUse(source.withPermission(0)),"/augment is available without operator permission");
            var suggestions=dispatcher.getCompletionSuggestions(dispatcher.parse("augment @s forgermod:fl",source)).get();
            test.assertTrue(suggestions.getList().stream().anyMatch(s->s.getText().equals("forgermod:flame_breath")),"Flame Breath is missing from command completion");

            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.EMBERFANG_DAGGER.get()));
            test.assertTrue(dispatcher.execute("augment @s forgermod:flame_breath 3",source)==1,"Command did not affect its target");
            test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==3,"Command did not set rank III");
            test.assertTrue(dispatcher.execute("augment @s flame_breath 2",source)==1,"Unqualified augment name was rejected");
            test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==2,"Command did not set the exact lower rank");
            test.assertTrue(dispatcher.execute("augment @s forgermod:flame_breath",source)==1,"Default rank was rejected");
            test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==1,"Default rank is not I");
            test.assertTrue(Augmentations.count(player.getMainHandItem())==0,"Command consumed an Augmentation Table upgrade");
            test.assertTrue(dispatcher.execute("augment @s forgermod:dual_wield",source)==1,"Dual Wield command name was rejected");
            test.assertTrue(Augmentations.level(player.getMainHandItem(),Augmentations.DUAL_WIELD)==1,"Dual Wield was not learned");
        } finally {test.getLevel().getServer().getPlayerList().remove(player);}
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void commandRespectsCompatibilityCapsAndTemplates(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);
        try {
            var dispatcher=test.getLevel().getServer().getCommands().getDispatcher();
            var source=player.createCommandSourceStack().withPermission(4);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.RIFTFANG_DAGGER.get()));
            expectFailure(test,()->dispatcher.execute("augment @s forgermod:flame_breath 1",source),"Incompatible material accepted Flame Breath");
            test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==0,"Incompatible command changed the weapon");
            expectFailure(test,()->dispatcher.execute("augment @s forgermod:not_an_augment",source),"Unknown augment was accepted");

            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.SKYBREAKER_AXE.get()));
            test.assertTrue(dispatcher.execute("augment @s forgermod:windcharge 7",source)==1,"Skybreaker super IV was rejected");
            test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.WINDCHARGE)==7,"Skybreaker super IV rank was lost");
            expectFailure(test,()->dispatcher.execute("augment @s forgermod:windcharge 8",source),"Rank above Skybreaker cap was accepted");

            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.AUGMENT_TEMPLATE.get()));
            test.assertTrue(dispatcher.execute("augment @s forgermod:flame_breath 4",source)==1,"Augment Template was rejected");
            test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==4,"Template did not store the requested rank");

            ItemStack full=new ItemStack(ModItems.AUGMENT_TEMPLATE.get());
            var active=Augmentations.catalog(full).stream().filter(Augmentations.Ability::active).toList();
            for(int index=0;index<Augmentations.limit(full,true);index++) full=Augmentations.apply(full,active.get(index).id());
            String fourth=active.get(Augmentations.limit(full,true)).id();
            player.setItemInHand(InteractionHand.MAIN_HAND,full);
            String command="augment @s forgermod:"+fourth.substring(fourth.lastIndexOf('.')+1);
            expectFailure(test,()->dispatcher.execute(command,source),"Full active slots accepted another augment");
            test.assertTrue(Augmentations.ids(player.getMainHandItem(),true).size()==Augmentations.limit(full,true),"Failed command changed full slots");
        } finally {test.getLevel().getServer().getPlayerList().remove(player);}
        test.succeed();
    }

    private static void expectFailure(GameTestHelper test, ThrowingCommand command, String message) throws Exception {
        try {command.run();test.fail(message);} catch (CommandSyntaxException expected) { /* Expected command rejection. */ }
    }

    @FunctionalInterface
    private interface ThrowingCommand { void run() throws CommandSyntaxException; }
}
