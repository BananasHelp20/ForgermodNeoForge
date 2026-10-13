package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AugmentAnvilEvents;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AugmentTemplateGameTests {
    @GameTest(template = "riftfang_test")
    public static void templateAcceptsTableOffersAndCompatibleAnvilTransfer(GameTestHelper test) {
        ItemStack blank = new ItemStack(ModItems.AUGMENT_TEMPLATE.get());
        test.assertTrue(Augmentations.augmentable(blank) && !Augmentations.eligible(blank), "Template target classification");
        var tablePos = test.absolutePos(new BlockPos(1, 2, 1));
        test.getLevel().setBlockAndUpdate(tablePos, ModBlocks.AUGMENTATION_TABLE.get().defaultBlockState());
        var table = (AugmentationTableBlockEntity)test.getLevel().getBlockEntity(tablePos);
        test.assertTrue(table.inventory.isItemValid(0, blank), "Template rejected by table");
        String id = Augmentations.pool(new ItemStack(ModItems.EMBERFANG_DAGGER.get())).stream()
                .filter(a -> a.active() && Augmentations.available(blank).contains(a)).findFirst().orElseThrow().id();
        ItemStack filled = Augmentations.apply(blank, id);
        test.assertTrue(Augmentations.level(filled, id) == 1 && Augmentations.learned(blank).isEmpty(), "Template failed to store an ability");
        ItemStack target = new ItemStack(ModItems.EMBERFANG_DAGGER.get());
        var player = test.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        AnvilUpdateEvent event = new AnvilUpdateEvent(target, filled, "Forged Fang", 0, player);
        AugmentAnvilEvents.onAnvil(event);
        test.assertTrue(Augmentations.level(event.getOutput(), id) == 1, "Anvil did not transfer compatible ability");
        test.assertTrue(event.getOutput().getHoverName().getString().equals("Forged Fang") && event.getCost() > 0,
                "Anvil name or cost missing");
        test.assertTrue(Augmentations.merge(new ItemStack(ModItems.DEATHWISPER_DAGGER.get()), filled).isEmpty(),
                "Incompatible ability transferred");
        test.assertTrue(Augmentations.merge(target, new ItemStack(Items.BOOK)).isEmpty(), "Ordinary item accepted as source");
        ItemStack damagedLeft = new ItemStack(ModItems.EMBERFANG_DAGGER.get());
        damagedLeft.setDamageValue(damagedLeft.getMaxDamage() / 2);
        ItemStack damagedRight = Augmentations.apply(new ItemStack(ModItems.EMBERFANG_DAGGER.get()), id);
        damagedRight.setDamageValue(damagedRight.getMaxDamage() / 2);
        AnvilUpdateEvent repaired = new AnvilUpdateEvent(damagedLeft, damagedRight, null, 0, player);
        AugmentAnvilEvents.onAnvil(repaired);
        test.assertTrue(repaired.getOutput().getDamageValue() < damagedLeft.getDamageValue()
                && Augmentations.level(repaired.getOutput(), id) == 1,
                "Same-weapon anvil repair lost its ability transfer");
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void fullSlotsWaitForChoiceAndEqualRanksIncrease(GameTestHelper test) {
        ItemStack original = new ItemStack(ModItems.INFERNAL_CLAYMORE.get());
        var actives = Augmentations.pool(original).stream().filter(Augmentations.Ability::active).limit(4).toList();
        test.assertTrue(actives.size() == 4, "Need four compatible actives");
        ItemStack target = original;
        for (int i = 0; i < 3; i++) target = Augmentations.apply(target, actives.get(i).id());
        ItemStack source = Augmentations.apply(new ItemStack(ModItems.AUGMENT_TEMPLATE.get()), actives.get(3).id());
        var pending = Augmentations.pendingReplacement(target, source, java.util.Map.of());
        test.assertTrue(pending != null && pending.choices().size() == 3, "Full active slots did not request a choice");
        test.assertTrue(Augmentations.merge(target, source).isEmpty(), "Full slots merged without a choice");
        var player = test.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        AnvilUpdateEvent blocked = new AnvilUpdateEvent(target, source, null, 0, player);
        AugmentAnvilEvents.onAnvil(blocked);
        test.assertTrue(blocked.isCanceled(), "Anvil offered an output before a replacement was chosen");
        ItemStack merged = Augmentations.merge(target, source,
                java.util.Map.of(actives.get(3).id(), actives.get(0).id()));
        test.assertTrue(Augmentations.pendingReplacement(target, source,
                        java.util.Map.of(actives.get(3).id(), actives.get(0).id())) == null
                        && Augmentations.replacementPrompt(target, source,
                        java.util.Map.of(actives.get(3).id(), actives.get(0).id())) != null,
                "Selected replacement cannot be revised with arrow keys");
        test.assertTrue(Augmentations.ids(merged, true).size() == 3, "Active capacity exceeded");
        test.assertTrue(Augmentations.level(merged, actives.get(0).id()) == 0, "Oldest active was not replaced");
        test.assertTrue(Augmentations.level(merged, actives.get(3).id()) == 1, "New active was not learned");
        ItemStack second = Augmentations.merge(merged, source);
        test.assertTrue(Augmentations.level(second, actives.get(3).id()) == 2, "Equal ranks did not combine");
        test.assertTrue(Augmentations.level(target, actives.get(0).id()) == 1, "Original stack mutated");
        ItemStack weaponSource = Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()), actives.get(0).id());
        test.assertTrue(Augmentations.level(Augmentations.merge(second, weaponSource,
                java.util.Map.of(actives.get(0).id(), actives.get(1).id())), actives.get(0).id()) == 1,
                "Weapon to weapon transfer failed");
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void passiveSlotsAlsoRequireExplicitChoice(GameTestHelper test) {
        ItemStack dagger = new ItemStack(ModItems.RIFTFANG_DAGGER.get());
        var passives = Augmentations.pool(dagger).stream()
                .filter(a -> !a.active() && !a.id().equals(Augmentations.MATERIAL_HIT)).limit(3).toList();
        test.assertTrue(passives.size() == 3, "Need three compatible passives");
        ItemStack target = Augmentations.apply(Augmentations.apply(dagger, passives.get(0).id()), passives.get(1).id());
        ItemStack source = Augmentations.apply(new ItemStack(ModItems.AUGMENT_TEMPLATE.get()), passives.get(2).id());
        var pending = Augmentations.pendingReplacement(target, source, java.util.Map.of());
        test.assertTrue(pending != null && pending.choices().size() == 2, "Passive replacement not requested");
        ItemStack result = Augmentations.merge(target, source,
                java.util.Map.of(passives.get(2).id(), passives.get(1).id()));
        test.assertTrue(Augmentations.ids(result, false).size() == 2
                        && Augmentations.level(result, passives.get(0).id()) == 1
                        && Augmentations.level(result, passives.get(1).id()) == 0
                        && Augmentations.level(result, passives.get(2).id()) == 1,
                "Selected passive was not replaced");
        test.succeed();
    }
}
