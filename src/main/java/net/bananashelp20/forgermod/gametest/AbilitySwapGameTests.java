package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.lang.reflect.Proxy;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AbilitySwapGameTests {
    private static IPayloadContext context(ServerPlayer player) {
        return (IPayloadContext)Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(), new Class<?>[]{IPayloadContext.class},
                (proxy, method, args) -> method.getName().equals("player") ? player : null);
    }
    private static String item(ItemStack stack) { return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(); }
    private static void swap(ServerPlayer player) throws Exception {
        var method = WeaponAbilityNetwork.class.getDeclaredMethod("handleSwap", WeaponAbilityNetwork.SwapAbilitiesPayload.class, IPayloadContext.class);
        method.setAccessible(true); method.invoke(null, new WeaponAbilityNetwork.SwapAbilitiesPayload(player.getInventory().selected, item(player.getMainHandItem())), context(player));
    }
    private static void use(ServerPlayer player, int slot) throws Exception {
        var method = WeaponAbilityNetwork.class.getDeclaredMethod("handleAbility", WeaponAbilityNetwork.UseAbilityPayload.class, IPayloadContext.class);
        method.setAccessible(true); method.invoke(null, new WeaponAbilityNetwork.UseAbilityPayload(slot, player.getInventory().selected, item(player.getMainHandItem())), context(player));
    }
    @GameTest(template = "riftfang_test")
    public static void swapPersistsThroughUpgradeAndInfusionAndIgnoresOffhand(GameTestHelper test) throws Exception {
        var player = test.makeMockServerPlayerInLevel();
        ItemStack stack = TestWeapons.ready(ModItems.DEAD_CALM_DAGGER.get());
        ItemStack off = TestWeapons.ready(ModItems.DEAD_CALM_DAGGER_JADE.get());
        var beforeOff = off.getComponentsPatch();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack); player.setItemInHand(InteractionHand.OFF_HAND, off);
        String first = Augmentations.activeId(stack, WeaponAbilitySlot.PRIMARY), second = Augmentations.activeId(stack, WeaponAbilitySlot.SECONDARY);
        int count = Augmentations.count(stack);
        swap(player);
        test.assertTrue(Augmentations.activeId(stack, WeaponAbilitySlot.PRIMARY).equals(second)
                && Augmentations.activeId(stack, WeaponAbilitySlot.SECONDARY).equals(first), "Order not swapped");
        test.assertTrue(Augmentations.count(stack) == count && off.getComponentsPatch().equals(beforeOff), "Swap augmented gear or changed offhand");
        stack = Augmentations.apply(stack, second);
        test.assertTrue(Augmentations.activeId(stack, WeaponAbilitySlot.PRIMARY).equals(second), "Upgrade reset ordering");
        ItemStack infused = new ItemStack(ModItems.DEAD_CALM_DAGGER_AMBER, 1, stack.getComponentsPatch());
        ItemStack serialized = ItemStack.parseOptional(test.getLevel().registryAccess(), (net.minecraft.nbt.CompoundTag)infused.save(test.getLevel().registryAccess()));
        test.assertTrue(Augmentations.activeId(serialized, WeaponAbilitySlot.PRIMARY).equals(second) && Augmentations.level(serialized, second) == 2, "Infusion/save lost order or rank");
        player.setItemInHand(InteractionHand.MAIN_HAND, serialized); swap(player);
        test.assertTrue(Augmentations.activeId(serialized, WeaponAbilitySlot.PRIMARY).equals(first), "Second swap did not restore order");
        ItemStack one = Augmentations.apply(new ItemStack(ModItems.DEAD_CALM_DAGGER.get()), first);
        player.setItemInHand(InteractionHand.MAIN_HAND, one); var before = one.getComponentsPatch(); swap(player);
        test.assertTrue(one.getComponentsPatch().equals(before), "One active ability was swapped");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CLAYMORE.get())); swap(player);
        test.assertFalse(player.getMainHandItem().has(DataComponents.CUSTOM_DATA), "Offhand abilities allowed mainhand swap");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void swapCancelsArmedAbilitiesWithoutBypassingCooldownOnOtherStacks(GameTestHelper test) throws Exception {
        var player = test.makeMockServerPlayerInLevel();
        ItemStack stack = TestWeapons.ready(ModItems.EMBERFANG_DAGGER.get());
        // Same variant, independently unswapped stack must share cooldown by ability identity.
        ItemStack another = stack.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack); player.setRemainingFireTicks(200);
        use(player, 0); use(player, 1);
        var runtime = Augmentations.canonical(stack);
        test.assertTrue(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY) && runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Fixture did not arm both abilities");
        swap(player);
        test.assertFalse(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY) || runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Swap left armed effects running");
        var cooldowns = player.getData(WeaponCooldownAttachments.COOLDOWNS); long now = player.level().getGameTime();
        test.assertTrue(cooldowns.get(WeaponCooldownKey.of(item(stack), WeaponAbilitySlot.PRIMARY)) == now + 600
                && cooldowns.get(WeaponCooldownKey.of(item(stack), WeaponAbilitySlot.SECONDARY)) == now + 1000, "Cooldown did not follow ability identity");
        use(player, 0); use(player, 1);
        test.assertFalse(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY) || runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Swapping bypassed cooldown");
        player.setItemInHand(InteractionHand.MAIN_HAND, another); use(player, 0); use(player, 1);
        test.assertFalse(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY) || runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Unswapped stack bypassed variant cooldown");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
