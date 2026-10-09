package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.TaifuniteWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class EyeOfStormGameTests {
    @GameTest(template = "riftfang_test")
    public static void stormAffectsEveryEntityTypeExceptCaster(GameTestHelper test) {
        var player = test.makeMockServerPlayerInLevel();
        player.setNoGravity(true);
        Vec3 center = test.getBounds().getCenter();
        player.setPos(center);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DEAD_CALM_DAGGER.get()));
        var other = test.makeMockServerPlayerInLevel();
        other.setNoGravity(true);
        other.setPos(center.add(2, 0, 0));
        var cow = EntityType.COW.create(test.getLevel());
        cow.setPos(center.add(-2, 0, 0));
        var zombie = EntityType.ZOMBIE.create(test.getLevel());
        zombie.setPos(center.add(0, 0, 2));
        var arrow = EntityType.ARROW.create(test.getLevel());
        arrow.setPos(center.add(0, 0, -2));
        var item = new ItemEntity(test.getLevel(), center.x + 1, center.y, center.z + 1, new ItemStack(Items.DIRT));
        var orb = new ExperienceOrb(test.getLevel(), center.x - 1, center.y, center.z - 1, 1);
        var outside = EntityType.COW.create(test.getLevel());
        outside.setPos(center.add(10.1, 0, 0));
        List<Entity> targets = List.of(other, cow, zombie, arrow, item, orb);
        for (Entity target : List.of(cow, zombie, arrow, item, orb, outside)) test.getLevel().addFreshEntity(target);
        for (Entity target : targets) { target.setDeltaMovement(Vec3.ZERO); target.hurtMarked = false; }
        player.setDeltaMovement(Vec3.ZERO);
        outside.setDeltaMovement(Vec3.ZERO);
        TaifuniteWeapon weapon = (TaifuniteWeapon)player.getMainHandItem().getItem();
        test.assertTrue(weapon.activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Storm did not activate");
        TaifuniteWeapon.onPlayerTick(new PlayerTickEvent.Post(player));
        for (Entity target : targets) {
            test.assertTrue(target.getDeltaMovement().y == .16 && target.getDeltaMovement().horizontalDistanceSqr() > 0,
                    "Storm ignored an entity type: " + target.getType());
            test.assertTrue(target.hurtMarked && target.hasImpulse, "Storm failed to synchronize target motion");
        }
        test.assertTrue(player.getDeltaMovement().equals(Vec3.ZERO), "Storm moved its caster");
        test.assertTrue(outside.getDeltaMovement().equals(Vec3.ZERO), "Storm exceeded ten blocks");
        test.assertTrue(weapon.abilityCooldownTicks(WeaponAbilitySlot.PRIMARY) == 1200, "Storm cooldown changed");
        // Embedded mock connections do not drive normal ServerPlayer ticks.
        for (int tick = 1; tick <= 12; tick++) {
            test.runAfterDelay(tick, () -> TaifuniteWeapon.onPlayerTick(new PlayerTickEvent.Post(player)));
        }
        test.runAfterDelay(13, () -> {
            test.assertTrue(other.hasEffect(MobEffects.LEVITATION) && cow.hasEffect(MobEffects.LEVITATION)
                    && zombie.hasEffect(MobEffects.LEVITATION), "Storm did not levitate all living target types");
            test.assertFalse(player.hasEffect(MobEffects.LEVITATION), "Storm levitated its caster");
            weapon.cancelAbility(player, WeaponAbilitySlot.PRIMARY);
            test.getLevel().getServer().getPlayerList().remove(other);
            test.getLevel().getServer().getPlayerList().remove(player);
            test.succeed();
        });
    }
}
