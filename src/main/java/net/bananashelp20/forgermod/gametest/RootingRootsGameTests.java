package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class RootingRootsGameTests {
    private static java.util.List<net.bananashelp20.forgermod.item.custom.abilities.RootingRootsVisuals.Root> models(GameTestHelper test,Vec3 center) {
        return test.getLevel().getEntitiesOfClass(net.bananashelp20.forgermod.item.custom.abilities.RootingRootsVisuals.Root.class,net.minecraft.world.phys.AABB.ofSize(center,12,12,12));
    }
    private static float height(net.bananashelp20.forgermod.item.custom.abilities.RootingRootsVisuals.Root root) {
        var data=root.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        return com.mojang.math.Transformation.EXTENDED_CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,data.get("transformation")).getOrThrow().getScale().y;
    }
    @GameTest(template="riftfang_test",timeoutTicks=240)
    public static void rootModelsGrowHoldAndRetractOnNaturalExpiry(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var start=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(start);
        for(int x=5;x<=13;x++)for(int z=5;z<=13;z++){test.setBlock(new BlockPos(x,4,z),net.minecraft.world.level.block.Blocks.STONE);for(int y=5;y<=8;y++)test.setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);}
        var mob=target(test,start.add(0,0,2));var stack=net.bananashelp20.forgermod.augmentation.Augmentations.apply(new ItemStack(ModItems.LEAFCUTTER_DAGGER.get()),"tooltips.forgermod.ability.rooting_roots");player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        AbilityTestPackets.use(player,0);var roots=models(test,start);
        test.assertTrue(roots.size()==3 && roots.stream().allMatch(r->height(r)<.01),"Roots do not start buried with three bounded models");
        test.runAfterDelay(2,()->{
            test.assertTrue(roots.stream().allMatch(r->height(r)>.5 && r.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getCompound("block_state").getString("Name").equals("minecraft:mangrove_roots")
                && Math.abs(r.getY()-start.y)<.001 && !r.shouldBeSaved() && r.noPhysics && r.isNoGravity()
                && r.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getInt("interpolation_duration")==10),"Growth transform, ground origin or safe display properties are wrong");
        });
        test.runAfterDelay(199,()->test.assertTrue(models(test,start).size()==3 && ((LushWeapon)stack.getItem()).isAbilityActive(player,WeaponAbilitySlot.PRIMARY),"Roots disappear before the hold ends"));
        test.runAfterDelay(201,()->{
            LushWeapon.onLevelTick(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(()->true,test.getLevel()));
            test.assertTrue(roots.stream().allMatch(r->!r.isRemoved() && height(r)<.01),"Expired roots vanish instead of interpolating back into ground");
            var free=mob.position().add(1,0,0);mob.setPos(free);LushWeapon.onEntityTick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(mob));test.assertTrue(mob.position().equals(free),"Retracting visuals keep target bound");
        });
        test.runAfterDelay(215,()->{
            try {test.assertTrue(roots.stream().allMatch(net.minecraft.world.entity.Entity::isRemoved),"Retracted roots leave display entities behind");test.assertTrue(test.getBlockState(new BlockPos(8,4,10)).is(net.minecraft.world.level.block.Blocks.STONE),"Root animation changes terrain");test.succeed();}
            finally {LushWeapon.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));mob.discard();player.server.getPlayerList().remove(player);}
        });
    }
    @GameTest(template="riftfang_test",timeoutTicks=60)
    public static void canceledRootHoldRetractsModelsAndReleasesImmediately(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var start=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(start);
        for(int x=6;x<=10;x++)for(int z=6;z<=12;z++){test.setBlock(new BlockPos(x,4,z),net.minecraft.world.level.block.Blocks.STONE);for(int y=5;y<=8;y++)test.setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);}
        var mob=target(test,start.add(0,0,2));var stack=net.bananashelp20.forgermod.augmentation.Augmentations.apply(new ItemStack(ModItems.LEAFCUTTER_DAGGER.get()),"tooltips.forgermod.ability.rooting_roots");player.setItemInHand(InteractionHand.MAIN_HAND,stack);AbilityTestPackets.use(player,0);var roots=models(test,start);
        test.runAfterDelay(15,()->{
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
            test.assertTrue(roots.size()==3 && roots.stream().allMatch(r->height(r)<.01 && !r.isRemoved()),"Switch does not start retraction");
            var free=mob.position().add(1,0,0);mob.setPos(free);LushWeapon.onEntityTick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(mob));test.assertTrue(mob.position().equals(free),"Switch leaves target bound during retraction");
        });
        test.runAfterDelay(29,()->{try {test.assertTrue(roots.stream().allMatch(net.minecraft.world.entity.Entity::isRemoved),"Canceled root models remain after retraction");test.succeed();}finally {LushWeapon.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));mob.discard();player.server.getPlayerList().remove(player);}});
    }
    private static Creeper target(GameTestHelper test, Vec3 position) {
        Creeper target = EntityType.CREEPER.create(test.getLevel());
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setPos(position);
        test.getLevel().addFreshEntity(target);
        return target;
    }

    @GameTest(template = "riftfang_test")
    public static void rootsReachTenBlocks(GameTestHelper test) {
        var player = test.makeMockServerPlayerInLevel();
        player.setNoGravity(true);
        Vec3 start = Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8, 8, 8)));
        player.setPos(start);
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(ModItems.LEAFCUTTER_DAGGER.get()));
        LushWeapon weapon = (LushWeapon)player.getMainHandItem().getItem();
        Creeper inside = target(test, start.add(0, 0, 9.5));
        Creeper edge = target(test, start.add(10, 0, 0));
        Creeper outside = target(test, start.add(10.25, 0, 0));
        test.assertTrue(weapon.activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "No roots activated at expanded range");
        test.assertTrue(inside.getEffect(MobEffects.POISON).getDuration() == 200 && edge.hasEffect(MobEffects.POISON), "Expanded targets were not poisoned");
        test.assertFalse(outside.hasEffect(MobEffects.POISON), "Root range exceeded ten blocks");
        inside.setPos(inside.position().add(1, 0, 0));
        test.runAfterDelay(3, () -> {
            test.assertTrue(Math.abs(inside.getX() - start.x) < .0001, "Expanded target was not anchored");
            test.assertTrue(weapon.abilityCooldownTicks(WeaponAbilitySlot.PRIMARY) == 1200, "Root cooldown changed");
            weapon.cancelAbility(player, WeaponAbilitySlot.PRIMARY);
            test.getLevel().getServer().getPlayerList().remove(player);
            test.succeed();
        });
    }
}
