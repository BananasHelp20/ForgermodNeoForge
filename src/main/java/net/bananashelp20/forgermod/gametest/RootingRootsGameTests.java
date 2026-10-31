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
    @GameTest(template="riftfang_test",timeoutTicks=250)
    public static void jadeImmobilizationAndPoisonBothLastElevenSeconds(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var start=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(start);
        for(int x=6;x<=10;x++)for(int z=6;z<=12;z++) {test.setBlock(new BlockPos(x,4,z),net.minecraft.world.level.block.Blocks.STONE);for(int y=5;y<=8;y++)test.setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);}
        var mob=target(test,start.add(0,0,2));var anchor=mob.position();
        var stack=net.bananashelp20.forgermod.augmentation.Augmentations.apply(new ItemStack(ModItems.LEAFCUTTER_DAGGER_JADE.get()),"tooltips.forgermod.ability.rooting_roots");player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        AbilityTestPackets.use(player,0);
        test.assertTrue(mob.getEffect(MobEffects.POISON)!=null && mob.getEffect(MobEffects.POISON).getDuration()==220 && mob.getEffect(MobEffects.POISON).getAmplifier()==1,"Jade poison no longer lasts eleven seconds at level II");
        test.runAfterDelay(201,()->{
            mob.setPos(anchor.add(1,0,0));LushWeapon.onEntityTick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(mob));
            test.assertTrue(mob.position().distanceTo(anchor)<.001 && ((LushWeapon)stack.getItem()).isAbilityActive(player,WeaponAbilitySlot.PRIMARY),"Jade roots expire at the old ten-second boundary");
        });
        test.runAfterDelay(221,()->{try{
            var free=anchor.add(1,0,0);mob.setPos(free);LushWeapon.onEntityTick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(mob));
            test.assertTrue(mob.position().equals(free) && !((LushWeapon)stack.getItem()).isAbilityActive(player,WeaponAbilitySlot.PRIMARY),"Jade immobilization does not end after eleven seconds");
            test.assertFalse(mob.hasEffect(MobEffects.POISON),"Poison and roots expire at different times");test.succeed();
        }finally{LushWeapon.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));mob.discard();player.server.getPlayerList().remove(player);}});
    }
    @GameTest(template="empty")
    public static void reviewedRootDescriptionUsesWhiteRangeAndConditionalDuration(GameTestHelper test) {
        for(var item:new net.minecraft.world.item.Item[]{ModItems.LEAFCUTTER_DAGGER.get(),ModItems.LEAFCUTTER_DAGGER_RUBY.get(),ModItems.LEAFCUTTER_DAGGER_AMBER.get(),ModItems.LEAFCUTTER_DAGGER_AMETHYST.get(),ModItems.LEAFCUTTER_DAGGER_JADE.get()})
        for(int rank=1;rank<=4;rank++)for(boolean compact:new boolean[]{false,true}) {
            var stack=new ItemStack(item);boolean jade=net.bananashelp20.forgermod.augmentation.JadeAugments.isJade(stack);
            var description=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(stack,"tooltips.forgermod.ability.rooting_roots",rank,compact);
            var white=new StringBuilder();var green=new StringBuilder();var gray=new StringBuilder();
            description.visit((style,part)->{int color=style.getColor()==null?-1:style.getColor().getValue();
                if(color==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);
                if(color==net.minecraft.ChatFormatting.GREEN.getColor())green.append(part);
                if(color==net.minecraft.ChatFormatting.GRAY.getColor())gray.append(part);
                return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
            if(compact) {
                test.assertTrue(description.getString().equals("Root and poison enemies within 10 blocks for "+(jade?11:10)+" s."),"Wrong compact root summary");
                test.assertTrue(white.toString().equals(jade?"10 blocks":"10 blocks10 s") && green.toString().equals(jade?"11 s":""),"Compact root values/colors disagree");continue;
            }
            test.assertTrue(description.getString().equals("Activate this ability for roots shooting out of the ground that immobilize and poison all enemies within 10 blocks for "+(jade?11:10)+" seconds."),"Rooting Roots does not show approved current wording");
            test.assertTrue(white.toString().equals(jade?"10 blocks":"10 blocks10 seconds") && green.toString().equals(jade?"11 seconds":""),"Range/duration conditional colors are wrong");
            test.assertTrue(gray.toString().contains("immobilize and poison all enemies within ") && gray.toString().contains(" for "),"Root prose is not gray");
        }
        test.succeed();
    }
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
        test.assertTrue(roots.size()==12 && roots.stream().allMatch(r->height(r)<.01),"Roots do not start buried with twelve bounded models");
        test.runAfterDelay(2,()->{
            test.assertTrue(roots.stream().allMatch(r->height(r)>.2 && r.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getCompound("block_state").getString("Name").equals("minecraft:mangrove_roots")
                && Math.abs(r.getY()-start.y)<.001 && !r.shouldBeSaved() && r.noPhysics && r.isNoGravity()
                && r.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getInt("interpolation_duration")==10),"Growth transform, ground origin or safe display properties are wrong");
        });
        test.runAfterDelay(12,()->{
            float before=height(roots.getFirst());
            mob.setPos(mob.position().add(0,.5,0));
            LushWeapon.onLevelTick(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(()->true,test.getLevel()));
            test.assertTrue(height(roots.getFirst())>before,"Roots do not adapt to vertical target movement");
            mob.setPos(start.add(0,0,2));
            LushWeapon.onLevelTick(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(()->true,test.getLevel()));
            test.assertTrue(Math.abs(height(roots.getFirst())-before)<.001,"Roots do not return to the ground binding after a target settles");
        });
        // Clear existing metadata, then verify even one-second boundaries do not restart growth.
        for(int delay=20;delay<=60;delay++) test.runAfterDelay(delay,()->{
            roots.forEach(r->r.getEntityData().packDirty());
            LushWeapon.onLevelTick(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(()->true,test.getLevel()));
            test.assertTrue(roots.stream().noneMatch(r->r.getEntityData().isDirty()),"Stationary roots restart their animation during the hold");
            mob.setDeltaMovement(1,0,1);mob.setPos(mob.position().add(1,0,1));
            LushWeapon.onEntityTick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(mob));
            test.assertTrue(mob.position().distanceTo(start.add(0,0,2))<.001 && mob.getDeltaMovement().horizontalDistanceSqr()==0,"Bound enemy escapes the visible roots");
        });
        test.runAfterDelay(16,()->{
            var endpoints=new java.util.ArrayList<Vec3>();
            for(var root:roots){
                var tag=root.saveWithoutId(new net.minecraft.nbt.CompoundTag());
                var transform=com.mojang.math.Transformation.EXTENDED_CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,tag.get("transformation")).getOrThrow();
                var top=transform.getMatrix().transformPosition(new org.joml.Vector3f(.5f,1,.5f));
                endpoints.add(new Vec3(top.x,top.y,top.z));
            }
            test.assertTrue(endpoints.stream().anyMatch(v->v.x>.1) && endpoints.stream().anyMatch(v->v.x<-.1)
                && endpoints.stream().anyMatch(v->v.z>.1) && endpoints.stream().anyMatch(v->v.z<-.1),"Roots do not wrap all sides of the target");
        });
        test.runAfterDelay(199,()->test.assertTrue(models(test,start).size()==12 && ((LushWeapon)stack.getItem()).isAbilityActive(player,WeaponAbilitySlot.PRIMARY),"Roots disappear before the hold ends"));
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
            test.assertTrue(roots.size()==12 && roots.stream().allMatch(r->height(r)<.01 && !r.isRemoved()),"Switch does not start retraction");
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
