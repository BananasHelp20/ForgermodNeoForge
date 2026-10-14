package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class SuperAugmentGameTests {
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
    public static void staticFieldProgressionAndTemplateRoundTrip(GameTestHelper test) {
        progression(test,new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()),AdditionalAbilities.DIRECT_CURRENT,"tooltips.forgermod.ability.static_field.name");
        for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get())) {
            ItemStack stack=new ItemStack(item);for(int rank=1;rank<=7;rank++)stack=Augmentations.apply(stack,AdditionalAbilities.DIRECT_CURRENT);
            test.assertTrue(Augmentations.level(stack,AdditionalAbilities.DIRECT_CURRENT)==7 && Augmentations.apply(stack,AdditionalAbilities.DIRECT_CURRENT).isEmpty(),"Template has different super cap");
        }
        test.succeed();
    }
}
