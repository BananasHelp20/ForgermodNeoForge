package net.bananashelp20.forgermod.gametest;
import com.google.gson.*;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeItems;
import net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
import java.nio.file.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class AbilityCatalogGameTests {
    @GameTest(template="riftfang_test")
    public static void actualRegisteredPoolsHaveNamesDescriptionsAndNoImplicitAugments(GameTestHelper test) throws Exception {
        var rows=new JsonArray();int special=0;
        for(var entry:ModItems.ITEMS.getEntries()) {
            var item=entry.get();if(!(item instanceof SwordItem))continue;
            var stack=new ItemStack(item);var row=new JsonObject();row.addProperty("item",BuiltInRegistries.ITEM.getKey(item).toString());row.addProperty("name",item.getDescriptionId());
            row.addProperty("family",item instanceof SwordItemWithEffect weapon?weapon.getClass().getSimpleName().replace("Weapon",""):"Other weapons");
            row.addProperty("type",DaggerItems.isDagger(stack)?"Dagger":AxeItems.isAxe(stack)?"Axe":BuiltInRegistries.ITEM.getKey(item).getPath().contains("claymore")?"Claymore":BuiltInRegistries.ITEM.getKey(item).getPath().contains("knife")?"Knife":BuiltInRegistries.ITEM.getKey(item).getPath().contains("bat")?"Bat":"Sword");
            var abilities=new JsonArray();
            if(Augmentations.eligible(stack)) {
                special++;
                test.assertTrue(Augmentations.learned(stack).isEmpty(),"Fresh weapon has implicit augments");
                var ids=new java.util.HashSet<String>();
                for(var ability:Augmentations.pool(stack)) {
                    test.assertTrue(ids.add(ability.id()),"Duplicate pooled ability: "+ability.id());
                    var a=new JsonObject();a.addProperty("id",ability.id());a.addProperty("active",ability.active());a.addProperty("maxRank",RecommendedAbilities.maxRank(ability.id()));
                    a.add("name",JsonParser.parseString(Component.Serializer.toJson(ability.name(),test.getLevel().registryAccess())));
                    abilities.add(a);
                }
            } else {
                String id=AxeItems.isAxe(stack)?Augmentations.VERTICAL_STRIKE:DaggerItems.isDagger(stack)?Augmentations.DUAL_WIELD:null;
                if(id!=null){var a=new JsonObject();a.addProperty("id",id);a.addProperty("active",false);a.addProperty("maxRank",1);a.add("name",JsonParser.parseString(Component.Serializer.toJson(Component.translatable(id+".name"),test.getLevel().registryAccess())));abilities.add(a);}
            }
            row.add("abilities",abilities);rows.add(row);
        }
        test.assertTrue(special==135,"Wrong special weapon count: "+special);
        Path output=Path.of("../ability-catalog.json");Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(rows));
        test.succeed();
    }
}
