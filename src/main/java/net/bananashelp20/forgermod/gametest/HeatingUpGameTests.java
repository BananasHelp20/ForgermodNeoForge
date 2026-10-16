package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.HeatingUpEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class HeatingUpGameTests {
    private static void ticks(net.minecraft.server.level.ServerPlayer player,int count) {
        for(int i=0;i<count;i++)HeatingUpEvents.onTick(new PlayerTickEvent.Pre(player));
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsAndRanksPreventFireAndHealSlowlyWithoutExtinguishing(GameTestHelper test) {
        var player=TestPlayers.survival(test);
        try {
            for(var item:java.util.List.of(ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),ModItems.INFERNAL_CLAYMORE_AMBER.get(),ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get())) {
                ItemStack stack=new ItemStack(item);
                for(int rank=1;rank<=5;rank++) {
                    if(rank<=4)stack=Augmentations.setLevel(stack,RecommendedAbilities.HEATING_UP,rank);
                    else stack=Augmentations.merge(stack,Augmentations.setLevel(new ItemStack(ModItems.SUPER_AUGMENT_TEMPLATE.get()),RecommendedAbilities.HEATING_UP,5));
                    player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setHealth(10);player.setRemainingFireTicks(200);
                    for(var source:java.util.List.of(player.damageSources().onFire(),player.damageSources().inFire(),player.damageSources().hotFloor())) {
                        player.invulnerableTime=0;player.hurt(source,4);test.assertTrue(player.getHealth()==10,"Fire damage is not prevented at rank "+rank);
                    }
                    int interval=40-5*(rank-1);ticks(player,interval-1);test.assertTrue(player.getHealth()==10,"Heating Up heals too soon");
                    ticks(player,1);test.assertTrue(player.getHealth()==11 && player.getRemainingFireTicks()==200,"Healing rate is wrong or burning is extinguished");
                    ticks(player,interval);test.assertTrue(player.getHealth()==12,"Healing does not repeat");
                    player.setHealth(19.5f);ticks(player,interval);test.assertTrue(player.getHealth()==20,"Healing exceeds maximum");
                    player.clearFire();ticks(player,1);
                }
            }
        } finally {HeatingUpEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void lavaNeverProtectsOrHealsAndOtherDamageRemains(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var stack=Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),RecommendedAbilities.HEATING_UP);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try {
            player.setHealth(20);player.invulnerableTime=0;player.hurt(player.damageSources().lava(),4);test.assertTrue(player.getHealth()<20,"Heating Up blocks lava damage");
            player.setHealth(20);player.invulnerableTime=0;player.hurt(player.damageSources().generic(),4);test.assertTrue(player.getHealth()<20,"Heating Up blocks unrelated damage");
            test.setBlock(new net.minecraft.core.BlockPos(2,2,2),net.minecraft.world.level.block.Blocks.LAVA);
            player.setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(test.absolutePos(new net.minecraft.core.BlockPos(2,2,2))));player.baseTick();
            test.assertTrue(player.isInLava(),"Lava test fixture is not submerged");
            player.setHealth(10);player.setRemainingFireTicks(200);ticks(player,120);test.assertTrue(player.getHealth()==10,"Heating Up heals while submerged in lava");
            player.invulnerableTime=0;player.hurt(player.damageSources().onFire(),2);test.assertTrue(player.getHealth()<10,"Heating Up still protects against fire while in lava");
        } finally {HeatingUpEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void burningAndEquippedPassiveAreRequiredAndSwitchingResetsRecovery(GameTestHelper test) {
        var player=TestPlayers.survival(test);
        var learned=Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),RecommendedAbilities.HEATING_UP);
        try {
            player.setHealth(10);player.setRemainingFireTicks(200);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.INFERNAL_CLAYMORE.get()));ticks(player,80);test.assertTrue(player.getHealth()==10,"Fresh weapon heals");
            player.invulnerableTime=0;player.hurt(player.damageSources().onFire(),2);test.assertTrue(player.getHealth()<10,"Fresh weapon protects");
            player.setItemInHand(InteractionHand.MAIN_HAND,learned);player.clearFire();player.setHealth(10);ticks(player,80);test.assertTrue(player.getHealth()==10,"Unburned player heals");
            player.setRemainingFireTicks(200);ticks(player,39);player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);ticks(player,1);
            player.setItemInHand(InteractionHand.MAIN_HAND,learned);ticks(player,1);test.assertTrue(player.getHealth()==10,"Switching stores recovery for an instant heal");ticks(player,39);test.assertTrue(player.getHealth()==11,"Reequipped passive does not recover");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,learned);ticks(player,80);test.assertTrue(player.getHealth()==11,"Offhand claymore grants the passive");
        } finally {HeatingUpEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void oldSavedHeatDoesNotIgniteHitsOrAppearInTooltips(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);
        var stack=Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),RecommendedAbilities.HEATING_UP);
        var data=stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();data.putFloat("forgermod_heating_up_heat",20);stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));
        stack=ItemStack.parseOptional(test.getLevel().registryAccess(),(net.minecraft.nbt.CompoundTag)stack.save(test.getLevel().registryAccess()));player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var target=EntityType.COW.create(test.getLevel());test.getLevel().addFreshEntity(target);
        try {
            target.hurt(player.damageSources().playerAttack(player),2);test.assertTrue(target.getRemainingFireTicks()<=0,"Old stored heat still ignites attacks");
            var tooltip=new java.util.ArrayList<net.minecraft.network.chat.Component>();stack.getItem().appendHoverText(stack,Item.TooltipContext.of(test.getLevel()),tooltip,TooltipFlag.NORMAL);
            test.assertFalse(tooltip.stream().anyMatch(c->c.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t && t.getKey().equals("tooltips.forgermod.passive.heating_up.heat")),"Retired heat tooltip remains");
            test.assertTrue(Augmentations.hasPassive(stack,RecommendedAbilities.HEATING_UP),"Old saved augment is lost");
        } finally {target.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
