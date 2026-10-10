package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SwingAttackGameTests {
    private static ServerPlayer player(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        for(int x=-5;x<=5;x++) for(int y=0;y<=3;y++) for(int z=-5;z<=5;z++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        return player;
    }
    private static Zombie enemy(GameTestHelper test,ServerPlayer player,double x,double z) {
        var enemy=EntityType.ZOMBIE.create(test.getLevel()); enemy.setNoAi(true); enemy.setNoGravity(true);
        enemy.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));
        enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); enemy.setHealth(1000); enemy.getAttribute(Attributes.ARMOR).setBaseValue(0);
        enemy.setPos(player.position().add(x,0,z)); test.getLevel().addFreshEntity(enemy); return enemy;
    }
    @GameTest(template="riftfang_test")
    public static void everySpecialAxeVariantHitsFrontAndRearForCurrentDamage(GameTestHelper test) {
        var player=player(test); int count=0;
        for(var item:BuiltInRegistries.ITEM) {
            if(!(item instanceof SwordItemWithEffect weapon) || !weapon.hasMaterialEffect() || !weapon.isAxe()) continue;
            count++;
            var fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
            test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,RecommendedAbilities.SWING_ATTACK),"Fresh axe has Swing Attack");
            var stack=Augmentations.apply(fresh,RecommendedAbilities.SWING_ATTACK);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack); player.doTick();
            var front=enemy(test,player,0,2); var rear=enemy(test,player,0,-2);
            float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.SWING_ATTACK),"Axe cannot swing");
            test.assertTrue(Math.abs(front.getHealth()-(1000-damage))<.001 && Math.abs(rear.getHealth()-(1000-damage))<.001,"Swing misses front/rear or uses wrong weapon damage");
            test.assertTrue(stack.getDamageValue()==2,"Successful targets do not apply normal weapon wear");
            front.discard(); rear.discard();
        }
        test.assertTrue(count==45,"Swing Attack does not cover all 45 special axes");
        test.assertFalse(Augmentations.pool(new ItemStack(ModItems.CARBON_STEEL_AXE.get())).stream().anyMatch(a->a.id().equals(RecommendedAbilities.SWING_ATTACK)),"Carbon steel receives special ability");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void wallsRangeNeutralTargetsAndInstantCooldownAreRespected(GameTestHelper test) throws Exception {
        var player=player(test); ItemStack stack=new ItemStack(ModItems.MOLTEN_AXE.get());
        for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.SWING_ATTACK);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); player.doTick();
        var near=enemy(test,player,-2,0); var blocked=enemy(test,player,0,2.5); var far=enemy(test,player,4,0);
        var ally=enemy(test,player,0,-2);
        var team=player.getScoreboard().addPlayerTeam("swing_"+player.getUUID().toString().substring(0,8));
        player.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);
        player.getScoreboard().addPlayerToTeam(ally.getScoreboardName(),team);
        var cow=EntityType.COW.create(test.getLevel()); cow.setNoAi(true); cow.setNoGravity(true); cow.setPos(player.position().add(0,0,-2)); test.getLevel().addFreshEntity(cow);
        for(int x=-1;x<=0;x++) for(int y=0;y<=2;y++) test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,1),Blocks.STONE.defaultBlockState());
        AbilityTestPackets.use(player,0);
        test.assertTrue(near.getHealth()<1000 && blocked.getHealth()==1000 && far.getHealth()==1000 && cow.getHealth()==10,"Swing ignores wall/range/enemy filtering");
        test.assertTrue(player.getHealth()==20,"Swing hits its owner");
        test.assertTrue(ally.getHealth()==1000,"Swing hits a team ally");
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.SWING_ATTACK;
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+300,"Rank IV instant cooldown wrong");
        float health=near.getHealth(); AbilityTestPackets.use(player,0); test.assertTrue(near.getHealth()==health,"Cooldown allows repeated swing");
        player.getScoreboard().removePlayerTeam(team);
        near.discard(); blocked.discard(); far.discard(); cow.discard(); ally.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
