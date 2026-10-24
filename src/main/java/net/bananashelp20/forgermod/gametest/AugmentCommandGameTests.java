package net.bananashelp20.forgermod.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class AugmentCommandGameTests {
    @GameTest(template="riftfang_test")
    public static void newAndUpgradeSkipIncompatibleTargetsAndReturnChangedCount(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var first=EntityType.ZOMBIE.create(test.getLevel());var second=EntityType.ZOMBIE.create(test.getLevel());var incompatible=EntityType.ZOMBIE.create(test.getLevel());
        first.setPos(player.position().add(1,0,0));second.setPos(player.position().add(-1,0,0));incompatible.setPos(player.position().add(0,0,2));
        for(var mob:java.util.List.of(first,second,incompatible)){mob.setNoAi(true);mob.setNoGravity(true);test.getLevel().addFreshEntity(mob);}
        first.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.EMBERFANG_DAGGER.get()));second.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.INFERNAL_CLAYMORE.get()));incompatible.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.RIFTFANG_DAGGER.get()));
        try{
            var dispatcher=player.server.getCommands().getDispatcher();var source=player.createCommandSourceStack().withPermission(4);
            String targets="@e[type=minecraft:zombie,distance=..3]";
            test.assertTrue(dispatcher.execute("augment new "+targets+" forgermod:flame_breath",source)==2,"New changed count/compatibility");
            test.assertTrue(dispatcher.execute("augment upgrade "+targets+" flame_breath",source)==2,"Upgrade changed count/compatibility");
            test.assertTrue(Augmentations.level(first.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==2 && Augmentations.level(second.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==2 && Augmentations.level(incompatible.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==0,"Mixed-target mutation is incorrect");
        }finally{first.discard();second.discard();incompatible.discard();player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void subcommandsLearnIncrementPreserveStateAndCompleteOnlyUsableNames(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);
        try{
            var dispatcher=player.server.getCommands().getDispatcher();var source=player.createCommandSourceStack().withPermission(4);var root=dispatcher.getRoot().getChild("augment");
            test.assertTrue(root!=null && root.getChild("new")!=null && root.getChild("upgrade")!=null && root.getChild("super")!=null,"Subcommands missing");
            test.assertFalse(root.canUse(source.withPermission(0)),"Nonoperator can augment");
            var stack=new ItemStack(ModItems.EMBERFANG_DAGGER.get());stack.setDamageValue(5);stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My dagger"));player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var suggestions=dispatcher.getCompletionSuggestions(dispatcher.parse("augment new @s forgermod:fl",source)).get();
            test.assertTrue(suggestions.getList().stream().anyMatch(s->s.getText().equals("forgermod:flame_breath")),"New completion omits compatible augment");
            test.assertTrue(dispatcher.execute("augment new @s \"Flame Breath\"",source)==1 && Augmentations.level(player.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==1,"Readable name/new rank I rejected");
            var before=player.getMainHandItem().copy();expectFailure(test,()->dispatcher.execute("augment new @s flame_breath",source),"New overwrites applied augment");test.assertTrue(ItemStack.matches(before,player.getMainHandItem()),"Rejected duplicate mutated item");
            for(int rank=2;rank<=4;rank++){dispatcher.execute("augment upgrade @s forgermod:flame_breath",source);test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==rank,"Upgrade does not increment one rank");}
            before=player.getMainHandItem().copy();expectFailure(test,()->dispatcher.execute("augment upgrade @s flame_breath",source),"Upgrade exceeds cap");test.assertTrue(ItemStack.matches(before,player.getMainHandItem()),"Capped upgrade mutated item");
            suggestions=dispatcher.getCompletionSuggestions(dispatcher.parse("augment upgrade @s forgermod:fl",source)).get();test.assertTrue(suggestions.getList().stream().noneMatch(s->s.getText().equals("forgermod:flame_breath")),"Upgrade completion includes capped ability");
            test.assertTrue(player.getMainHandItem().getDamageValue()==5 && player.getMainHandItem().getHoverName().getString().equals("My dagger") && Augmentations.count(player.getMainHandItem())==0,"Command changes durability/name/table count");
            dispatcher.execute("augment new @s dual_wield",source);test.assertTrue(Augmentations.level(player.getMainHandItem(),Augmentations.DUAL_WIELD)==1,"Passive new rejected");
            expectFailure(test,()->dispatcher.execute("augment upgrade @s fire_resistance",source),"Upgrade learns a missing ability");
            expectFailure(test,()->dispatcher.execute("augment new @s flame_breath 3",source),"Obsolete rank argument accepted");
        }finally{player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void everyCompatibleSuperFormCanBeAppliedDirectlyAndUpgradedWithoutDowngrading(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);int checked=0;
        try{
            var dispatcher=player.server.getCommands().getDispatcher();var source=player.createCommandSourceStack().withPermission(4);
            for(var entry:ModItems.ITEMS.getEntries()){
                var fresh=new ItemStack(entry.get());if(!Augmentations.eligible(fresh))continue;
                for(var ability:Augmentations.catalog(fresh)){
                    if(!Augmentations.hasSuperForm(fresh,ability.id()))continue;checked++;player.setItemInHand(InteractionHand.MAIN_HAND,fresh.copy());
                    String key=Augmentations.superName(fresh,ability.id());String alias=key.substring(0,key.length()-5);alias=alias.substring(alias.lastIndexOf('.')+1);
                    String superName=alias.replace('_',' ');
                    test.assertTrue(dispatcher.execute("augment super @s "+superName,source)==1,"Cannot directly apply super "+alias);
                    int start=Augmentations.superStart(ability.id());test.assertTrue(Augmentations.level(player.getMainHandItem(),ability.id())==start && Augmentations.displayRank(player.getMainHandItem(),ability.id(),start)==1,"Super starts at wrong stored/display rank");
                    var before=player.getMainHandItem().copy();String command="augment super @s "+alias;expectFailure(test,()->dispatcher.execute(command,source),"Super recast can overwrite an existing super");test.assertTrue(ItemStack.matches(before,player.getMainHandItem()),"Rejected super mutates weapon");
                    for(int rank=start+1;rank<=Augmentations.maxRank(fresh,ability.id());rank++)dispatcher.execute("augment upgrade @s "+alias,source);
                    test.assertTrue(Augmentations.level(player.getMainHandItem(),ability.id())==Augmentations.maxRank(fresh,ability.id()),"Cannot upgrade super to its max");
                }
            }
            test.assertTrue(checked>=40,"Skipped registered super forms/variants");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()));
            var suggestions=dispatcher.getCompletionSuggestions(dispatcher.parse("augment super @s forgermod:sta",source)).get();test.assertTrue(suggestions.getList().stream().anyMatch(s->s.getText().equals("forgermod:static_field")),"Super completion does not suggest super-form names");
            dispatcher.execute("augment new @s direct_current",source);dispatcher.execute("augment super @s direct_current",source);test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.DIRECT_CURRENT)==4,"Super command cannot transform a learned base");
        }finally{player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void compatibilityPrerequisitesSlotLimitsAndTemplatesRemainEnforced(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);
        try{
            var dispatcher=player.server.getCommands().getDispatcher();var source=player.createCommandSourceStack().withPermission(4);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.RIFTFANG_DAGGER.get()));
            expectFailure(test,()->dispatcher.execute("augment new @s flame_breath",source),"Incompatible material accepted");expectFailure(test,()->dispatcher.execute("augment new @s not_an_augment",source),"Unknown accepted");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.DEAD_CALM_DAGGER.get()));expectFailure(test,()->dispatcher.execute("augment super @s skybreaker",source),"Dagger accepts axe-only super");dispatcher.execute("augment new @s windcharge",source);expectFailure(test,()->dispatcher.execute("augment upgrade @s skybreaker",source),"Axe-only super alias upgrades an incompatible dagger ability");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.MOLTEN_AXE.get()));expectFailure(test,()->dispatcher.execute("augment new @s flaming_stomp",source),"Prerequisite bypassed");dispatcher.execute("augment new @s vertical_strike",source);dispatcher.execute("augment new @s flaming_stomp",source);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.AUGMENT_TEMPLATE.get()));dispatcher.execute("augment new @s flame_breath",source);dispatcher.execute("augment upgrade @s flame_breath",source);test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==2,"Template support lost");
            ItemStack full=new ItemStack(ModItems.AUGMENT_TEMPLATE.get());var active=Augmentations.catalog(full).stream().filter(a->a.active() && !Augmentations.requiresVerticalStrike(a.id())).toList();
            for(int i=0;i<Augmentations.limit(full,true);i++)full=Augmentations.apply(full,active.get(i).id());String id=active.get(Augmentations.limit(full,true)).id();player.setItemInHand(InteractionHand.MAIN_HAND,full);
            String command="augment new @s "+id.substring(id.lastIndexOf('.')+1);expectFailure(test,()->dispatcher.execute(command,source),"Full slots accepted another active");
            test.assertTrue(Augmentations.ids(player.getMainHandItem(),true).size()==Augmentations.limit(full,true),"Failed command changes slot count");
        }finally{player.server.getPlayerList().remove(player);}test.succeed();
    }
    private static void expectFailure(GameTestHelper test,ThrowingCommand command,String message)throws Exception{
        try{command.run();test.fail(message);}catch(CommandSyntaxException expected){}
    }
    @FunctionalInterface private interface ThrowingCommand{void run()throws CommandSyntaxException;}
}
