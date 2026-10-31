package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class FinalDescriptionsGameTests {
    private static String color(Component text,int color) {
        var result=new StringBuilder();text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==color)result.append(part);return java.util.Optional.<Void>empty();},Style.EMPTY);return result.toString();
    }
    @GameTest(template="riftfang_test")
    public static void approvedHighlightBoundariesAndJadeEffectsAreExact(GameTestHelper test) {
        var base=new ItemStack(ModItems.DEATHWISPER_DAGGER.get());var jade=new ItemStack(ModItems.DEATHWISPER_DAGGER_JADE.get());
        String[][] cases={
            {"tooltips.forgermod.passive.axe","10 second combo"},
            {"tooltips.forgermod.passive.stone_song","2 armor"},
            {"tooltips.forgermod.ability.storing_anger","10 seconds"},
            {"tooltips.forgermod.passive.aftershock","1 delayed extra damage1 second"},
            {"tooltips.forgermod.passive.chain_lightning","50% chance10 mobs"},
            {"tooltips.forgermod.passive.echoing_speed","5% attack speed20% more attack speed8 seconds"},
            {"tooltips.forgermod.passive.soul_hunger","1 absorption heart3 hearts10 seconds"},
            {"tooltips.forgermod.ability.strong_arm","30% chance"}
        };
        for(var row:cases){var text=AbilityDescriptions.description(base,row[0],1,false);test.assertTrue(color(text,0xFFFFFF).equals(row[1]),"Incorrect approved white boundary: "+row[0]+" -> "+color(text,0xFFFFFF));}
        for(String id:java.util.List.of("tooltips.forgermod.ability.absolute_nightmare","tooltips.forgermod.passive.daydreamer","tooltips.forgermod.passive.fire_resistance","tooltips.forgermod.passive.homesick","tooltips.forgermod.passive.scar_compass")) {
            var normal=AbilityDescriptions.description(base,id,1,false);var infused=AbilityDescriptions.description(jade,id,1,false);
            test.assertTrue(!color(normal,0xFFFFFF).isEmpty() && color(normal,0x55FF55).isEmpty(),"Normal effects must be white: "+id);
            test.assertTrue(!color(infused,0x55FF55).isEmpty(),"Jade effect must be green: "+id);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void materialEffectsUseApprovedSentenceAndOnlyEffectValuesAreColored(GameTestHelper test)throws Exception {
        var append=net.bananashelp20.forgermod.item.custom.SwordItemWithEffect.class.getDeclaredMethod("appendWeaponTooltip",ItemStack.class,java.util.List.class,String.class,String.class,boolean.class);append.setAccessible(true);int count=0;
        for(var entry:ModItems.ITEMS.getEntries())if(entry.get() instanceof net.bananashelp20.forgermod.item.custom.SwordItemWithEffect weapon && weapon.hasMaterialEffect()) {
            var stack=Augmentations.apply(new ItemStack(weapon),Augmentations.MATERIAL_HIT);var rows=new java.util.ArrayList<Component>();
            append.invoke(weapon,stack,rows,"item.forgermod."+entry.getId().getPath(),weapon.gemstoneName(),true);
            var text=rows.stream().filter(c->c.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t && t.getKey().equals("tooltips.forgermod.passive.material_effect")).findFirst().orElseThrow();
            boolean jade=JadeAugments.isJade(stack);String highlights=color(text,jade?0x55FF55:0xFFFFFF);
            test.assertTrue(text.getString().startsWith("Apply ") && text.getString().endsWith(" seconds on hit."),"Wrong material sentence");
            test.assertTrue(!highlights.isEmpty() && highlights.endsWith(" seconds") && !highlights.contains("for") && !highlights.contains("Apply") && !highlights.contains("on hit"),"Material highlight includes prose or omits duration");
            test.assertTrue(color(text,jade?0xFFFFFF:0x55FF55).isEmpty(),"Wrong material effect color");count++;
        }
        test.assertTrue(count==135,"Skipped material/gem/weapon combination");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void allWeaponRankDescriptionsRenderAndExportCurrentValues(GameTestHelper test)throws Exception {
        var rows=new java.util.ArrayList<java.util.Map<String,Object>>();int finalRows=0;
        for(var entry:ModItems.ITEMS.getEntries()) {
            var stack=new ItemStack(entry.get());if(!Augmentations.eligible(stack))continue;
            for(var ability:Augmentations.pool(stack))for(int rank=1;rank<=Augmentations.maxRank(stack,ability.id());rank++) {
                var text=AbilityDescriptions.description(stack,ability.id(),rank,false);
                var card=AbilityDescriptions.description(stack,ability.id(),rank,true);
                test.assertTrue(!text.getString().contains("%s") && !text.getString().contains("{hp}") && !text.getString().contains("white;"),"Unresolved final description: "+ability.id()+" rank="+rank+" text="+text.getString());
                if(FinalReviewedDescriptions.description(stack,ability.id(),rank)!=null){finalRows++;test.assertTrue(!card.getString().isBlank() && !card.getString().contains("%s"),"Invalid compact card: "+ability.id());
                    if(FinalReviewedDescriptions.card(stack,ability.id(),rank)==null)test.assertTrue(text.getString().equals(card.getString()),"Unexpected item/card mismatch: "+ability.id());}
                rows.add(java.util.Map.of("weapon",entry.getId().toString(),"ability",ability.id(),"name",Augmentations.abilityName(stack,ability.id(),rank).getString(),"rank",rank,"text",text.getString(),"card",card.getString()));
            }
        }
        test.assertTrue(finalRows>500,"Final descriptions not exercised across weapon variants/ranks");
        java.nio.file.Files.writeString(java.nio.file.Path.of("final-approved-descriptions.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));
        test.succeed();
    }
}
