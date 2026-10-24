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
    public static void unnamedUpgradeUpdatesBothHandsOncePreservesCapsAndSuperProgression(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);
        try{
            var dispatcher=player.server.getCommands().getDispatcher();var source=player.createCommandSourceStack().withPermission(4);
            var main=new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get());
            main=Augmentations.setLevel(main,AdditionalAbilities.DIRECT_CURRENT,3);
            main=Augmentations.setLevel(main,AdditionalAbilities.LIGHTNING_ROD,1);
            main.setDamageValue(5);main.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Preserved name"));
            var off=new ItemStack(ModItems.DEAD_CALM_DAGGER.get());
            off=Augmentations.setLevel(off,Augmentations.DUAL_WIELD,Augmentations.maxRank(off,Augmentations.DUAL_WIELD));
            off=Augmentations.setLevel(off,AdditionalAbilities.WINDCHARGE,1);
            var beforeMain=main.copy();var beforeOff=off.copy();
            player.setItemInHand(InteractionHand.MAIN_HAND,main);player.setItemInHand(InteractionHand.OFF_HAND,off);
            test.assertTrue(dispatcher.execute("augment upgrade @s",source)==1,"Bulk command counts hands instead of targets");
            test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.DIRECT_CURRENT)==4 && Augmentations.isSuper(player.getMainHandItem(),AdditionalAbilities.DIRECT_CURRENT,4),"Bulk upgrade does not advance into super form");
            for(var hand:InteractionHand.values()){
                var before=hand==InteractionHand.MAIN_HAND?beforeMain:beforeOff;var after=player.getItemInHand(hand);
                for(var ability:Augmentations.catalog(before)){
                    int rank=Augmentations.level(before,ability.id());int expected=rank==0?0:Math.min(rank+1,Augmentations.maxRank(before,ability.id()));
                    test.assertTrue(Augmentations.level(after,ability.id())==expected,"Bulk missed, added, capped incorrectly or double-upgraded "+hand+" "+ability.id());
                }
                test.assertTrue(Augmentations.count(before)==Augmentations.count(after),"Bulk upgrade changes table count");
            }
            test.assertTrue(player.getMainHandItem().getDamageValue()==5 && player.getMainHandItem().getHoverName().getString().equals("Preserved name"),"Bulk loses item components");
            dispatcher.execute("augment upgrade @s",source);test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.DIRECT_CURRENT)==5,"Repeated bulk command fails to upgrade super");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            dispatcher.execute("augment upgrade @s",source);test.assertTrue(Augmentations.level(player.getOffhandItem(),AdditionalAbilities.WINDCHARGE)==4,"Empty mainhand prevents offhand upgrade");
            player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.DEAD_CALM_DAGGER.get()));
            expectFailure(test,()->dispatcher.execute("augment upgrade @s",source),"Bulk learns abilities on a fresh weapon");
            test.assertTrue(Augmentations.ids(player.getOffhandItem(),true).isEmpty() && Augmentations.ids(player.getOffhandItem(),false).isEmpty(),"No-op learns augments");
            var capped=Augmentations.setLevel(new ItemStack(ModItems.DEAD_CALM_DAGGER.get()),AdditionalAbilities.WINDCHARGE,4);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.STICK));player.setItemInHand(InteractionHand.OFF_HAND,capped);
            expectFailure(test,()->dispatcher.execute("augment upgrade @s",source),"Bulk exceeds every learned augment's cap");
            test.assertTrue(ItemStack.matches(capped,player.getOffhandItem()),"Fully capped bulk attempt mutates item");
        }finally{player.server.getPlayerList().remove(player);}test.succeed();
    }

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
            incompatible.setItemInHand(InteractionHand.OFF_HAND,Augmentations.setLevel(new ItemStack(ModItems.DEAD_CALM_DAGGER.get()),AdditionalAbilities.WINDCHARGE,1));
            test.assertTrue(dispatcher.execute("augment upgrade "+targets,source)==3,"Bulk multi-target command skips offhand-only target or miscounts");
            test.assertTrue(Augmentations.level(first.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==3 && Augmentations.level(second.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==3 && Augmentations.level(incompatible.getOffhandItem(),AdditionalAbilities.WINDCHARGE)==2,"Bulk selector upgrades incorrect items");
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
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.MOLTEN_AXE.get()));dispatcher.execute("augment new @s flaming_stomp",source);test.assertTrue(Augmentations.level(player.getMainHandItem(),Augmentations.VERTICAL_STRIKE)==1,"Command did not learn Vertical Strike automatically");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.AUGMENT_TEMPLATE.get()));dispatcher.execute("augment new @s flame_breath",source);dispatcher.execute("augment upgrade @s flame_breath",source);test.assertTrue(Augmentations.level(player.getMainHandItem(),AdditionalAbilities.FLAME_BREATH)==2,"Template support lost");
            ItemStack full=new ItemStack(ModItems.AUGMENT_TEMPLATE.get());var active=Augmentations.catalog(full).stream().filter(a->a.active() && !Augmentations.requiresVerticalStrike(a.id())).toList();
            for(int i=0;i<Augmentations.limit(full,true);i++)full=Augmentations.apply(full,active.get(i).id());String id=active.get(Augmentations.limit(full,true)).id();player.setItemInHand(InteractionHand.MAIN_HAND,full);
            String command="augment new @s "+id.substring(id.lastIndexOf('.')+1);expectFailure(test,()->dispatcher.execute(command,source),"Full slots accepted another active");
            test.assertTrue(Augmentations.ids(player.getMainHandItem(),true).size()==Augmentations.limit(full,true),"Failed command changes slot count");
        }finally{player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void everyDependentVariantAutomaticallyLearnsMissingTechnique(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);int checked=0;
        try{
            var dispatcher=player.server.getCommands().getDispatcher();var source=player.createCommandSourceStack().withPermission(4);
            for(var entry:ModItems.ITEMS.getEntries()){
                var fresh=new ItemStack(entry.get());if(!Augmentations.eligible(fresh))continue;
                for(var ability:Augmentations.catalog(fresh)){
                    if(!Augmentations.requiresVerticalStrike(ability.id()))continue;checked++;
                    for(boolean known:new boolean[]{false,true}){
                        var input=known?Augmentations.setLevel(fresh,Augmentations.VERTICAL_STRIKE,1):fresh.copy();player.setItemInHand(InteractionHand.MAIN_HAND,input);
                        String suffix=ability.id().substring(ability.id().lastIndexOf('.')+1);
                        var suggestions=dispatcher.getCompletionSuggestions(dispatcher.parse("augment new @s forgermod:"+suffix.substring(0,suffix.length()-1),source)).get();
                        test.assertTrue(suggestions.getList().stream().anyMatch(v->v.getText().equals("forgermod:"+suffix)),"Auto-dependency choice is absent from completion: "+suffix);
                        test.assertTrue(ItemStack.matches(input,player.getMainHandItem()),"Completion adds a prerequisite before execution");
                        dispatcher.execute("augment new @s "+suffix,source);
                        test.assertTrue(Augmentations.level(player.getMainHandItem(),ability.id())==1 && Augmentations.level(player.getMainHandItem(),Augmentations.VERTICAL_STRIKE)==1,"Missing dependent/prerequisite on "+entry.getId());
                        test.assertTrue(Augmentations.ids(player.getMainHandItem(),false).stream().filter(Augmentations.VERTICAL_STRIKE::equals).count()==1,"Existing prerequisite duplicated");
                    }
                }
            }
            test.assertTrue(checked==15,"Skipped dependent augment gemstone variants: "+checked);
        }finally{player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void failedDependencyTransactionsRollbackAndLegacyUpgradesRestorePrerequisite(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);
        try{
            var dispatcher=player.server.getCommands().getDispatcher();var source=player.createCommandSourceStack().withPermission(4);
            ItemStack full=new ItemStack(ModItems.MOLTEN_AXE.get());
            for(String id:java.util.List.of(AdditionalAbilities.FLAME_BREATH,AdditionalAbilities.MAGMA_TOSS,RecommendedAbilities.CRUCIBLE_HOOK))full=Augmentations.setLevel(full,id,1);
            test.assertTrue(!full.isEmpty() && Augmentations.ids(full,true).size()==3,"Full active-slot fixture invalid");player.setItemInHand(InteractionHand.MAIN_HAND,full);var before=full.copy();
            expectFailure(test,()->dispatcher.execute("augment new @s flaming_stomp",source),"Dependent applied despite full active slots");
            test.assertTrue(ItemStack.matches(before,player.getMainHandItem()) && Augmentations.level(player.getMainHandItem(),Augmentations.VERTICAL_STRIKE)==0,"Failed active command partially adds prerequisite");
            ItemStack echo=new ItemStack(ModItems.ECHOING_AXE.get());
            var passives=Augmentations.catalog(echo).stream().filter(a->!a.active() && !a.id().equals(Augmentations.MATERIAL_HIT) && !a.id().equals(Augmentations.VERTICAL_STRIKE) && !Augmentations.requiresVerticalStrike(a.id())).toList();
            test.assertTrue(passives.size()>=4,"Passive-slot fixture incomplete");
            for(int i=0;i<4;i++)echo=Augmentations.setLevel(echo,passives.get(i).id(),1);
            player.setItemInHand(InteractionHand.MAIN_HAND,echo);before=echo.copy();
            expectFailure(test,()->dispatcher.execute("augment new @s echo_salvage",source),"One passive slot accepted two augments");
            test.assertTrue(ItemStack.matches(before,player.getMainHandItem()),"Partial prerequisite leaked when dependent had no remaining passive slot");
            var suggestions=dispatcher.getCompletionSuggestions(dispatcher.parse("augment new @s forgermod:echo_s",source)).get();test.assertTrue(suggestions.getList().stream().noneMatch(v->v.getText().equals("forgermod:echo_salvage")),"Impossible dependency pair appears in completion");
            var legacy=Augmentations.setLevel(new ItemStack(ModItems.NULLIFIED_AXE.get()),Augmentations.VERTICAL_STRIKE,1);legacy=Augmentations.setLevel(legacy,RecommendedAbilities.NULL_RECEIPT,1);
            var data=legacy.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();var levels=data.getCompound("forgermod_augments").getList("levels",net.minecraft.nbt.Tag.TAG_COMPOUND);
            for(int i=levels.size()-1;i>=0;i--)if(levels.getCompound(i).getString("id").equals(Augmentations.VERTICAL_STRIKE))levels.remove(i);
            legacy.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));player.setItemInHand(InteractionHand.MAIN_HAND,legacy);
            dispatcher.execute("augment upgrade @s null_receipt",source);test.assertTrue(Augmentations.level(player.getMainHandItem(),RecommendedAbilities.NULL_RECEIPT)==2 && Augmentations.level(player.getMainHandItem(),Augmentations.VERTICAL_STRIKE)==1,"Legacy upgrade failed to repair its missing prerequisite");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.AUGMENT_TEMPLATE.get()));dispatcher.execute("augment new @s echo_salvage",source);
            test.assertTrue(Augmentations.level(player.getMainHandItem(),RecommendedAbilities.ECHO_SALVAGE)==1 && Augmentations.level(player.getMainHandItem(),Augmentations.VERTICAL_STRIKE)==0,"Storage template incorrectly gains a weapon prerequisite");
        }finally{player.server.getPlayerList().remove(player);}test.succeed();
    }
    private static void expectFailure(GameTestHelper test,ThrowingCommand command,String message)throws Exception{
        try{command.run();test.fail(message);}catch(CommandSyntaxException expected){}
    }
    @FunctionalInterface private interface ThrowingCommand{void run()throws CommandSyntaxException;}
}
