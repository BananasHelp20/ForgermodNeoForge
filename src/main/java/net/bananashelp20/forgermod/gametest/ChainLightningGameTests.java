package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class ChainLightningGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveDaggersChainThroughPassivePetsFlyingMobsAndAlliedPlayers(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);var other=TestPlayers.survival(test);
        var origin=test.getBounds().getCenter().add(0,20,0);player.setPos(origin);player.setNoGravity(true);
        var original=EntityType.ZOMBIE.create(test.getLevel());original.setPos(origin.add(0,0,2));original.setNoAi(true);test.getLevel().addFreshEntity(original);
        var cow=EntityType.COW.create(test.getLevel());var wolf=EntityType.WOLF.create(test.getLevel());wolf.tame(player);var bat=EntityType.BAT.create(test.getLevel());
        var targets=java.util.List.<LivingEntity>of(cow,wolf,bat,other);
        for(int i=0;i<targets.size();i++) {
            var target=targets.get(i);target.setPos(origin.add(0,0,2+9*(i+1)));target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            if(target instanceof Mob mob){mob.setNoAi(true);test.getLevel().addFreshEntity(mob);}
        }
        var team=player.getScoreboard().addPlayerTeam("chain_"+player.getUUID().toString().substring(0,8));
        for(var member:java.util.List.of(player,cow,other))player.getScoreboard().addPlayerToTeam(member.getScoreboardName(),team);
        boolean pvp=player.server.isPvpAllowed();player.server.setPvpAllowed(true);
        var method=ElectriumWeapon.class.getDeclaredMethod("chainLightning",LivingEntity.class,net.minecraft.server.level.ServerPlayer.class);method.setAccessible(true);
        long seed=0;
        for(;;seed++) {var rng=net.minecraft.util.RandomSource.create(seed);boolean matches=true;for(int i=0;i<4;i++)matches&=rng.nextBoolean();if(matches && !rng.nextBoolean())break;}
        int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                if(!(entry.get() instanceof ElectriumWeapon weapon) || !weapon.isDagger())continue;
                count++;player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(entry.get()));player.doTick();
                for(var target:targets){target.setHealth(200);target.invulnerableTime=0;}
                float before=player.getHealth();float expected=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*.5F;
                player.getRandom().setSeed(seed);method.invoke(weapon,original,player);
                for(var target:targets)test.assertTrue(Math.abs(target.getHealth()-(200-expected))<.001,"Chain misses or repeats allied/passive target: "+target.getType());
                test.assertTrue(player.getHealth()==before && original.getHealth()==original.getMaxHealth(),"Chain damages caster or revisits initial target");
            }
            test.assertTrue(count==5,"Skipped Electrium dagger variants");test.succeed();
        } finally {
            original.discard();cow.discard();wolf.discard();bat.discard();player.getScoreboard().removePlayerTeam(team);player.server.setPvpAllowed(pvp);
            player.server.getPlayerList().remove(other);player.server.getPlayerList().remove(player);
        }
    }
}
