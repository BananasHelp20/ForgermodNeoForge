package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class SuperAugmentGameTests {
    @GameTest(template="riftfang_test")
    public static void superNamesAreGoldWithOnlyTwoObfuscatedCharacters(GameTestHelper test) throws Exception {
        var exported=new java.util.LinkedHashSet<String>();
        int checked=0;
        for(var entry:ModItems.ITEMS.getEntries()) {
            var stack=new ItemStack(entry.get());
            if(!Augmentations.augmentable(stack))continue;
            for(var ability:Augmentations.catalog(stack)) for(int rank=1;rank<=Augmentations.maxRank(stack,ability.id());rank++) {
                var plain=Augmentations.abilityName(stack,ability.id(),rank);
                var display=Augmentations.displayAbilityName(stack,ability.id(),rank);
                boolean legendary=Augmentations.isSuper(stack,ability.id(),rank);
                if(!legendary) {
                    test.assertTrue(display.equals(plain),"Ordinary augment received legendary formatting");
                    continue;
                }
                checked++;
                test.assertTrue(display.getString().equals("k "+plain.getString()+" k"),"Super name is missing its flanking characters/spaces");
                var wrapper=net.minecraft.network.chat.Component.empty()
                        .append(display).append(net.minecraft.network.chat.Component.literal(" IV"))
                        .withStyle(net.minecraft.ChatFormatting.GRAY);
                int[] obfuscated={0};
                wrapper.visit((style,text)->{
                    if(text.isEmpty())return java.util.Optional.empty();
                    if(text.equals(" IV")) {
                        test.assertTrue(!style.isObfuscated() && style.getColor().getValue()==net.minecraft.ChatFormatting.GRAY.getColor(),"Formatting leaks into rank text");
                    } else {
                        test.assertTrue(style.getColor().getValue()==net.minecraft.ChatFormatting.GOLD.getColor(),"Parent gray overrides legendary gold");
                        if(style.isObfuscated()) {
                            obfuscated[0]+=text.length();
                            test.assertTrue(text.equals("k"),"Name or spacing is obfuscated");
                        }
                    }
                    return java.util.Optional.empty();
                },net.minecraft.network.chat.Style.EMPTY);
                test.assertTrue(obfuscated[0]==2,"Expected exactly two animated characters");
                display.copy().withStyle(style->style.withColor(0x666666)).visit((style,text)->{
                    test.assertTrue(style.getColor().getValue()==0x666666,"Rejected card retains bright gold");
                    return java.util.Optional.empty();
                },net.minecraft.network.chat.Style.EMPTY);
                exported.add(net.minecraft.network.chat.Component.Serializer.toJson(display,test.getLevel().registryAccess()));
            }
        }
        test.assertTrue(checked>100,"Skipped super forms, gemstone variants or templates");
        java.nio.file.Files.writeString(java.nio.file.Path.of("../super-name-components.json"),"["+String.join(",",exported)+"]");
        test.succeed();
    }
    public static void progression(GameTestHelper test,ItemStack fresh,String id,String name) {
        ItemStack stack=fresh;
        for(int rank=1;rank<=7;rank++) {
            stack=Augmentations.apply(stack,id);
            test.assertFalse(stack.isEmpty(),"Upgrade refused at "+rank);
            test.assertTrue(Augmentations.level(stack,id)==rank && Augmentations.displayRank(stack,id,rank)==(rank<4?rank:rank-3),"Stored/displayed rank wrong");
            test.assertTrue(Augmentations.learned(stack).size()==1,"Super form consumes another slot");
            if(rank>=4) test.assertTrue(Augmentations.abilityName(stack,id,rank).getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text && text.getKey().equals(name),"Super name missing");
        }
        test.assertTrue(Augmentations.apply(stack,id).isEmpty(),"Super IV can upgrade again");
        var extracted=Augmentations.extractFirst(stack,new ItemStack(ModItems.AUGMENT_TEMPLATE.get()));
        test.assertTrue(extracted!=null && Augmentations.level(extracted.template(),id)==7,"Extraction loses super rank");
        var merged=Augmentations.merge(fresh,extracted.template());
        test.assertTrue(Augmentations.level(merged,id)==7 && Augmentations.displayRank(merged,id,7)==4,"Anvil transfer loses super rank");
        test.assertTrue(Augmentations.merge(merged,extracted.template()).isEmpty(),"Equal super IV ranks exceed cap");
    }
    @GameTest(template="riftfang_test")
    public static void windwalkerProgressionAndTemplateRoundTrip(GameTestHelper test) {
        progression(test,new ItemStack(ModItems.DEAD_CALM_DAGGER.get()),Augmentations.DOUBLE_JUMP,"tooltips.forgermod.passive.windwalker.name");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void skybreakerProgressionAndTemplateRoundTrip(GameTestHelper test) {
        progression(test,new ItemStack(ModItems.SKYBREAKER_AXE.get()),AdditionalAbilities.WINDCHARGE,"tooltips.forgermod.ability.skybreaker.name");
        test.assertTrue(Augmentations.maxRank(new ItemStack(ModItems.STORMING_CLAYMORE.get()),AdditionalAbilities.WINDCHARGE)==4,"Claymore gets axe super progression");
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void staticFieldProgressionAndTemplateRoundTrip(GameTestHelper test) {
        progression(test,new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()),AdditionalAbilities.DIRECT_CURRENT,"tooltips.forgermod.ability.static_field.name");
        for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get())) {
            ItemStack stack=new ItemStack(item);for(int rank=1;rank<=7;rank++)stack=Augmentations.apply(stack,AdditionalAbilities.DIRECT_CURRENT);
            test.assertTrue(Augmentations.level(stack,AdditionalAbilities.DIRECT_CURRENT)==7 && Augmentations.apply(stack,AdditionalAbilities.DIRECT_CURRENT).isEmpty(),"Template has different super cap");
        }
        test.succeed();
    }
}
