package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.AugmentationAnimation;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownKey;
import net.bananashelp20.forgermod.screen.custom.AugmentationTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.lang.reflect.Proxy;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AugmentationGameTests {
    @GameTest(template = "riftfang_test")
    public static void withdrawnEntriesAreInertAndMaterialHitMigratesAcrossAllVariants(GameTestHelper test) {
        for (var entry : ModItems.ITEMS.getEntries()) {
            ItemStack stack = new ItemStack(entry.get());
            if (!Augmentations.eligible(stack)) continue;
            var old = new CompoundTag(); var data = new CompoundTag(); var levels = new net.minecraft.nbt.ListTag();
            for (String id : new String[]{"augmentation.forgermod.guarded", "augmentation.forgermod.empowered_hit",
                    Augmentations.canonical(new ItemStack(ModItems.DEAD_CALM_DAGGER.get())).abilityDescriptionKey(WeaponAbilitySlot.SECONDARY)}) {
                var row = new CompoundTag(); row.putString("id", id); row.putInt("level", 4); levels.add(row);
            }
            data.put("levels", levels); data.putInt("version", 2); data.putInt("count", 12); old.put("forgermod_augments", data);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(old));
            test.assertTrue(Augmentations.level(stack, Augmentations.MATERIAL_HIT) == 4 && Augmentations.count(stack) == 12,
                    "Material learning or historical count lost during migration");
            test.assertFalse(Augmentations.hasPassive(stack, "augmentation.forgermod.guarded"), "Saved Guarded still active");
            test.assertTrue(Augmentations.pool(stack).stream().noneMatch(a -> a.id().contains("guarded") || a.id().contains("empowered_hit")),
                    "Withdrawn ability still offered");
            if (!((SwordItemWithEffect)stack.getItem()).isDagger()) {
                test.assertTrue(Augmentations.ids(stack, true).isEmpty() && Augmentations.ids(stack, false).isEmpty(), "Copied dagger abilities remain");
                test.assertTrue(Augmentations.pool(stack).size() == 1
                        +net.bananashelp20.forgermod.augmentation.RecommendedAbilities.pool(stack).size(), "Claymore/axe has unapproved ability assignments");
            }
        }
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void withdrawnPendingChoicesRepairWithoutLosingGear(GameTestHelper test) {
        var table=table(test);
        table.inventory.setStackInSlot(0,new ItemStack(ModItems.OVERGROWN_CLAYMORE.get()));
        table.inventory.setStackInSlot(1,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get(),2));
        table.inventory.setStackInSlot(2,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get(),2));
        table.tick();
        CompoundTag saved=table.saveWithoutMetadata(test.getLevel().registryAccess());
        saved.putInt("phase",AugmentationAnimation.CHOOSE); saved.putString("first","augmentation.forgermod.guarded");
        saved.putString("second","tooltips.forgermod.ability.rooting_roots");
        table.loadWithComponents(saved,test.getLevel().registryAccess()); table.tick();
        test.assertTrue(table.data.get(0)==AugmentationAnimation.CHOOSE && table.data.get(3)>0 && table.data.get(4)>0,"Removed pending offers stuck");
        test.assertTrue(table.inventory.getStackInSlot(1).getCount()==2 && table.inventory.getStackInSlot(2).getCount()==2,"Repair charged ingredients");
        test.assertTrue(table.choose(0),"Repaired offer cannot be chosen");
        table.inventory.extractItem(3,1,false);
        ItemStack maxed=new ItemStack(ModItems.OVERGROWN_CLAYMORE.get());
        while(!Augmentations.available(maxed).isEmpty()) maxed=Augmentations.apply(maxed,Augmentations.available(maxed).getFirst().id());
        table.inventory.setStackInSlot(0,maxed);
        saved=table.saveWithoutMetadata(test.getLevel().registryAccess()); saved.putInt("phase",AugmentationAnimation.CHOOSE);
        saved.putString("first","augmentation.forgermod.guarded"); saved.putString("second","augmentation.forgermod.guarded");
        table.loadWithComponents(saved,test.getLevel().registryAccess()); table.tick();
        test.assertFalse(table.locked(),"No legal choices still lock ingredients");
        test.assertTrue(Augmentations.level(table.inventory.getStackInSlot(0),Augmentations.MATERIAL_HIT)==4,"Repair changed gear");
        test.assertTrue(table.inventory.getStackInSlot(1).getCount()==1 && table.inventory.getStackInSlot(2).getCount()==1,"Cancellation duplicated or lost ingredients");
        table.tick(); test.assertTrue(table.inventory.getStackInSlot(1).getCount()==1,"Repeated refund");
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void allMaterialVariantsRespectSlotsRanksAndOffers(GameTestHelper test) {
        int checked = 0;
        for (var entry : ModItems.ITEMS.getEntries()) {
            ItemStack stack = new ItemStack(entry.get());
            if (!Augmentations.eligible(stack)) continue;
            checked++;
            test.assertTrue(Augmentations.ids(stack, true).isEmpty() && Augmentations.ids(stack, false).isEmpty()
                    && Augmentations.level(stack, Augmentations.MATERIAL_HIT) == 0, "Fresh item has learned abilities");
            test.assertTrue(Augmentations.duration(stack) == 100, "Initial cook time is not five seconds");
            var random = RandomSource.create(123);
            while (!Augmentations.available(stack).isEmpty()) {
                var offers = Augmentations.offers(stack, random);
                test.assertTrue(offers.size() == 2, "Missing choice");
                test.assertTrue(Augmentations.available(stack).containsAll(offers), "Illegal offer");
                if (Augmentations.available(stack).size() > 1)
                    test.assertFalse(offers.get(0).equals(offers.get(1)), "Duplicate choice despite alternatives");
                int oldCount = Augmentations.count(stack);
                stack = Augmentations.apply(stack, offers.get(0).id());
                test.assertTrue(!stack.isEmpty() && Augmentations.count(stack) == oldCount + 1, "Augment did not apply once");
                test.assertTrue(Augmentations.duration(stack) == 100 + 20 * (oldCount + 1), "Cook scaling wrong");
                test.assertTrue(Augmentations.ids(stack, true).size() <= 2 && Augmentations.ids(stack, false).size() <= 2, "Slot cap exceeded");
                test.assertTrue(Augmentations.count(stack) <= 20, "Augmentation loop did not terminate");
            }
            test.assertTrue(Augmentations.offers(stack, random).isEmpty(), "Maxed item still has choices");
            for (boolean active : new boolean[]{false, true}) for (String id : Augmentations.ids(stack, active)) {
                test.assertTrue(Augmentations.level(stack, id) == net.bananashelp20.forgermod.augmentation.RecommendedAbilities.maxRank(id), "Ability did not reach its maximum rank");
                test.assertTrue(Augmentations.apply(stack, id).isEmpty(), "Ability exceeded its maximum rank");
            }
        }
        test.assertTrue(checked == 135, "Not all 135 special-material variants were checked: " + checked);
        test.assertFalse(Augmentations.eligible(new ItemStack(Items.DIAMOND_SWORD)), "Ordinary sword accepted");
        test.assertFalse(Augmentations.eligible(new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get())), "Ingredient accepted as gear");
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void freshGearHasNoMaterialEffectAndLegacyAugmentsKeepNativeAbilities(GameTestHelper test) {
        ItemStack fresh = new ItemStack(ModItems.DEATHWISPER_DAGGER.get());
        var weapon = (SwordItemWithEffect)fresh.getItem();
        Cow target = EntityType.COW.create(test.getLevel());
        weapon.applyMaterialEffect(target, fresh);
        test.assertFalse(target.hasEffect(MobEffects.WEAKNESS), "Fresh weapon still applies unlearned material effect");
        ItemStack learned = Augmentations.apply(fresh, Augmentations.MATERIAL_HIT);
        weapon.applyMaterialEffect(target, learned);
        test.assertTrue(target.hasEffect(MobEffects.WEAKNESS), "Learning material hit has no combat effect");
        test.assertTrue(Augmentations.ids(learned, false).isEmpty(), "Material hit consumed a unique passive slot");
        CompoundTag oldData = new CompoundTag(), augments = new CompoundTag();
        augments.putInt("count", 1);
        var levels = new net.minecraft.nbt.ListTag(); var row = new CompoundTag();
        row.putString("id", "tooltips.forgermod.ability.storing_anger"); row.putInt("level", 2); levels.add(row);
        augments.put("levels", levels); oldData.put("forgermod_augments", augments);
        ItemStack legacy = fresh.copy(); legacy.set(DataComponents.CUSTOM_DATA, CustomData.of(oldData));
        test.assertTrue(Augmentations.ids(legacy, true).size() == 2 && Augmentations.ids(legacy, false).size() == 2
                && Augmentations.level(legacy, Augmentations.MATERIAL_HIT) == 1, "Existing augmented item lost its native abilities");
        ItemStack migrated = Augmentations.apply(legacy, "tooltips.forgermod.ability.storing_anger");
        test.assertTrue(Augmentations.level(migrated, "tooltips.forgermod.ability.storing_anger") == 3
                && Augmentations.ids(migrated, true).size() == 2 && Augmentations.ids(migrated, false).size() == 2,
                "Upgrade did not preserve migrated abilities");
        test.succeed();
    }

    private static AugmentationTableBlockEntity table(GameTestHelper test) {
        BlockPos pos = test.absolutePos(new BlockPos(1, 2, 1));
        test.getLevel().setBlockAndUpdate(pos, ModBlocks.AUGMENTATION_TABLE.get().defaultBlockState());
        return (AugmentationTableBlockEntity)test.getLevel().getBlockEntity(pos);
    }

    @GameTest(template = "riftfang_test")
    public static void cookingPersistenceInventoryLocksAndSingleChoice(GameTestHelper test) {
        var table = table(test);
        var player = test.makeMockServerPlayerInLevel();
        player.setPos(table.getBlockPos().getCenter());
        ItemStack input = new ItemStack(ModItems.OVERGROWN_CLAYMORE.get());
        input.setDamageValue(12);
        input.set(DataComponents.CUSTOM_NAME, Component.literal("Keep my name"));
        CompoundTag custom = new CompoundTag(); custom.putString("other_mod", "keep me");
        input.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
        table.inventory.setStackInSlot(0, input);
        table.inventory.setStackInSlot(1, new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get(), 2));
        table.inventory.setStackInSlot(2, new ItemStack(Items.LAPIS_LAZULI));
        table.tick();
        test.assertTrue(table.data.get(0) == 0 && table.inventory.getStackInSlot(1).getCount() == 2, "Invalid input consumed template");
        table.inventory.setStackInSlot(2, new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get(), 2));
        table.tick();
        test.assertTrue(table.locked() && table.data.get(2) == 100, "Cooking failed to start");
        test.assertTrue(table.inventory.getStackInSlot(1).getCount() == 2 && table.inventory.getStackInSlot(2).getCount() == 2, "Ingredients consumed before selection");
        var menu = new AugmentationTableMenu(0, player.getInventory(), table, table.data);
        test.assertTrue(menu.getSlot(0).mayPickup(player), "Weapon cannot cancel crafting");
        test.assertFalse(menu.getSlot(1).mayPlace(new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get())), "Cooking inputs can be changed");
        test.assertFalse(menu.clickMenuButton(player, 0), "Premature choice succeeded");
        for (int i = 0; i < 99; i++) table.tick();
        test.assertTrue(table.data.get(0) == 1, "Choices appeared before five seconds");
        table.tick();
        test.assertTrue(table.data.get(0) == AugmentationAnimation.COVER, "First overlay did not advance to second");
        while (table.data.get(0) < AugmentationAnimation.CHOOSE) table.tick();
        var first = menu.offer(0); var second = menu.offer(1);
        test.assertTrue(first != null && second != null, "Missing legal choices");
        CompoundTag saved = table.saveWithoutMetadata(test.getLevel().registryAccess());
        table.loadWithComponents(saved, test.getLevel().registryAccess());
        test.assertTrue(menu.offer(0).equals(first) && menu.offer(1).equals(second) && table.data.get(0) == AugmentationAnimation.CHOOSE, "Reload rerolled or reset choices");
        test.assertFalse(menu.clickMenuButton(player, -1) || menu.clickMenuButton(player, 2), "Invalid choice accepted");
        test.assertTrue(menu.clickMenuButton(player, 0), "Valid choice failed");
        test.assertFalse(menu.clickMenuButton(player, 0), "Replayed choice accepted");
        test.assertTrue(table.inventory.getStackInSlot(1).getCount()==1 && table.inventory.getStackInSlot(2).getCount()==1
                && table.data.get(0)==AugmentationAnimation.COMPLETE && table.data.get(5)==0,"Selection did not consume exactly one pair or persist selected card");
        ItemStack result = table.inventory.getStackInSlot(3);
        test.assertTrue(Augmentations.count(result) == 1 && Augmentations.level(result, first.id()) == 1, "Wrong learned ability");
        test.assertTrue(result.getDamageValue() == 12 && result.getHoverName().getString().equals("Keep my name"), "Damage/name lost");
        test.assertTrue(result.get(DataComponents.CUSTOM_DATA).copyTag().getString("other_mod").equals("keep me"), "Other custom data lost");
        test.assertTrue(table.inventory.getStackInSlot(0).isEmpty() && menu.getSlot(3).mayPickup(player), "Output inventory broken");
        test.assertTrue(!menu.quickMoveStack(player, 3).isEmpty() && table.inventory.getStackInSlot(3).isEmpty(), "Output shift click failed");
        test.assertTrue(table.data.get(0)==0 && menu.getSlot(0).isActive() && !menu.getSlot(3).isActive(),"Taking result did not reset GUI");
        player.setPos(table.getBlockPos().getCenter().add(20, 0, 0));
        test.assertFalse(menu.stillValid(player) || menu.clickMenuButton(player, 1), "Remote choice allowed");
        test.getLevel().getServer().getPlayerList().remove(player);
        test.succeed();
    }

    private static void use(ServerPlayer player, WeaponAbilitySlot slot) throws Exception {
        var context = (IPayloadContext)Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(), new Class[]{IPayloadContext.class},
                (proxy, method, arguments) -> method.getName().equals("player") ? player : null);
        var handler = WeaponAbilityNetwork.class.getDeclaredMethod("handleAbility", WeaponAbilityNetwork.UseAbilityPayload.class, IPayloadContext.class);
        handler.setAccessible(true);
        handler.invoke(null, new WeaponAbilityNetwork.UseAbilityPayload(slot.ordinal(), player.getInventory().selected,
                BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString()), context);
    }

    @GameTest(template = "riftfang_test")
    public static void learnedSecondaryBindsFirstKeyAndRanksKeepApprovedCooldown(GameTestHelper test) throws Exception {
        var player = test.makeMockServerPlayerInLevel();
        player.setPos(test.getBounds().getCenter());
        ItemStack stack = new ItemStack(ModItems.EMBERFANG_DAGGER.get());
        String id = Augmentations.canonical(stack).abilityDescriptionKey(WeaponAbilitySlot.SECONDARY);
        for (int i = 0; i < 4; i++) stack = Augmentations.apply(stack, id);
        test.assertTrue(Augmentations.sourceSlot(stack, WeaponAbilitySlot.PRIMARY) == WeaponAbilitySlot.SECONDARY, "Acquired secondary source mapping wrong");
        test.assertTrue(Augmentations.activeId(stack, WeaponAbilitySlot.SECONDARY) == null, "Second physical slot unexpectedly filled");
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var runtime = Augmentations.canonical(stack);
        player.setRemainingFireTicks(200);
        use(player, WeaponAbilitySlot.PRIMARY);
        test.assertTrue(runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Dagger could not activate learned ability");
        Cow cow = EntityType.COW.create(test.getLevel()); cow.setPos(player.position().add(0, 0, 2));
        test.getLevel().addFreshEntity(cow);
        ((SwordItemWithEffect)stack.getItem()).postHurtEnemy(stack, cow, player);
        test.assertFalse(runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Dagger hit did not consume learned ability");
        test.assertTrue(cow.isOnFire(), "Learned infernal hit had no effect");
        var update = WeaponAbilityNetwork.class.getDeclaredMethod("updateSession", ServerPlayer.class); update.setAccessible(true); update.invoke(null, player);
        String key = WeaponCooldownKey.of(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), WeaponAbilitySlot.SECONDARY);
        long remaining = player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key) - player.level().getGameTime();
        test.assertTrue(remaining == runtime.abilityCooldownTicks(WeaponAbilitySlot.SECONDARY), "Rank IV cooldown wrong");
        cow.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void materialRanksKeepApprovedDurationAndPreserveGemConversion(GameTestHelper test) {
        ItemStack stack = new ItemStack(ModItems.OVERGROWN_CLAYMORE.get());
        for (int i = 0; i < 4; i++) stack = Augmentations.apply(stack, Augmentations.MATERIAL_HIT);
        Cow baseline = EntityType.COW.create(test.getLevel()), augmented = EntityType.COW.create(test.getLevel());
        var weapon = (SwordItemWithEffect)stack.getItem();
        weapon.applyMaterialEffect(baseline); weapon.applyMaterialEffect(augmented, stack);
        test.assertTrue(augmented.getEffect(MobEffects.HUNGER).getDuration() == baseline.getEffect(MobEffects.HUNGER).getDuration(), "Unapproved material duration bonus remains");
        ItemStack converted = new ItemStack(ModItems.OVERGROWN_CLAYMORE_JADE.get().builtInRegistryHolder(), 1, stack.getComponentsPatch());
        test.assertTrue(Augmentations.level(converted, Augmentations.MATERIAL_HIT) == 4 && Augmentations.count(converted) == 4, "Gem conversion lost upgrades");
        var table = table(test); table.inventory.setStackInSlot(0, converted);
        test.assertTrue(Augmentations.duration(converted) == 180, "Previous augment cook duration wrong");
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void brokenWeaponCancelsLearnedSecondaryAndBothCooldownsRemainIndependent(GameTestHelper test) throws Exception {
        var player = test.makeMockServerPlayerInLevel();
        ItemStack stack = new ItemStack(ModItems.EMBERFANG_DAGGER.get());
        var runtime = Augmentations.canonical(stack);
        String secondary = runtime.abilityDescriptionKey(WeaponAbilitySlot.SECONDARY);
        String primary = runtime.abilityDescriptionKey(WeaponAbilitySlot.PRIMARY);
        stack = Augmentations.apply(Augmentations.apply(stack, secondary), primary);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setRemainingFireTicks(200);
        use(player, WeaponAbilitySlot.PRIMARY);
        use(player, WeaponAbilitySlot.SECONDARY);
        test.assertTrue(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY) && runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Both learned slots could not run together");
        String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        stack.setCount(0); // Durability break invalidates the equipped item and its augmentation lookups.
        var update = WeaponAbilityNetwork.class.getDeclaredMethod("updateSession", ServerPlayer.class); update.setAccessible(true); update.invoke(null, player);
        test.assertFalse(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY) || runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Broken weapon left an armed ability behind");
        var deadlines = player.getData(WeaponCooldownAttachments.COOLDOWNS);
        long now = player.level().getGameTime();
        test.assertTrue(deadlines.get(WeaponCooldownKey.of(item, WeaponAbilitySlot.SECONDARY)) == now + 1000
                && deadlines.get(WeaponCooldownKey.of(item, WeaponAbilitySlot.PRIMARY)) == now + 600, "Swapped learned slots lost independent cooldowns on break");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void learnedDaggerCriticalCompletes(GameTestHelper test) throws Exception {
        var player = test.makeMockServerPlayerInLevel();
        ItemStack stack = ModItems.ITEMS.getEntries().stream().map(e -> new ItemStack(e.get()))
                .filter(s -> s.getItem() instanceof net.bananashelp20.forgermod.item.custom.VulnusiumWeapon w && w.isDagger()).findFirst().orElseThrow();
        String deep = Augmentations.canonical(stack).abilityDescriptionKey(WeaponAbilitySlot.PRIMARY);
        stack = Augmentations.apply(stack, deep);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        use(player, WeaponAbilitySlot.PRIMARY);
        Cow cow = EntityType.COW.create(test.getLevel());
        cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100); cow.setHealth(100);
        var crit = new net.neoforged.neoforge.event.entity.player.CriticalHitEvent(player, cow, 1.5F, true);
        net.bananashelp20.forgermod.item.custom.abilities.DaggerCriticalEvents.onCriticalHit(crit);
        net.bananashelp20.forgermod.item.custom.VulnusiumWeapon.onCriticalHit(crit);
        test.assertTrue(crit.getDamageMultiplier() == 3, "Learned dagger critical did not gain damage");
        ((SwordItemWithEffect)stack.getItem()).postHurtEnemy(stack, cow, player);
        test.assertTrue(cow.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && cow.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 2, "Learned dagger Deep Wound failed to slow target");
        test.assertFalse(Augmentations.canonical(stack).isAbilityActive(player, WeaponAbilitySlot.PRIMARY), "Learned critical charge not consumed");
        var update = WeaponAbilityNetwork.class.getDeclaredMethod("updateSession", ServerPlayer.class); update.setAccessible(true); update.invoke(null, player);
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
