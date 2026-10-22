package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AbilityDescriptionGameTests {
    private static String key(Component text) {
        return text.getContents() instanceof TranslatableContents translated ? translated.getKey() : "";
    }
    private static String json(GameTestHelper test, Component text) {
        return Component.Serializer.toJson(text,test.getLevel().registryAccess());
    }
    @GameTest(template="riftfang_test")
    public static void currentRankCountsAndSuperMechanicsAreSeparate(GameTestHelper test) {
        var thunder=new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get());
        for(int rank=1;rank<=7;rank++) {
            var text=AbilityDescriptions.description(thunder,AdditionalAbilities.DIRECT_CURRENT,rank,false);
            test.assertTrue(key(text).equals(AdditionalAbilities.DIRECT_CURRENT+(rank>=4?".super":"")+".current"),"Wrong electrical form");
            var targets=(Component)((TranslatableContents)text.getContents()).getArgs()[0];
            test.assertTrue(((TranslatableContents)targets.getContents()).getArgs()[0].equals(""+Math.min(4,rank+1)),"Shows future target count");
            test.assertTrue(AbilityDescriptions.canBecomeSuper(thunder,AdditionalAbilities.DIRECT_CURRENT,rank)==(rank<4),"Super hint remains after conversion");
        }
        var dagger=new ItemStack(ModItems.DEAD_CALM_DAGGER.get());
        var axe=new ItemStack(ModItems.SKYBREAKER_AXE.get());
        test.assertFalse(AbilityDescriptions.canBecomeSuper(dagger,AdditionalAbilities.WINDCHARGE,3),"Dagger claims Skybreaker");
        test.assertTrue(key(AbilityDescriptions.description(dagger,AdditionalAbilities.WINDCHARGE,4,false)).endsWith("windcharge.current"),"Dagger gets axe mechanics");
        test.assertTrue(key(AbilityDescriptions.description(axe,AdditionalAbilities.WINDCHARGE,4,false)).endsWith("windcharge.super.current"),"Skybreaker has base description");
        var ghost=new ItemStack(ModItems.HOLLOW_CLAYMORE.get());
        var baseGhost=AbilityDescriptions.description(ghost,AdditionalAbilities.GHOSTWALK,3,false);
        var superGhost=AbilityDescriptions.description(ghost,AdditionalAbilities.GHOSTWALK,4,false);
        test.assertTrue(key(baseGhost).endsWith("ghostwalk.reviewed") && !baseGhost.getString().contains("damage"),"Base invisibility leaks opening damage");
        test.assertTrue(key(superGhost).endsWith("ghostwalk.super.reviewed") && superGhost.getString().contains("damage"),"Super opening damage missing");
        var jade=new ItemStack(ModItems.NIGHTMARE_DAGGER_JADE.get());
        var nightmare=AbilityDescriptions.description(jade,"tooltips.forgermod.ability.absolute_nightmare",1,false);
        test.assertTrue(key(nightmare).endsWith(".current.jade") && json(test,nightmare).contains("6"),"Jade current effect is wrong");
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void expandedHintFollowsCooldownAndDisappearsInSuperForm(GameTestHelper test) throws Exception {
        var method=SwordItemWithEffect.class.getDeclaredMethod("appendWeaponTooltip",ItemStack.class,List.class,String.class,String.class,boolean.class);
        method.setAccessible(true);
        var fresh=new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get());
        for(int rank:new int[]{1,3,4,7}) for(boolean expanded:new boolean[]{false,true}) {
            var stack=Augmentations.setLevel(fresh,AdditionalAbilities.DIRECT_CURRENT,rank);
            var rows=new ArrayList<Component>();
            method.invoke(stack.getItem(),stack,rows,"test.lore","no_gemstone",expanded);
            var keys=rows.stream().map(AbilityDescriptionGameTests::key).toList();
            var title=rows.stream().filter(row->key(row).equals("tooltips.forgermod.ability.tooltip")).findFirst().orElseThrow();
            if(rank>=4) {
                test.assertTrue(title.getString().startsWith("k ") && title.getString().endsWith("] k"),"Super glyph does not follow the full keybinding label");
                int[] animated={0};
                title.visit((style,text)->{
                    if(!text.isEmpty()) test.assertTrue(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.GOLD.getColor(),"Super label rank, punctuation or keybinding is not legendary gold");
                    if(style.isObfuscated()) {
                        test.assertTrue(text.equals("k"),"Ability text/keybinding became obfuscated");
                        animated[0]+=text.length();
                    }
                    return Optional.empty();
                },net.minecraft.network.chat.Style.EMPTY);
                test.assertTrue(animated[0]==2,"Super title has duplicate glyphs around its name");
                test.assertTrue(json(test,title).contains("key.forgermod.ability_primary"),"Configured keybinding was lost");
            }
            int hint=keys.indexOf("augmentation.forgermod.super_available");
            test.assertTrue((hint>=0)==(expanded && rank<4),"Hint ignores Shift or current form");
            if(hint>=0) test.assertTrue(keys.get(hint-1).equals("augmentation.forgermod.cooldown"),"Hint is not below cooldown");
            test.assertTrue(keys.contains(AdditionalAbilities.DIRECT_CURRENT+(rank>=4?".super":"")+".current")==expanded,"Description ignores Shift");
        }
        // Passives have no cooldown: their note follows their current-rank description.
        var dagger=Augmentations.setLevel(new ItemStack(ModItems.DEAD_CALM_DAGGER.get()),Augmentations.DOUBLE_JUMP,1);
        var rows=new ArrayList<Component>();
        method.invoke(dagger.getItem(),dagger,rows,"test.lore","no_gemstone",true);
        var keys=rows.stream().map(AbilityDescriptionGameTests::key).toList();
        test.assertTrue(keys.indexOf("augmentation.forgermod.super_available")==keys.indexOf(Augmentations.DOUBLE_JUMP+".current")+1,"Passive note is misplaced");
        var windwalker=Augmentations.setLevel(dagger,Augmentations.DOUBLE_JUMP,4);
        rows.clear();method.invoke(windwalker.getItem(),windwalker,rows,"test.lore","no_gemstone",false);
        var passive=rows.stream().filter(row->row.getString().contains("Windwalker")).findFirst().orElseThrow();
        test.assertTrue(passive.getString().equals("k Windwalker I k"),"Super passive glyph follows the name instead of the rank");
        int[] glyphs={0};
        passive.visit((style,text)->{
            if(!text.isEmpty()) test.assertTrue(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.GOLD.getColor(),"Super passive rank is not gold");
            if(style.isObfuscated()){test.assertTrue(text.equals("k"),"Super passive name/rank is obfuscated");glyphs[0]++;}
            return Optional.empty();
        },net.minecraft.network.chat.Style.EMPTY);
        test.assertTrue(glyphs[0]==2,"Super passive glyph count is wrong");
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void allCurrentDescriptionsAndLearningCardsAreReadOnly(GameTestHelper test) throws Exception {
        var descriptions=new LinkedHashSet<String>();
        var cards=new LinkedHashSet<String>();
        int checked=0;
        for(var entry:ModItems.ITEMS.getEntries()) {
            var stack=new ItemStack(entry.get());
            if(!Augmentations.augmentable(stack)) continue;
            var before=stack.copy();
            for(var ability:Augmentations.catalog(stack)) {
                if(ability.id().equals(Augmentations.MATERIAL_HIT)) continue; // Needs the receiving material's format arguments.
                for(int rank=1;rank<=Augmentations.maxRank(stack,ability.id());rank++) {
                    var text=AbilityDescriptions.description(stack,ability.id(),rank,false);
                    var serialized=json(test,text);
                    test.assertFalse(serialized.contains(":null") || serialized.contains("[null") || serialized.contains(",null"),"Missing current-rank value: "+ability.id());
                    descriptions.add(serialized);
                    checked++;
                }
                cards.add(json(test,AugmentUpgradeDescriptions.description(stack,ability.id(),0,true,true)));
            }
            test.assertTrue(ItemStack.matches(stack,before),"Description applied an augment");
        }
        test.assertTrue(checked>1000,"Skipped registered weapons/ranks");
        java.nio.file.Files.writeString(java.nio.file.Path.of("../current-ability-components.json"),"["+String.join(",",descriptions)+"]");
        java.nio.file.Files.writeString(java.nio.file.Path.of("../learning-card-components.json"),"["+String.join(",",cards)+"]");
        test.succeed();
    }
}
