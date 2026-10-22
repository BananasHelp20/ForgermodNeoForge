package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.MagmaTossEvents;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MagmaTossGameTests {
    @GameTest(template="riftfang_test", timeoutTicks=100)
    public static void realProjectileHitsWallAndLeavesTerrainAndHiddenEnemyUntouched(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);player.setYRot(0);player.setXRot(0);
        for(int x=6;x<=10;x++) for(int y=5;y<=8;y++) for(int z=7;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        for(int y=5;y<=7;y++) test.setBlock(new BlockPos(8,y,10),Blocks.STONE);
        var hidden=EntityType.ZOMBIE.create(test.getLevel());hidden.setNoAi(true);hidden.setNoGravity(true);
        hidden.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN));
        hidden.setPos(origin.add(0,0,4));test.getLevel().addFreshEntity(hidden);
        var stack=Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),AdditionalAbilities.MAGMA_TOSS);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.MAGMA_TOSS),"Cannot launch real rock");
        test.runAfterDelay(10,()->{
            try {
                test.assertFalse(MagmaTossEvents.active(player),"Real projectile ignores stone wall");
                test.assertTrue(hidden.getHealth()==hidden.getMaxHealth() && hidden.getRemainingFireTicks()<=0,"Blast damages hidden enemy");
                test.assertTrue(test.getBlockState(new BlockPos(8,6,10)).is(Blocks.STONE),"Magma blast changes terrain");
            } finally {MagmaTossEvents.cancel(player.getUUID());hidden.discard();player.server.getPlayerList().remove(player);}
            test.succeed();
        });
    }

    @GameTest(template="riftfang_test")
    public static void impactDamagesAndIgnitesAllVariantsWithoutTerrainOrCasterDamage(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);player.setYRot(0);player.setXRot(0);
        for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) for(int z=6;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var target=EntityType.ZOMBIE.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(point.add(0,0,3));
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);test.getLevel().addFreshEntity(target);
        int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                if(!(entry.get() instanceof IgnisiumWeapon weapon) || !weapon.isAxe()) continue;
                count++;player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.MAGMA_TOSS),"Fresh axe throws magma");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.MAGMA_TOSS);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.MAGMA_TOSS),"Magma Toss cannot activate");
                var rock=player.level().getEntitiesOfClass(Snowball.class,player.getBoundingBox().inflate(3),e->e.getOwner()==player).getFirst();
                test.assertTrue(rock.getDeltaMovement().length()>1,"Rock is not a moving projectile");
                test.assertTrue(rock.getItem().is(net.minecraft.world.item.Items.MAGMA_BLOCK),"Molten rock does not render as a magma block");
                target.invulnerableTime=0;target.setRemainingFireTicks(0);float before=target.getHealth(),owner=player.getHealth();
                var event=new ProjectileImpactEvent(rock,new EntityHitResult(target,target.getBoundingBox().getCenter()));MagmaTossEvents.impact(event);
                test.assertTrue(event.isCanceled() && !rock.isAlive() && !MagmaTossEvents.active(player),"Impact does not end rock");
                test.assertTrue(Math.abs(before-target.getHealth()-player.getAttributeValue(Attributes.ATTACK_DAMAGE))<.001 && target.getRemainingFireTicks()==60,"Impact damage/fire wrong");
                test.assertTrue(player.getHealth()==owner,"Magma blast damages caster");
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.MAGMA_TOSS),"Cannot start next direct-runtime test");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(MagmaTossEvents.active(player),"Switch does not cancel rock");
            }
            test.assertTrue(count==5,"Skipped magma variants");
        } finally {MagmaTossEvents.cancel(player.getUUID());target.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
