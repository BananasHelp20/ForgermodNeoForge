package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PoisonedVeinGameTests {
    @GameTest(template="riftfang_test",timeoutTicks=80)
    public static void jadeCloudPreservesCasterPoisonButAnotherCastersCloudStillApplies(GameTestHelper test) {
        var position=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,8,8)));
        var owner=player(test,position);owner.setItemInHand(InteractionHand.MAIN_HAND,TestWeapons.ready(ModItems.LEAFCUTTER_DAGGER_JADE.get()));
        owner.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON,120,0));
        var center=cloud(test,owner);var other=player(test,center.add(1,0,0));
        test.runAfterDelay(12,()->{
            var original=owner.getEffect(MobEffects.POISON);
            // Mock players may not tick effects; either way the original poison must not be replaced/refreshed.
            test.assertTrue(original!=null && original.getAmplifier()==0 && original.getDuration()>=105 && original.getDuration()<=120,"Own cloud clears or refreshes unrelated caster poison: "+original);
            var jade=other.getEffect(MobEffects.POISON);
            test.assertTrue(jade!=null && jade.getAmplifier()==1 && jade.getDuration()>=610 && jade.getDuration()<=620,"Jade cloud no longer gives other occupants Poison II for 31 seconds");
            cloud(test,other);
        });
        test.runAfterDelay(24,()->{try{
            poison(test,owner);test.succeed();
        }finally{cleanup(test,owner,other);}});
    }

    @GameTest(template="empty")
    public static void reviewedPoisonDescriptionColorsOnlyApprovedConditionalPhrases(GameTestHelper test) {
        for(var item:new net.minecraft.world.item.Item[]{ModItems.LEAFCUTTER_DAGGER.get(),ModItems.LEAFCUTTER_DAGGER_RUBY.get(),ModItems.LEAFCUTTER_DAGGER_AMBER.get(),ModItems.LEAFCUTTER_DAGGER_AMETHYST.get(),ModItems.LEAFCUTTER_DAGGER_JADE.get()})
        for(int rank=1;rank<=4;rank++)for(boolean compact:new boolean[]{false,true}) {
            var stack=new ItemStack(item);boolean jade=net.bananashelp20.forgermod.augmentation.JadeAugments.isJade(stack);
            var text=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(stack,"tooltips.forgermod.ability.poisoned_vein",rank,compact);
            var white=new StringBuilder();var green=new StringBuilder();var gray=new StringBuilder();
            text.visit((style,part)->{
                int color=style.getColor()==null?-1:style.getColor().getValue();
                if(color==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);
                if(color==net.minecraft.ChatFormatting.GREEN.getColor())green.append(part);
                if(color==net.minecraft.ChatFormatting.GRAY.getColor())gray.append(part);
                return java.util.Optional.<Void>empty();
            },net.minecraft.network.chat.Style.EMPTY);
            if(compact) {
                test.assertTrue(text.getString().equals("Next dagger kill: 5-second cloud within 3 blocks. Poisons others for "+(jade?31:30)+" s."),"Wrong compact poison cloud summary");
                test.assertTrue(white.toString().equals(jade?"":"30 s") && green.toString().equals(jade?"31 s":""),"Compact poison duration/color wrong");continue;
            }
            test.assertTrue(text.getString().equals("Your next dagger kill leaves a 5-second cloud. Everyone besides you within 3 blocks receives Poison "+(jade?"II for 31":"I for 30")+" seconds."),"Poisoned Vein does not use approved current wording");
            test.assertTrue(white.toString().equals(jade?"":"30 seconds") && green.toString().equals(jade?"Poison II31 seconds":""),"Poison effect/duration conditional colors are wrong");
            test.assertTrue(gray.toString().contains("5-second cloud") && gray.toString().contains("3 blocks") && gray.toString().contains(" for "),"Unannotated prose or for lost its gray style");
        }
        test.succeed();
    }

    private static ServerPlayer player(GameTestHelper test, Vec3 position) {
        ServerPlayer player = test.makeMockServerPlayerInLevel();
        player.setNoGravity(true);
        player.setPos(position);
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(ModItems.LEAFCUTTER_DAGGER.get()));
        return player;
    }

    private static <T extends Mob> T mob(GameTestHelper test, EntityType<T> type, Vec3 position) {
        T mob = type.create(test.getLevel());
        mob.setNoAi(true);
        mob.setNoGravity(true);
        mob.setPos(position);
        test.getLevel().addFreshEntity(mob);
        return mob;
    }

    private static LushWeapon weapon(ServerPlayer player) {
        return (LushWeapon)player.getMainHandItem().getItem();
    }

    private static Vec3 cloud(GameTestHelper test, ServerPlayer owner) {
        var victim = mob(test, EntityType.ZOMBIE, owner.position().add(0, -.975, 0));
        test.assertTrue(weapon(owner).activateAbility(owner, owner.getMainHandItem(), WeaponAbilitySlot.SECONDARY), "Failed to arm Poisoned Vein");
        LushWeapon.onHostileDeath(new LivingDeathEvent(victim, owner.damageSources().playerAttack(owner)));
        Vec3 center = victim.position().add(0, victim.getBbHeight() * .5, 0);
        victim.discard();
        return center;
    }

    private static void poison(GameTestHelper test, LivingEntity target) {
        var effect = target.getEffect(MobEffects.POISON);
        test.assertTrue(effect != null, "Cloud failed to poison " + target.getType());
        test.assertTrue(effect.getAmplifier() == 0 && effect.getDuration() >= 590 && effect.getDuration() <= 600,
                "Cloud did not apply/refresh thirty seconds of Poison I: " + effect);
    }

    private static void cleanup(GameTestHelper test, ServerPlayer... players) {
        for (ServerPlayer player : players) {
            weapon(player).cancelAbility(player, WeaponAbilitySlot.SECONDARY);
            test.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    @GameTest(template = "riftfang_test", timeoutTicks = 180)
    public static void everybodyExceptOwnerGetsThirtySeconds(GameTestHelper test) {
        Vec3 position = Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8, 8, 8)));
        ServerPlayer owner = player(test, position);
        Vec3 center = cloud(test, owner);
        ServerPlayer bystander = player(test, center.add(1, 0, 0));
        var animal = mob(test, EntityType.COW, center.add(-1, 0, 0));
        var hostile = mob(test, EntityType.CREEPER, center.add(0, 0, 2));
        var outside = mob(test, EntityType.COW, center.add(3.1, 0, 0));
        test.runAfterDelay(12, () -> {
            test.assertFalse(owner.hasEffect(MobEffects.POISON), "Caster is poisoned by its own cloud");
            poison(test, bystander);
            poison(test, animal);
            poison(test, hostile);
            test.assertFalse(outside.hasEffect(MobEffects.POISON), "Cloud poisoned an entity outside its radius");
        });
        test.runAfterDelay(120, () -> {
            test.assertFalse(weapon(owner).isAbilityActive(owner, WeaponAbilitySlot.SECONDARY), "Cloud lifespan changed from five seconds");
            test.assertTrue(animal.hasEffect(MobEffects.POISON) && animal.getEffect(MobEffects.POISON).getDuration() > 450,
                    "Poison disappeared with the cloud or had the old two-second duration");
            cleanup(test, owner, bystander);
            test.succeed();
        });
    }

    @GameTest(template = "riftfang_test", timeoutTicks = 100)
    public static void refreshesInsideAndPersistsAfterLeaving(GameTestHelper test) {
        Vec3 position = Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8, 8, 8)));
        ServerPlayer owner = player(test, position);
        Vec3 center = cloud(test, owner);
        var target = mob(test, EntityType.COW, center.add(4, 0, 0));
        test.runAfterDelay(12, () -> {
            test.assertFalse(target.hasEffect(MobEffects.POISON), "Outside target was poisoned");
            target.setPos(center);
        });
        test.runAfterDelay(24, () -> poison(test, target));
        test.runAfterDelay(50, () -> {
            poison(test, target);
            target.setPos(center.add(5, 0, 0));
            int remaining = target.getEffect(MobEffects.POISON).getDuration();
            weapon(owner).cancelAbility(owner, WeaponAbilitySlot.SECONDARY);
            test.runAfterDelay(20, () -> {
                test.assertTrue(target.hasEffect(MobEffects.POISON)
                                && Math.abs(target.getEffect(MobEffects.POISON).getDuration() - (remaining - 20)) <= 1,
                        "Leaving/canceling removed poison or continued refreshing it outside the cloud");
                cleanup(test, owner);
                test.succeed();
            });
        });
    }
}
