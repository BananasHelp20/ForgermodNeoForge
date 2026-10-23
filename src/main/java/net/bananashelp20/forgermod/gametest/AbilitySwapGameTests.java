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
    @GameTest(template = "riftfang_test")
    public static void threeAbilitiesCycleThroughPacketsAndKeepOrderAcrossUpgradeInfusionAndExtraction(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);
        var ids=java.util.List.of(net.bananashelp20.forgermod.augmentation.AdditionalAbilities.STATIC_BLADE,
                net.bananashelp20.forgermod.augmentation.AdditionalAbilities.ALTERNATING_CURRENT,
                net.bananashelp20.forgermod.augmentation.AdditionalAbilities.DIRECT_CURRENT);
        var stack=new ItemStack(ModItems.STATIC_DAGGER.get());for(var id:ids)stack=Augmentations.apply(stack,id);
        stack=Augmentations.apply(stack,Augmentations.DUAL_WIELD);
        var off=new ItemStack(ModItems.STATIC_DAGGER_JADE.get());player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setItemInHand(InteractionHand.OFF_HAND,off);
        var offData=off.getComponentsPatch();var passives=Augmentations.ids(stack,false);int count=Augmentations.count(stack);
        try {
            for(int cycle=1;cycle<=3;cycle++) {
                swap(player);
                test.assertTrue(Augmentations.ids(stack,true).equals(java.util.List.of(ids.get(cycle%3),ids.get((cycle+1)%3),ids.get((cycle+2)%3))),"Wrong three-ability rotation at step "+cycle);
                test.assertTrue(Augmentations.count(stack)==count && Augmentations.ids(stack,false).equals(passives) && off.getComponentsPatch().equals(offData),"Rotation changes augments, passives or offhand");
            }
            swap(player);swap(player); // C,A,B
            stack=Augmentations.apply(stack,ids.get(1));
            var infused=new ItemStack(ModItems.STATIC_DAGGER_AMBER,1,stack.getComponentsPatch());
            var saved=ItemStack.parseOptional(test.getLevel().registryAccess(),(net.minecraft.nbt.CompoundTag)infused.save(test.getLevel().registryAccess()));
            test.assertTrue(Augmentations.ids(saved,true).equals(java.util.List.of(ids.get(2),ids.get(0),ids.get(1))) && Augmentations.level(saved,ids.get(1))==2,"Upgrade, infusion or saving loses three-slot order");
            var extracted=Augmentations.extractFirst(saved,new ItemStack(ModItems.AUGMENT_TEMPLATE.get()));
            test.assertTrue(extracted!=null && Augmentations.level(extracted.template(),ids.get(0))==1,"Rotation changes which augment was learned first");
            var relearned=Augmentations.apply(extracted.weapon(),ids.get(0));
            test.assertTrue(Augmentations.ids(relearned,true).equals(java.util.List.of(ids.get(2),ids.get(1),ids.get(0))),"Relearning an extracted ability does not append after surviving assignments");
            var legacy=stack.copy();var data=legacy.get(DataComponents.CUSTOM_DATA).copyTag();var augments=data.getCompound("forgermod_augments");augments.remove("active_order");augments.putBoolean("swapped",true);legacy.set(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));
            test.assertTrue(Augmentations.ids(legacy,true).equals(java.util.List.of(ids.get(1),ids.get(0),ids.get(2))),"Legacy swapped order not preserved");
            Augmentations.swapActives(legacy);test.assertTrue(Augmentations.ids(legacy,true).equals(java.util.List.of(ids.get(0),ids.get(2),ids.get(1))),"Legacy order does not migrate into a left rotation");
            test.succeed();
        } finally {WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
    }
    @GameTest(template = "riftfang_test")
    public static void rotatingThreeArmedAbilitiesKeepsCooldownsOnEveryKeyAndOtherStacks(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);
        player.setNoGravity(true);player.setPos(test.getBounds().getCenter());player.setXRot(-90);
        var marked=net.minecraft.world.entity.EntityType.ZOMBIE.create(test.getLevel());marked.setNoAi(true);marked.setNoGravity(true);marked.setPos(player.position().add(0,4,0));test.getLevel().addFreshEntity(marked);
        var ids=java.util.List.of(net.bananashelp20.forgermod.augmentation.AdditionalAbilities.STATIC_BLADE,
                net.bananashelp20.forgermod.augmentation.AdditionalAbilities.ALTERNATING_CURRENT,
                net.bananashelp20.forgermod.augmentation.AdditionalAbilities.DIRECT_CURRENT);
        var stack=new ItemStack(ModItems.STATIC_DAGGER.get());for(var id:ids)stack=Augmentations.apply(stack,id);var original=stack.copy();player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try {
            for(int slot=0;slot<3;slot++)use(player,slot);
            test.assertTrue(ids.stream().allMatch(id->net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime.active(player,id)),"Three armed fixtures not active");
            long now=player.level().getGameTime();
            for(int rotation=0;rotation<3;rotation++) {
                swap(player);for(int slot=0;slot<3;slot++)use(player,slot);
                test.assertFalse(ids.stream().anyMatch(id->net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime.active(player,id)),"Rotation leaves armed effects or bypasses cooldown");
                var cooldowns=player.getData(WeaponCooldownAttachments.COOLDOWNS);
                for(int i=0;i<3;i++)test.assertTrue(java.util.Objects.equals(cooldowns.get(item(stack)+":"+ids.get(i)),now+(i==0?800:2400)),"Cooldown no longer follows rotated ability identity");
            }
            player.setItemInHand(InteractionHand.MAIN_HAND,original);for(int slot=0;slot<3;slot++)use(player,slot);
            test.assertFalse(ids.stream().anyMatch(id->net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime.active(player,id)),"Unrotated stack bypasses cooldowns");marked.discard();test.succeed();
        } finally {WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
    }
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
        ItemStack stack = TestWeapons.ready(ModItems.DEATHWISPER_DAGGER.get());
        // Same variant, independently unswapped stack must share cooldown by ability identity.
        ItemStack another = stack.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack); player.setRemainingFireTicks(200);
        use(player, 0); use(player, 1);
        var runtime = Augmentations.canonical(stack);
        test.assertTrue(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY) && runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Fixture did not arm both abilities");
        swap(player);
        test.assertFalse(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY) || runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Swap left armed effects running");
        var cooldowns = player.getData(WeaponCooldownAttachments.COOLDOWNS); long now = player.level().getGameTime();
        test.assertTrue(cooldowns.get(WeaponCooldownKey.of(item(stack), WeaponAbilitySlot.PRIMARY)) == now + 1800
                && cooldowns.get(WeaponCooldownKey.of(item(stack), WeaponAbilitySlot.SECONDARY)) == now + 1200, "Cooldown did not follow ability identity");
        use(player, 0); use(player, 1);
        test.assertFalse(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY) || runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Swapping bypassed cooldown");
        player.setItemInHand(InteractionHand.MAIN_HAND, another); use(player, 0); use(player, 1);
        test.assertFalse(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY) || runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Unswapped stack bypassed variant cooldown");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
