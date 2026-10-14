package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.event.tick.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class HeatingUpFireAspectGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsNeedHeatAndFireAspectForInfiniteBurn(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var target=EntityType.ZOMBIE.create(test.getLevel());target.setNoGravity(true);target.setNoAi(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));target.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(target);
        var fireAspect=player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FIRE_ASPECT);
        try {
            for(var item:java.util.List.of(ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),ModItems.INFERNAL_CLAYMORE_AMBER.get(),ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get())) {
                for(int mode=0;mode<4;mode++) {
                    ItemStack stack=new ItemStack(item);if(mode!=0)stack=Augmentations.apply(stack,RecommendedAbilities.HEATING_UP);if(mode!=1)stack.enchant(fireAspect,1);
                    target.clearFire();target.invulnerableTime=0;player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                    if(mode==1 || mode==3){player.setRemainingFireTicks(100);HeatingUpEvents.onTick(new PlayerTickEvent.Pre(player));}
                    for(int tick=0;tick<60;tick++)player.doTick();
                    target.setPos(player.position().add(0,0,2));player.attack(target);
                    test.assertTrue(InfiniteBurnEvents.active(target)==(mode==3),"Wrong infinite burn prerequisites: "+mode);
                    if(mode==3)test.assertTrue(HeatingUpEvents.storedHeat(stack)==0 && target.getRemainingFireTicks()>=30000,"Permanent attack fails to pay heat or ignite");
                    else test.assertTrue(target.getRemainingFireTicks()>0 && target.getRemainingFireTicks()<30000,"Ordinary ignition became infinite");
                }
            }
            // Explicit extinguishing must end the marker even if a finite fire starts in the same tick.
            target.clearFire();target.setRemainingFireTicks(60);InfiniteBurnEvents.tick(new EntityTickEvent.Post(target));test.assertFalse(InfiniteBurnEvents.active(target),"Extinguished burn resurrects after finite reignition");
        } finally {target.discard();HeatingUpEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void infiniteBurnSavesKeepsVanillaDamageCadenceAndExtinguishes(GameTestHelper test) {
        var target=EntityType.COW.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,6,8))));target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);
        for(int x=7;x<=9;x++)for(int y=5;y<=9;y++)for(int z=7;z<=9;z++)test.setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);
        InfiniteBurnEvents.ignite(target);
        for(int tick=0;tick<100;tick++){target.baseTick();InfiniteBurnEvents.tick(new EntityTickEvent.Post(target));}
        test.assertTrue(target.getHealth()==995 && target.getRemainingFireTicks()>=30000,"Infinite burning loses periodic damage cadence");
        var restored=EntityType.COW.create(test.getLevel());restored.load(target.saveWithoutId(new net.minecraft.nbt.CompoundTag()));
        test.assertTrue(InfiniteBurnEvents.active(restored) && restored.getRemainingFireTicks()>=30000,"Save/load loses permanent burn");
        restored.setRemainingFireTicks(40);InfiniteBurnEvents.tick(new EntityTickEvent.Post(restored));test.assertTrue(restored.getRemainingFireTicks()>=30000,"Later finite burn shortens infinite effect");
        restored.clearFire();InfiniteBurnEvents.tick(new EntityTickEvent.Post(restored));test.assertTrue(!InfiniteBurnEvents.active(restored) && restored.getRemainingFireTicks()<=0,"Clear fire does not end permanent burn");
        InfiniteBurnEvents.ignite(restored);restored.setRemainingFireTicks(0);test.assertFalse(InfiniteBurnEvents.active(restored),"Direct extinguishing does not clear marker");
        InfiniteBurnEvents.ignite(target);test.setBlock(new BlockPos(8,6,8),net.minecraft.world.level.block.Blocks.WATER);target.baseTick();InfiniteBurnEvents.tick(new EntityTickEvent.Post(target));
        test.assertTrue(!InfiniteBurnEvents.active(target) && target.getRemainingFireTicks()<=0,"Water does not extinguish infinite burn");
        var immune=EntityType.BLAZE.create(test.getLevel());InfiniteBurnEvents.ignite(immune);test.assertFalse(InfiniteBurnEvents.active(immune),"Fire-immune entity gets permanent burn");
        target.discard();restored.discard();immune.discard();test.succeed();
    }
}
