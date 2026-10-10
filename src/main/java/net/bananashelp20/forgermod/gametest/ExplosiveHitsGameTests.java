package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.ExplosiveHitsEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ExplosiveHitsGameTests {
    @GameTest(template="riftfang_test")
    public static void rankFourBlastStopsAtWallsAndFinishingStartsCooldown(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        ItemStack stack=new ItemStack(ModItems.INFERNAL_CLAYMORE.get());
        for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.EXPLOSIVE_HITS);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var victim=EntityType.COW.create(test.getLevel()); var blocked=EntityType.COW.create(test.getLevel());
        for(var cow:new net.minecraft.world.entity.animal.Cow[]{victim,blocked}) {
            cow.setNoAi(true); cow.setNoGravity(true); cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); cow.setHealth(1000);
        }
        victim.setPos(player.position().add(0,0,2)); blocked.setPos(victim.position().add(3,0,0));
        test.getLevel().addFreshEntity(victim); test.getLevel().addFreshEntity(blocked);
        var wall=victim.blockPosition().east();
        test.getLevel().setBlockAndUpdate(wall,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        test.getLevel().setBlockAndUpdate(wall.above(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        AbilityTestPackets.use(player,0); victim.setInvulnerable(true);
        test.assertFalse(victim.hurt(player.damageSources().playerAttack(player),2),"Invulnerable test hit succeeds");
        victim.setInvulnerable(false);
        float blast=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*.3F;
        for(int hit=0;hit<10;hit++) {
            float before=victim.getHealth(); victim.invulnerableTime=0; victim.hurt(player.damageSources().playerAttack(player),2);
            test.assertTrue(Math.abs(before-victim.getHealth()-(2+blast))<.001,"Primary victim misses full extra explosion damage");
        }
        test.assertTrue(blocked.getHealth()==1000,"Blast penetrates a solid wall");
        test.assertTrue(player.getHealth()==20,"Rank IV blast harms nearby owner");
        test.assertTrue(test.getLevel().getBlockState(wall).is(net.minecraft.world.level.block.Blocks.STONE),"Blast destroys terrain");
        net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
        String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.EXPLOSIVE_HITS;
        test.assertTrue(player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS).get(key)==player.level().getGameTime()+900,
                "Finishing ten charges does not start cooldown");
        AbilityTestPackets.use(player,0); test.assertFalse(ExplosiveHitsEvents.active(player),"Cooldown allows immediate rearm");
        victim.discard(); blocked.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsHaveExactlyTenNonRecursiveThirtyPercentBlasts(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),
                ModItems.INFERNAL_CLAYMORE_AMBER.get(),ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get()}) {
            var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
            var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.EXPLOSIVE_HITS); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var victim=EntityType.COW.create(test.getLevel()); var nearby=EntityType.COW.create(test.getLevel());
            for(var cow:new net.minecraft.world.entity.animal.Cow[]{victim,nearby}) {
                cow.setNoAi(true); cow.setNoGravity(true); cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); cow.setHealth(1000);
            }
            victim.setPos(player.position().add(0,0,2)); nearby.setPos(victim.position().add(1,0,0));
            test.getLevel().addFreshEntity(victim); test.getLevel().addFreshEntity(nearby);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.EXPLOSIVE_HITS),"Explosive Hits does not arm");
            test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.EXPLOSIVE_HITS),"Armed charge can be restarted");
            float blast=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*.3F;
            for(int hit=0;hit<10;hit++) {
                victim.invulnerableTime=0; float before=nearby.getHealth();
                victim.hurt(player.damageSources().playerAttack(player),2);
                test.assertTrue(Math.abs(before-nearby.getHealth()-blast)<.001,"Blast damage differs from 30% weapon damage");
                test.assertTrue(ExplosiveHitsEvents.active(player)==(hit<9),"Explosion recursively consumes charges or wrong hit count");
                test.assertTrue(player.getHealth()==20,"Blast damages its wielder");
            }
            float before=nearby.getHealth(); victim.invulnerableTime=0; victim.hurt(player.damageSources().playerAttack(player),2);
            test.assertTrue(nearby.getHealth()==before,"Eleventh hit still explodes");
            RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.EXPLOSIVE_HITS);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());
            test.assertFalse(ExplosiveHitsEvents.active(player),"Switching retains charges");
            victim.discard(); nearby.discard(); test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
}
