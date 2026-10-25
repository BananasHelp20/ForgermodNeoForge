package net.bananashelp20.forgermod.gametest;

import com.google.gson.JsonParser;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class AncientGraveLootGameTests {
    private static final ResourceKey<LootTable> LOOT=ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"chests/ancient_grave"));

    @GameTest(template="sonic_boom_test",timeoutTicks=400)
    public static void bothTemplatesPlaceIndependentOneTimeLootChests(GameTestHelper test) {
        var level=test.getLevel();var manager=level.getStructureManager();int index=0;
        for(String name:List.of("ancient_grave","ancient_grave_city_room")) {
            var template=manager.getOrCreate(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,name));
            for(var rotation:Rotation.values()) {
                var chests=template.filterBlocks(BlockPos.ZERO,new StructurePlaceSettings().setRotation(rotation),Blocks.CHEST);
                test.assertTrue(chests.size()==2,"Add-on chest count changed in "+name);
                for(var chest:chests)test.assertTrue(chest.nbt()!=null && chest.nbt().getString("LootTable").equals(LOOT.location().toString()) && !chest.nbt().contains("Items"),"Template chest has fixed items or lacks the city-plus-sapphire table");
            }
            var origin=test.absolutePos(new BlockPos(100+index++*80,16,100));var settings=new StructurePlaceSettings().setIgnoreEntities(true);
            var bounds=template.getBoundingBox(settings,origin);
            for(int x=bounds.minX()>>4;x<=bounds.maxX()>>4;x++)for(int z=bounds.minZ()>>4;z<=bounds.maxZ()>>4;z++)level.getChunk(x,z);
            test.assertTrue(template.placeInWorld(level,origin,origin,settings,RandomSource.create(37+index),2),"Cannot place add-on loot fixture");
            var seeds=new HashSet<Long>();
            for(var info:template.filterBlocks(origin,settings,Blocks.CHEST)) {
                test.assertTrue(level.getBlockEntity(info.pos()) instanceof ChestBlockEntity,"Placed chest block entity missing");
                var chest=(ChestBlockEntity)level.getBlockEntity(info.pos());
                test.assertTrue(LOOT.equals(chest.getLootTable()),"Placed chest lost its loot-table reference");seeds.add(chest.getLootTableSeed());
                var generated=new ArrayList<ItemStack>();for(int slot=0;slot<chest.getContainerSize();slot++)generated.add(chest.getItem(slot).copy());
                test.assertTrue(chest.getLootTable()==null && generated.stream().anyMatch(s->!s.isEmpty()),"Opening chest does not generate Ancient City loot once");
                test.assertTrue(generated.stream().filter(s->s.is(ModItems.SAPPHIRE_GEMSTONE.get())).mapToInt(ItemStack::getCount).sum()<=1,"Chest contains more than one bonus sapphire");
                for(int slot=0;slot<generated.size();slot++)test.assertTrue(ItemStack.matches(generated.get(slot),chest.getItem(slot)),"Reopening chest rerolls its contents");
            }
            test.assertTrue(seeds.size()==2,"Both chests share a fixed loot seed");
        }
        test.succeed();
    }

    @GameTest(template="empty")
    public static void exactTenPercentBonusPreservesAllVanillaLoot(GameTestHelper test) throws Exception {
        var level=test.getLevel();var server=level.getServer();
        try(var reader=server.getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"loot_table/chests/ancient_grave.json"))) {
            var json=JsonParser.parseReader(reader).getAsJsonObject();var pools=json.getAsJsonArray("pools");
            test.assertTrue(json.get("type").getAsString().equals("minecraft:chest") && pools.size()==2,"Missing full vanilla loot plus independent bonus pool");
            var base=pools.get(0).getAsJsonObject();var entry=base.getAsJsonArray("entries").get(0).getAsJsonObject();
            test.assertTrue(base.get("rolls").getAsInt()==1 && entry.get("type").getAsString().equals("minecraft:loot_table") && entry.get("value").getAsString().equals("minecraft:chests/ancient_city"),"Base loot does not delegate to the vanilla Ancient City table");
            var bonus=pools.get(1).getAsJsonObject();var chance=bonus.getAsJsonArray("conditions").get(0).getAsJsonObject();
            test.assertTrue(bonus.get("rolls").getAsInt()==1 && chance.get("condition").getAsString().equals("minecraft:random_chance") && chance.get("chance").getAsFloat()==.1F,"Sapphire chance is not exactly one 10% roll per chest");
        }
        var table=server.reloadableRegistries().getLootTable(LOOT);var vanilla=server.reloadableRegistries().getLootTable(BuiltInLootTables.ANCIENT_CITY);
        test.assertTrue(table!=LootTable.EMPTY && vanilla!=LootTable.EMPTY,"Loot tables failed to load");
        var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(test.absolutePos(BlockPos.ZERO))).create(LootContextParamSets.CHEST);
        int successes=0;
        for(long seed=1;seed<=256;seed++) {
            var actual=table.getRandomItems(params,seed);var expected=vanilla.getRandomItems(params,seed);
            int gems=actual.stream().filter(s->s.is(ModItems.SAPPHIRE_GEMSTONE.get())).mapToInt(ItemStack::getCount).sum();
            test.assertTrue(gems==0 || gems==1,"Bonus does not add exactly one sapphire");successes+=gems;
            var original=actual.stream().filter(s->!s.is(ModItems.SAPPHIRE_GEMSTONE.get())).toList();
            test.assertTrue(original.size()==expected.size(),"Bonus replaces or duplicates vanilla loot");
            for(int slot=0;slot<expected.size();slot++)test.assertTrue(ItemStack.matches(original.get(slot),expected.get(slot)),"Ancient City loot changed for seed "+seed);
        }
        test.assertTrue(successes>0 && successes<256,"Bonus always or never generates across deterministic samples");test.succeed();
    }
}
