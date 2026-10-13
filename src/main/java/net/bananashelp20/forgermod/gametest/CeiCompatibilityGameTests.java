package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CeiCompatibilityGameTests {
    private static boolean installed() { return ModList.get().isLoaded("create_enchantment_industry"); }

    private static Object field(Object owner, String name) throws ReflectiveOperationException {
        Field field = owner.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }

    private static Object mode(String name) throws ReflectiveOperationException {
        Class<?> mode = Class.forName("plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerMode");
        @SuppressWarnings({"rawtypes", "unchecked"}) Object value = Enum.valueOf((Class) mode, name);
        return value;
    }

    private static Object result(Object inventory) throws ReflectiveOperationException {
        return inventory.getClass().getMethod("getLastResult").invoke(inventory);
    }

    private static ItemStack output(Object result, String method) throws ReflectiveOperationException {
        return (ItemStack) result.getClass().getMethod(method).invoke(result);
    }

    @GameTest(templateNamespace = "forgermod", template = "riftfang_test")
    public static void forgerAppliesAndExtractsAugments(GameTestHelper test) throws Exception {
        if (!installed()) { test.succeed(); return; }
        var block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create_enchantment_industry", "blaze_forger"));
        BlockPos pos = test.absolutePos(new BlockPos(1, 2, 1));
        test.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        Object forger = test.getLevel().getBlockEntity(pos);
        test.assertTrue(forger != null, "CEI Blaze Forger did not initialize");
        Method setMode = forger.getClass().getMethod("setMode", mode("APPLY").getClass());
        Object inventory = field(forger, "inventory");
        IItemHandlerModifiable slots = (IItemHandlerModifiable) inventory;
        ItemStack weapon = new ItemStack(ModItems.EMBERFANG_DAGGER.get());
        String id = Augmentations.pool(weapon).getFirst().id();
        ItemStack filled = Augmentations.apply(new ItemStack(ModItems.AUGMENT_TEMPLATE.get()), id);
        setMode.invoke(forger, mode("APPLY"));
        slots.setStackInSlot(0, weapon);
        slots.setStackInSlot(1, filled);
        ItemStack applied = output(result(inventory), "primaryOutput");
        test.assertTrue(Augmentations.level(applied, id) == 1, "Blaze Forger did not apply the template augment");
        setMode.invoke(forger, mode("EXTRACT"));
        slots.setStackInSlot(0, applied);
        slots.setStackInSlot(1, new ItemStack(ModItems.AUGMENT_TEMPLATE.get()));
        Object extracted = result(inventory);
        test.assertTrue(Augmentations.level(output(extracted, "primaryOutput"), id) == 0
                && Augmentations.level(output(extracted, "secondaryOutput"), id) == 1,
                "Blaze Forger did not move the augment into a blank template");
        Field heat = forger.getClass().getDeclaredField("special");
        heat.setAccessible(true);
        heat.setBoolean(forger, true);
        ItemStack superTemplate = new ItemStack(ModItems.SUPER_AUGMENT_TEMPLATE.get());
        int cap = net.bananashelp20.forgermod.augmentation.RecommendedAbilities.maxRank(id);
        for (int rank = 0; rank <= cap; rank++) superTemplate = Augmentations.apply(superTemplate, id);
        setMode.invoke(forger, mode("APPLY"));
        slots.setStackInSlot(0, weapon);
        slots.setStackInSlot(1, superTemplate);
        test.assertTrue(Augmentations.level(output(result(inventory), "primaryOutput"), id) == cap + 1,
                "Super Blaze Forger did not apply the bonus rank");
        test.succeed();
    }

    @GameTest(templateNamespace = "forgermod", template = "riftfang_test")
    public static void enchanterFillsBothTemplateKinds(GameTestHelper test) throws Exception {
        if (!installed()) { test.succeed(); return; }
        var block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create_enchantment_industry", "blaze_enchanter"));
        BlockPos pos = test.absolutePos(new BlockPos(1, 2, 1));
        test.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        Object enchanter = test.getLevel().getBlockEntity(pos);
        test.assertTrue(enchanter != null, "CEI Blaze Enchanter did not initialize");
        Object behaviour = field(enchanter, "enchanter");
        test.assertTrue(behaviour != null, "CEI Enchanter behaviour did not initialize");
        Method canProcess = behaviour.getClass().getMethod("canProcess", ItemStack.class);
        Method getResult = behaviour.getClass().getMethod("getResult", ItemStack.class);
        Method getCost = behaviour.getClass().getMethod("getExperienceCost");
        Field held = enchanter.getClass().getDeclaredField("heldItem");
        held.setAccessible(true);
        for (boolean special : new boolean[] {false, true}) {
            Field heat = enchanter.getClass().getDeclaredField("special");
            heat.setAccessible(true);
            heat.setBoolean(enchanter, special);
            ItemStack blank = new ItemStack(special ? ModItems.SUPER_AUGMENT_TEMPLATE.get() : ModItems.AUGMENT_TEMPLATE.get());
            test.assertTrue((boolean) canProcess.invoke(behaviour, blank), "Blaze Enchanter rejected matching augment template");
            held.set(enchanter, blank);
            test.assertTrue((int) getCost.invoke(behaviour) > 0, "Blaze Enchanter had no augment cost");
            ItemStack filled = (ItemStack) getResult.invoke(behaviour, blank);
            test.assertTrue(!Augmentations.learned(filled).isEmpty(), "Blaze Enchanter did not add an augment");
        }
        test.succeed();
    }
}
