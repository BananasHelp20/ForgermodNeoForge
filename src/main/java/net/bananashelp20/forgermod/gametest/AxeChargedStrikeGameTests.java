package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeHeavyNetwork;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Map;
import java.util.UUID;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AxeChargedStrikeGameTests {
    private static boolean swing(ServerPlayer player) throws Exception {
        var method=AxeHeavyNetwork.class.getDeclaredMethod("queueAttack",ServerPlayer.class);method.setAccessible(true);
        return (Boolean)method.invoke(null,player);
    }
    private static void airSwing(ServerPlayer player) throws Exception {
        var contextType=net.neoforged.neoforge.network.handling.IPayloadContext.class;
        var context=(net.neoforged.neoforge.network.handling.IPayloadContext)java.lang.reflect.Proxy.newProxyInstance(contextType.getClassLoader(),new Class<?>[]{contextType},
                (proxy,method,args)->{if(method.getName().equals("player"))return player;throw new UnsupportedOperationException(method.getName());});
        var method=AxeHeavyNetwork.class.getDeclaredMethod("handleAttack",AxeHeavyNetwork.AxeAttackPayload.class,contextType);method.setAccessible(true);
        method.invoke(null,new AxeHeavyNetwork.AxeAttackPayload(1),context);
    }
    @SuppressWarnings("unchecked")
    private static Map<UUID,Object> state(String name) throws Exception {
        var field=AxeHeavyNetwork.class.getDeclaredField(name);field.setAccessible(true);return (Map<UUID,Object>)field.get(null);
    }
    private static int count(ServerPlayer player) throws Exception {
        var combo=state("COMBOS").get(player.getUUID());if(combo==null)return -1;
        var method=combo.getClass().getDeclaredMethod("count");method.setAccessible(true);return (Integer)method.invoke(combo);
    }
    private static void ageAcceptedSwing(ServerPlayer player,int count) throws Exception {
        var type=Class.forName(AxeHeavyNetwork.class.getName()+"$Combo");
        var constructor=type.getDeclaredConstructor(Item.class,long.class,long.class,int.class);constructor.setAccessible(true);
        long now=player.level().getGameTime(),delay=(long)Math.ceil(player.getCurrentItemAttackStrengthDelay());
        state("COMBOS").put(player.getUUID(),constructor.newInstance(player.getMainHandItem().getItem(),now-delay*2,now-delay,count));
    }
    private static void recharge(ServerPlayer player) {for(int i=0;i<100;i++)player.doTick();}
    private static void clear(ServerPlayer player) {AxeHeavyNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));}

    @GameTest(template="riftfang_test")
    public static void allAxesRequireFullChargeForEveryComboStep(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        player.setPos(origin);player.setNoGravity(true);player.setYRot(0);player.setXRot(0);
        test.setBlock(new BlockPos(8,4,10),Blocks.STONE);int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var stack=new ItemStack(entry.get());if(!AxeItems.isAxe(stack))continue;checked++;clear(player);
                if(Augmentations.eligible(stack))stack=Augmentations.apply(stack,Augmentations.VERTICAL_STRIKE);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);recharge(player);player.resetAttackStrengthTicker();
                test.assertFalse(swing(player),"Uncharged swing schedules slam");test.assertTrue(count(player)==-1,"Uncharged swing starts combo");
                int delay=(int)Math.ceil(player.getCurrentItemAttackStrengthDelay());
                for(int tick=0;tick<delay-1;tick++)player.doTick();
                test.assertTrue(player.getAttackStrengthScale(0)<1,"Partial-charge fixture is full");
                swing(player);test.assertTrue(count(player)==-1,"Nearly charged swing counts");
                player.doTick();test.assertFalse(swing(player),"First charged swing slams");test.assertTrue(count(player)==1,"Full swing does not count");
                test.assertTrue(player.getAttackStrengthScale(0)==1,"Counting weakens following vanilla melee damage");
                swing(player);test.assertTrue(count(player)==1,"Duplicate/air packet bypasses recharge");
                airSwing(player);test.assertTrue(player.getAttackStrengthScale(0)==0 && count(player)==1,"Air miss does not synchronize recharge");
                player.doTick();airSwing(player);test.assertTrue(player.getAttackStrengthScale(0)==0 && count(player)==1,"Weak air miss charges smash or fails to reset charge");
                ageAcceptedSwing(player,1);player.resetAttackStrengthTicker();swing(player);test.assertTrue(count(player)==1,"Weak second swing advances or clears progress");
                recharge(player);test.assertFalse(swing(player),"Second charged swing slams");test.assertTrue(count(player)==2,"Second full swing does not count");
                ageAcceptedSwing(player,2);player.resetAttackStrengthTicker();swing(player);test.assertTrue(count(player)==2 && !state("PENDING").containsKey(player.getUUID()),"Weak third swing slams");
                recharge(player);test.assertTrue(swing(player) && state("PENDING").containsKey(player.getUUID()),"Third charged swing does not schedule slam");
                test.assertTrue(count(player)==0,"Third swing retains charged progress");swing(player);test.assertTrue(count(player)==0,"Duplicate third packet charges next combo");
            }
        } finally {clear(player);player.server.getPlayerList().remove(player);}
        test.assertTrue(checked==45,"Skipped axe variants: "+checked);test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void countedPacketPreservesFullVanillaAttackDamage(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        player.setPos(origin);player.setNoGravity(true);player.setOnGround(true);player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),Augmentations.VERTICAL_STRIKE));recharge(player);
        var target=EntityType.ZOMBIE.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(origin.add(0,0,2));
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.getAttribute(Attributes.ARMOR).setBaseValue(0);target.setHealth(1000);test.getLevel().addFreshEntity(target);
        try {
            swing(player);float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);player.attack(target);
            test.assertTrue(Math.abs(target.getHealth()-(1000-damage))<.001,"Accepted charge packet weakens real melee hit: health="+target.getHealth()+" damage="+damage);
            test.assertTrue(player.getAttackStrengthScale(0)==0,"Vanilla melee did not reset charge");swing(player);test.assertTrue(count(player)==1,"Immediate weak melee advances combo");
        } finally {clear(player);target.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
