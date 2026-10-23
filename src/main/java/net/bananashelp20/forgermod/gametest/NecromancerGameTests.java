package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.bananashelp20.forgermod.screen.custom.NecromancerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class NecromancerGameTests {
    private static ServerPlayer player(GameTestHelper test){
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(10,5,10))));
        for(int x=2;x<=22;x++)for(int z=2;z<=22;z++){test.setBlock(new BlockPos(x,4,z),Blocks.STONE);for(int y=5;y<=12;y++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);}
        return player;
    }
    private static ItemStack weapon(ServerPlayer player,int rank){var stack=Augmentations.setLevel(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);return stack;}
    private static List<Mob> guards(ServerPlayer player){return player.level().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(12),mob->mob instanceof BoneGuardEvents.FriendlyGuard guard && guard.owner()==player && mob.isAlive());}
    private static void clean(ServerPlayer player){BoneGuardEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
    private static void roster(NecromancerLoadouts.Loadout loadout,int... types){loadout.types.clear();for(int type:types)loadout.types.add(type);loadout.gear.setChanged();}
    @GameTest(template="riftfang_test",batch="review_necromancer_catalog")
    public static void everyAllowedUndeadSpawnsAndHorseIncludesRider(GameTestHelper test){
        var player=player(test);var stack=weapon(player,7);var bank=NecromancerLoadouts.get(player,stack);
        try{
            test.assertTrue(NecromancerCatalog.CHOICES.size()==12 && NecromancerCatalog.CHOICES.stream().noneMatch(choice->choice.type()==EntityType.WITHER || choice.type()==EntityType.ZOMBIE_HORSE),"Boss or zombie horse in catalog");
            for(var type:BuiltInRegistries.ENTITY_TYPE)if(type.is(net.minecraft.tags.EntityTypeTags.UNDEAD) && type!=EntityType.WITHER && type!=EntityType.ZOMBIE_HORSE)test.assertTrue(NecromancerCatalog.CHOICES.stream().anyMatch(choice->choice.type()==type),"Missing non-boss undead: "+type);
            for(int choice=0;choice<NecromancerCatalog.CHOICES.size();choice++){
                roster(bank,choice);bank.publish(stack);test.assertTrue(BoneGuardEvents.activate(player,stack),"Spawn rejected: "+choice);var group=guards(player);
                test.assertTrue(group.size()==(choice==10?2:1),"Wrong physical mob count: "+choice);
                var type=NecromancerCatalog.CHOICES.get(choice).type();test.assertTrue(group.stream().anyMatch(mob->mob.getType()==type),"Wrong mob type: "+choice);
                if(choice==10){var horse=group.stream().filter(mob->mob.getType()==EntityType.SKELETON_HORSE).findFirst().orElseThrow();test.assertTrue(horse.getFirstPassenger()!=null && horse.getFirstPassenger().getType()==EntityType.SKELETON && bank.used()==3,"Horse lacks its included rider or costs more than three");}
                for(var mob:group){test.assertFalse(mob.canAttack(player) || mob.shouldBeSaved() || mob.shouldDropExperience() || mob.canUsePortal(true),"Unsafe summon lifecycle/friendliness: "+choice);
                    var interaction=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract(player,InteractionHand.MAIN_HAND,mob);NecromancerEquipment.interact(interaction);test.assertTrue(interaction.isCanceled(),"Summon interaction permits equipment extraction");}
                BoneGuardEvents.cancel(player.getUUID());
            }
        }finally{clean(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_necromancer_editor")
    public static void editorValidatesCapacityRanksSlotsAndReturnsActualItems(GameTestHelper test){
        var player=player(test);
        try{
            for(int rank=4;rank<=7;rank++){
                var stack=weapon(player,rank);var bank=NecromancerLoadouts.get(player,stack);var menu=new NecromancerMenu(1,player.getInventory(),player.getInventory().selected);
                test.assertTrue(menu.capacity()==4+2*(rank-4) && menu.used()==menu.capacity(),"Wrong rank capacity");test.assertFalse(menu.clickMenuButton(player,1),"Over-capacity addition accepted");
                roster(bank,0);test.assertTrue(menu.clickMenuButton(player,111) && bank.used()==4,"Zoglin replacement/cost failed");
                test.assertFalse(menu.clickMenuButton(player,999),"Forged mob type accepted");
                test.assertTrue(menu.canEquip()==(rank>=5),"Equipment rank gate wrong");
                test.assertFalse(menu.getSlot(0).mayPlace(new ItemStack(Items.DIRT)) || menu.getSlot(1).mayPlace(new ItemStack(Items.IRON_CHESTPLATE)),"Invalid equipment accepted");
                if(rank>=5){var sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(17);menu.getSlot(0).set(sword);test.assertTrue(bank.gear.getItem(0)==sword,"Editor copied supplied gear");
                    test.assertTrue(menu.clickMenuButton(player,100),"Mob replacement failed");test.assertTrue(bank.gear.getItem(0).isEmpty() && player.getInventory().items.stream().anyMatch(item->item.is(Items.IRON_SWORD) && item.getDamageValue()==17),"Replacement did not return supplied sword with its wear");}
                test.assertTrue(menu.clickMenuButton(player,2) && bank.types.isEmpty(),"Removal failed");
                test.assertTrue(menu.clickMenuButton(player,110) && bank.used()==3,"Horse cost/empty-roster selection wrong");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(menu.stillValid(player),"Editor accepts switched weapon identity");
            }
            var stack=weapon(player,5);var bank=NecromancerLoadouts.get(player,stack);roster(bank,0);var helmet=new ItemStack(Items.DIAMOND_HELMET);bank.gear.setItem(1,helmet);
            for(int i=0;i<36;i++)if(i!=player.getInventory().selected)player.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
            bank.remove(0,player);var drops=player.level().getEntitiesOfClass(ItemEntity.class,player.getBoundingBox().inflate(1),item->item.getItem()==helmet);
            test.assertTrue(drops.size()==1 && drops.getFirst().position().distanceTo(player.position())<.01,"Full inventory did not return gear at player's feet");drops.forEach(Entity::discard);
        }finally{clean(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_necromancer_bank")
    public static void equipmentWearDeathReusePersistenceAndCopyLock(GameTestHelper test){
        var player=player(test);var other=TestPlayers.survival(test);other.setPos(player.position().add(10,0,0));
        var stack=weapon(player,5);var bank=NecromancerLoadouts.get(player,stack);roster(bank,0);
        var bow=new ItemStack(Items.BOW);var helmet=new ItemStack(Items.DIAMOND_HELMET);bow.enchant(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FLAME),1);bank.gear.setItem(0,bow);bank.gear.setItem(1,helmet);bank.publish(stack);
        var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setPos(player.position().add(0,0,7));test.getLevel().addFreshEntity(zombie);
        try{
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Equipped summon failed");var guard=(net.minecraft.world.entity.monster.AbstractSkeleton)guards(player).getFirst();
            test.assertTrue(guard.getMainHandItem()==bow && guard.getItemBySlot(EquipmentSlot.HEAD)==helmet,"Summon copied bank equipment");
            guard.performRangedAttack(zombie,1);test.assertTrue(bow.getDamageValue()==1,"Real supplied bow did not wear");
            test.assertTrue(test.getLevel().getEntitiesOfClass(AbstractArrow.class,guard.getBoundingBox().inflate(2),arrow->arrow.getOwner()==player && arrow.isOnFire()).size()==1,"Supplied Flame enchantment ignored");
            guard.hurt(player.damageSources().playerAttack(player),4);test.assertTrue(helmet.getDamageValue()>0,"Player-style armor durability ignored");
            var menu=new NecromancerMenu(1,player.getInventory(),player.getInventory().selected);test.assertFalse(menu.stillValid(player) || menu.clickMenuButton(player,2) || bank.replace(0,1,6,player),"Active roster editable");
            var copy=stack.copy();other.setItemInHand(InteractionHand.MAIN_HAND,copy);test.assertTrue(NecromancerLoadouts.get(other,copy)==bank,"Weapon copy cloned equipment bank");test.assertFalse(BoneGuardEvents.activate(other,copy),"Second owner reused already active bank");
            var save=player.server.overworld().getDataStorage();
            var storage=save.computeIfAbsent(new net.minecraft.world.level.saveddata.SavedData.Factory<>(NecromancerLoadouts::new,NecromancerLoadouts::load),"forgermod_necromancer_loadouts");var data=storage.save(new net.minecraft.nbt.CompoundTag(),player.registryAccess());var saved=NecromancerLoadouts.load(data,player.registryAccess());
            test.assertTrue(saved.save(new net.minecraft.nbt.CompoundTag(),player.registryAccess()).equals(data),"Equipment bank serialization lost item/enchantment/durability data");
            guard.hurt(player.damageSources().genericKill(),10000);test.assertTrue(bank.gear.getItem(0)==bow && bank.gear.getItem(1)==helmet && !BoneGuardEvents.active(player),"Dead summon consumed/released bank ownership incorrectly");
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Resummon failed");var next=guards(player).getFirst();test.assertTrue(next.getMainHandItem()==bow && bow.getDamageValue()==1,"Resummon reset durability or copied gear");
            test.assertFalse(stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().contains("Gear"),"Weapon carries clonable gear NBT");
        }finally{clean(player);clean(other);zombie.discard();}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_necromancer_enchantments")
    public static void suppliedMeleeRangedAndMendingUseRealEquipment(GameTestHelper test){
        var player=player(test);var stack=weapon(player,7);var bank=NecromancerLoadouts.get(player,stack);roster(bank,1);
        var registry=player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);var sword=new ItemStack(Items.DIAMOND_SWORD);sword.enchant(registry.getOrThrow(Enchantments.SHARPNESS),5);sword.enchant(registry.getOrThrow(Enchantments.FIRE_ASPECT),2);bank.gear.setItem(0,sword);
        var target=EntityType.ZOMBIE.create(test.getLevel());target.setNoAi(true);target.setPos(player.position().add(0,0,7));target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);test.getLevel().addFreshEntity(target);
        try{
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Melee summon failed");var guard=guards(player).getFirst();guard.setNoAi(true);guard.tick();float before=target.getHealth();
            test.assertTrue(guard.doHurtTarget(target) && before-target.getHealth()>=8 && target.isOnFire() && sword.getDamageValue()==1,"Supplied melee stats/enchantments/durability ignored");
            BoneGuardEvents.cancel(player.getUUID());var bow=new ItemStack(Items.BOW);bow.enchant(registry.getOrThrow(Enchantments.MENDING),1);bow.setDamageValue(20);bank.gear.setItem(0,bow);
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Mending summon failed");guard=guards(player).getFirst();var orb=new ExperienceOrb(test.getLevel(),guard.getX(),guard.getY(),guard.getZ(),3);test.getLevel().addFreshEntity(orb);
            NecromancerEquipment.mend(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(guard));test.assertTrue(bow.getDamageValue()==14 && orb.isRemoved(),"Mending did not consume contact XP to repair actual bank item");
            BoneGuardEvents.cancel(player.getUUID());var crossbow=new ItemStack(Items.CROSSBOW);crossbow.enchant(registry.getOrThrow(Enchantments.MULTISHOT),1);crossbow.enchant(registry.getOrThrow(Enchantments.QUICK_CHARGE),3);bank.gear.setItem(0,crossbow);
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Crossbow summon failed");guard=guards(player).getFirst();guard.setTarget(target);
            for(int i=0;i<25;i++)guard.goalSelector.tick();
            var arrows=test.getLevel().getEntitiesOfClass(AbstractArrow.class,guard.getBoundingBox().inflate(3),arrow->!arrow.isRemoved() && arrow.getOwner()==player);
            test.assertTrue(arrows.size()==3 && crossbow.getDamageValue()>0 && arrows.stream().allMatch(arrow->arrow.pickup==AbstractArrow.Pickup.DISALLOWED),"Native Multishot, crossbow durability or safe projectile ownership missing");
            BoneGuardEvents.cancel(player.getUUID());var trident=new ItemStack(Items.TRIDENT);trident.enchant(registry.getOrThrow(Enchantments.LOYALTY),3);bank.gear.setItem(0,trident);
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Trident summon failed");guard=guards(player).getFirst();guard.setTarget(target);for(int i=0;i<25;i++)guard.goalSelector.tick();
            var spears=test.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.ThrownTrident.class,guard.getBoundingBox().inflate(3),arrow->arrow.getOwner()==player);test.assertTrue(spears.size()==1 && trident.getDamageValue()==1,"Supplied trident not thrown/worn");
            var spear=spears.getFirst();spear.playerTouch(player);test.assertTrue(!spear.isRemoved() && !spear.shouldBeSaved(),"Loyalty trident could duplicate bank equipment");
        }finally{clean(player);target.discard();}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_necromancer_recast")
    public static void packetCooldownStartsOnUseAndReadyRecastReplacesGroup(GameTestHelper test) throws Exception{
        var player=player(test);var stack=weapon(player,5);var bank=NecromancerLoadouts.get(player,stack);roster(bank,0,1);bank.publish(stack);
        try{
            AbilityTestPackets.use(player,0);var first=guards(player);test.assertTrue(first.size()==2,"Initial packet did not summon");String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+AdditionalAbilities.BONE_GUARD;
            long ready=player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L);test.assertTrue(ready==player.level().getGameTime()+RecommendedAbilities.cooldown(AdditionalAbilities.BONE_GUARD,5),"Activation cooldown missing/wrong");
            AbilityTestPackets.use(player,0);test.assertTrue(first.stream().noneMatch(Entity::isRemoved),"Early recast killed army");
            player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of(key,player.level().getGameTime()));AbilityTestPackets.use(player,0);
            test.assertTrue(first.stream().allMatch(Entity::isRemoved) && guards(player).size()==2,"Ready recast did not replace entire army");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
            test.assertTrue(BoneGuardEvents.active(player) && player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key)==ready,"Weapon switch removed army or restarted cooldown");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);var description=AbilityDescriptions.description(stack,AdditionalAbilities.BONE_GUARD,5,false);test.assertTrue(description.getString().contains("1 Skeleton and 1 Zombie") && description.getString().contains("Shift+Alt"),"Description ignores current roster");
        }finally{clean(player);}test.succeed();
    }
}
