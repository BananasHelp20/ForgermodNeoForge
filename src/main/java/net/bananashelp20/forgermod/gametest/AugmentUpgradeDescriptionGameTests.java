package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AugmentUpgradeDescriptionGameTests {
    private static AugmentUpgradeDescriptions.Change property(GameTestHelper test, ItemStack stack,
                                                              String id, int rank, String key) {
        var result=AugmentUpgradeDescriptions.changes(stack,id,rank).stream().filter(c->c.property().equals(key)).findFirst();
        test.assertTrue(result.isPresent(),"Missing changed property: "+id+" "+key);
        return result.orElseThrow();
    }
    private static boolean has(ItemStack stack,String id,int rank,String key) {
        return AugmentUpgradeDescriptions.changes(stack,id,rank).stream().anyMatch(c->c.property().equals(key));
    }
    @GameTest(template="riftfang_test")
    public static void previewsCoverEveryOfferWithoutChangingTheWeapon(GameTestHelper test) throws Exception {
        int comparisons=0;
        var previews=new java.util.LinkedHashSet<String>();
        for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());
            if(!Augmentations.augmentable(fresh)) continue;
            for(var ability:Augmentations.catalog(fresh)) {
                for(int rank=1;rank<Augmentations.maxRank(fresh,ability.id());rank++) {
                    var prepared=Augmentations.canonical(fresh)!=null && Augmentations.canonical(fresh).isAxe() && Augmentations.requiresVerticalStrike(ability.id())
                            ? Augmentations.apply(fresh,Augmentations.VERTICAL_STRIKE):fresh;
                    var stack=Augmentations.setLevel(prepared,ability.id(),rank);
                    test.assertFalse(stack.isEmpty(),"Cannot create rank fixture");
                    var before=stack.copy();
                    var compact=AugmentUpgradeDescriptions.description(stack,ability.id(),rank,true,true);
                    previews.add(Component.Serializer.toJson(compact,test.getLevel().registryAccess()));
                    var full=AugmentUpgradeDescriptions.description(stack,ability.id(),rank,false,true);
                    test.assertTrue(ItemStack.matches(before,stack),"Preview mutates the item");
                    test.assertTrue(!compact.getSiblings().isEmpty() && !full.getSiblings().isEmpty(),"Empty upgrade description");
                    var augmented=Augmentations.apply(stack,ability.id());
                    test.assertFalse(augmented.isEmpty(),"Previewed rank cannot be applied");
                    var completed=AugmentUpgradeDescriptions.description(augmented,ability.id(),rank,true,true);
                    test.assertTrue(Component.Serializer.toJson(compact,test.getLevel().registryAccess()).equals(
                            Component.Serializer.toJson(completed,test.getLevel().registryAccess())),"Completed card compares against the new rank instead of the offered rank");
                    comparisons++;
                }
            }
        }
        test.assertTrue(comparisons>1000,"Skipped weapon variants or template offers");
        java.nio.file.Files.writeString(java.nio.file.Path.of("../upgrade-preview-components.json"),"["+String.join(",",previews)+"]");
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void changesShowTheActualNewValuesAndColorOnlyThoseValues(GameTestHelper test) {
        var axe=new ItemStack(ModItems.MOLTEN_AXE.get());
        var damage=property(test,axe,RecommendedAbilities.CRUCIBLE_HOOK,1,"damage");
        test.assertTrue(damage.before().equals(1.25) && damage.after().equals(1.5),"Wrong resulting hook damage");
        var sentence=damage.sentence(true);
        var value=(Component)((TranslatableContents)sentence.getContents()).getArgs()[0];
        test.assertTrue(sentence.getStyle().getColor()==null,"Whole upgrade sentence is green");
        test.assertTrue(value.getStyle().getColor()!=null && value.getStyle().getColor().getValue()==AugmentUpgradeDescriptions.CHANGE_COLOR,"Changed damage is not green");
        var muted=(Component)((TranslatableContents)damage.sentence(false).getContents()).getArgs()[0];
        test.assertTrue(muted.getStyle().getColor()==null,"Rejected card retains a bright improvement");
        var cooldown=property(test,axe,RecommendedAbilities.CRUCIBLE_HOOK,1,"cooldown");
        test.assertTrue(cooldown.before().equals(40.0) && cooldown.after().equals(35.0),"Cooldown-only improvement is wrong");
        var dagger=new ItemStack(ModItems.ASSASSIN_DAGGER.get());
        test.assertTrue(property(test,dagger,"tooltips.forgermod.ability.leech",1,"healing").after().equals(12.0),"Leech preview uses a range instead of the new value");
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void superFormsWeaponTypesAndJadeHaveCorrectDifferences(GameTestHelper test) {
        var axe=new ItemStack(ModItems.SKYBREAKER_AXE.get());
        var dagger=new ItemStack(ModItems.DEAD_CALM_DAGGER.get());
        test.assertTrue(property(test,axe,AdditionalAbilities.WINDCHARGE,3,"launch").after().equals(30.0),"Axe launch height is wrong");
        test.assertTrue(has(axe,AdditionalAbilities.WINDCHARGE,3,"unlocks") && !has(dagger,AdditionalAbilities.WINDCHARGE,3,"unlocks"),"Non-axe offers Skybreaker");
        test.assertTrue(property(test,dagger,AdditionalAbilities.WINDCHARGE,3,"launch").after().equals(20.0),"Dagger launch height is wrong");
        test.assertTrue(has(dagger,Augmentations.DOUBLE_JUMP,3,"air_jumps") && !has(dagger,Augmentations.DOUBLE_JUMP,4,"air_jumps")
                && has(dagger,Augmentations.DOUBLE_JUMP,4,"jump_height"),"Windwalker highlights unchanged jump count");
        var ghost=new ItemStack(ModItems.HOLLOW_CLAYMORE.get());
        test.assertTrue(has(ghost,AdditionalAbilities.GHOSTWALK,3,"opening_hit") && !has(ghost,AdditionalAbilities.GHOSTWALK,3,"duration"),"Jumpscare Horror highlights unchanged duration");
        var jade=new ItemStack(ModItems.NIGHTMARE_DAGGER_JADE.get());
        test.assertTrue(property(test,jade,"tooltips.forgermod.ability.absolute_nightmare",1,"duration").after().equals(11.0),"Jade's extra second is missing");
        var thunder=new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get());
        test.assertTrue(property(test,thunder,AdditionalAbilities.DIRECT_CURRENT,4,"field_damage").after().equals(1.25)
                && !has(thunder,AdditionalAbilities.DIRECT_CURRENT,4,"targets") && !has(thunder,AdditionalAbilities.DIRECT_CURRENT,4,"unlocks"),"Static Field repeats unchanged unlocks");
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void fixedRanksAndRosterChangesAreNotMisrepresented(GameTestHelper test) {
        var dagger=new ItemStack(ModItems.DEAD_CALM_DAGGER.get());
        test.assertTrue(AugmentUpgradeDescriptions.changes(dagger,Augmentations.DOUBLE_JUMP,1).isEmpty(),"Double Jump II invents a bonus");
        test.assertTrue(AugmentUpgradeDescriptions.changes(dagger,Augmentations.MATERIAL_HIT,1).isEmpty(),"Material Hit rank invents a bonus");
        var claymore=new ItemStack(ModItems.HOLLOW_CLAYMORE.get());
        var roster=property(test,claymore,AdditionalAbilities.BONE_GUARD,2,"summons");
        test.assertFalse(roster.before().equals(roster.after()),"Same-sized reinforcement upgrade hides the new mob type");
        test.assertTrue(has(claymore,AdditionalAbilities.BONE_GUARD,3,"capacity") && has(claymore,AdditionalAbilities.BONE_GUARD,4,"equipment")
                && !has(claymore,AdditionalAbilities.BONE_GUARD,6,"equipment"),"Necromancer capacity/equipment unlock is wrong");
        test.assertTrue(!has(claymore,AdditionalAbilities.BONE_GUARD,3,"summons"),"Custom roster is replaced by a fake fixed roster preview");
        test.succeed();
    }
}
