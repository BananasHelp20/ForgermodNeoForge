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
    @GameTest(template="riftfang_test",batch="necro_hands")
    public static void specialAxesBlockBothInsertionOrdersAndLegacyOffhandsAreRefunded(GameTestHelper test){
        var player=player(test);var stack=weapon(player,7);var bank=NecromancerLoadouts.get(player,stack);roster(bank,0);
        int checked=0;
        try{
            var menu=new NecromancerMenu(1,player.getInventory(),player.getInventory().selected);
            for(var entry:ModItems.ITEMS.getEntries()){
                var axe=new ItemStack(entry.get());if(!net.bananashelp20.forgermod.item.custom.attacks.axe.AxeItems.isAxe(axe))continue;checked++;
                bank.gear.setItem(5,new ItemStack(Items.SHIELD));test.assertFalse(menu.getSlot(0).mayPlace(axe),"Heavy axe can be inserted beside an offhand");
                bank.gear.setItem(5,ItemStack.EMPTY);test.assertTrue(menu.getSlot(0).mayPlace(axe),"Heavy axe cannot be equipped alone");bank.gear.setItem(0,axe);
                test.assertFalse(menu.getSlot(5).mayPlace(new ItemStack(Items.SHIELD)),"Offhand can be inserted beside heavy axe");bank.gear.setItem(0,ItemStack.EMPTY);
            }
            test.assertTrue(checked==45 && NecromancerEquipment.validHands(new ItemStack(Items.IRON_AXE),new ItemStack(Items.SHIELD)),"Wrong special/ordinary axe restrictions");
            bank.gear.setItem(0,new ItemStack(ModItems.GHOST_AXE.get()));bank.gear.setItem(5,new ItemStack(Items.SHIELD));
            new NecromancerMenu(2,player.getInventory(),player.getInventory().selected);
            test.assertTrue(bank.gear.getItem(5).isEmpty() && player.getInventory().items.stream().filter(item->item.is(Items.SHIELD)).mapToInt(ItemStack::getCount).sum()==1,"Legacy invalid offhand lost or duplicated on refund");
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Cannot summon heavy-axe reinforcement");var guard=guards(player).getFirst();
            guard.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.APPLE));NecromancerEquipment.hands(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(guard));
            test.assertTrue(guard.getOffhandItem().isEmpty() && player.getInventory().items.stream().filter(item->item.is(Items.APPLE)).mapToInt(ItemStack::getCount).sum()==1,"Runtime offhand restriction loses the item or leaves it equipped");
        }finally{clean(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necromancer_ui")
    public static void consumedOffhandTotemMarksBankDirtyAndDoesNotReturnOnSave(GameTestHelper test){
        var player=player(test);var stack=weapon(player,5);var bank=NecromancerLoadouts.get(player,stack);roster(bank,0);bank.gear.setItem(5,new ItemStack(Items.TOTEM_OF_UNDYING));
        var storage=player.server.overworld().getDataStorage().computeIfAbsent(new net.minecraft.world.level.saveddata.SavedData.Factory<>(NecromancerLoadouts::new,NecromancerLoadouts::load),"forgermod_necromancer_loadouts");
        try{
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Cannot summon totem reinforcement");var guard=guards(player).getFirst();guard.setNoAi(true);storage.setDirty(false);
            guard.hurt(player.damageSources().playerAttack(player),1000);
            test.assertTrue(guard.isAlive() && bank.gear.getItem(5).isEmpty() && storage.isDirty(),"Consumed offhand totem remains bank-owned or was not marked for saving");
        }finally{clean(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necromancer_ui")
    public static void pickerAppendsSelectsAndCancelsWithoutReplacingGearAndSlotsFollowSelection(GameTestHelper test){
        var player=player(test);var stack=weapon(player,7);var bank=NecromancerLoadouts.get(player,stack);roster(bank,0,1);bank.publish(stack);
        var sword=new ItemStack(Items.DIAMOND_SWORD);var bow=new ItemStack(Items.BOW);var shield=new ItemStack(Items.SHIELD);
        bank.gear.setItem(0,sword);bank.gear.setItem(6,bow);bank.gear.setItem(11,shield);
        try{
            var menu=new NecromancerMenu(1,player.getInventory(),player.getInventory().selected);
            test.assertTrue(menu.selected()==0 && menu.rosterSize()==2 && menu.slots.size()==42 && menu.getSlot(0).getItem()==sword,"Default selection, six equipment slots or inventory layout wrong");
            test.assertTrue(menu.getSlot(0).x==9 && menu.getSlot(1).y==8 && menu.getSlot(5).x==57 && menu.getSlot(6).y==84 && menu.getSlot(33).y==142,"Slots do not align with redesigned texture");
            test.assertTrue(menu.clickMenuButton(player,11) && menu.getSlot(0).getItem()==bow && menu.getSlot(5).getItem()==shield,"Selection does not expose correct hand items");
            test.assertFalse(menu.clickMenuButton(player,200),"Forged selection appends without opening picker");
            test.assertTrue(menu.clickMenuButton(player,1) && menu.picking() && bank.types.equals(List.of(0,1)),"Add mutates roster before a mob is chosen");
            test.assertTrue(!menu.getSlot(0).isActive() && !menu.getSlot(6).isActive() && menu.quickMoveStack(player,0).isEmpty(),"Picker leaves inventory interactions enabled");
            test.assertTrue(menu.clickMenuButton(player,206) && !menu.picking() && menu.selected()==2 && bank.types.equals(List.of(0,1,6)),"Chosen reinforcement is not appended and selected");
            test.assertTrue(bank.gear.getItem(0)==sword && bank.gear.getItem(6)==bow && bank.gear.getItem(11)==shield,"Appending replaces or clones existing equipment");
            test.assertTrue(menu.clickMenuButton(player,1) && menu.clickMenuButton(player,3) && bank.types.size()==3,"Back changes roster");
            player.getInventory().setItem(9,new ItemStack(Items.IRON_HELMET));menu.quickMoveStack(player,6);
            test.assertTrue(menu.getSlot(1).getItem().is(Items.IRON_HELMET) && menu.getSlot(0).getItem().isEmpty(),"Shift-click armor chooses main hand instead of armor slot");
            player.getInventory().setItem(10,new ItemStack(Items.SHIELD));menu.quickMoveStack(player,7);
            test.assertTrue(menu.getSlot(5).getItem().is(Items.SHIELD),"Shift-click shield does not choose offhand");
            test.assertTrue(menu.clickMenuButton(player,301) && bank.types.equals(List.of(0,6)) && menu.selected()==1,"Removing preceding row changes selected reinforcement");
            test.assertTrue(menu.getSlot(1).getItem().is(Items.IRON_HELMET) && menu.getSlot(5).getItem().is(Items.SHIELD)
                    && player.getInventory().items.stream().filter(item->item.is(Items.BOW)).mapToInt(ItemStack::getCount).sum()==1 && player.getInventory().items.stream().filter(item->item.is(Items.SHIELD)).mapToInt(ItemStack::getCount).sum()==1,"Removing row loses, mixes or duplicates hand/armor items");
            while(menu.used()<menu.capacity()){test.assertTrue(menu.clickMenuButton(player,1) && menu.clickMenuButton(player,200),"Cannot fill remaining roster capacity");}
            test.assertTrue(!menu.clickMenuButton(player,1) && menu.rosterSize()==9,"Full roster opens add picker or has wrong capacity count");
        }finally{clean(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necromancer_ui")
    public static void legacyFiveSlotSavesMigrateEachRowAndNewOffhandSavesRoundTrip(GameTestHelper test){
        var registry=test.getLevel().registryAccess();var root=new net.minecraft.nbt.CompoundTag();var record=new net.minecraft.nbt.CompoundTag();record.putUUID("Id",UUID.randomUUID());record.putIntArray("Types",new int[]{0,1});
        var gear=new net.minecraft.nbt.ListTag();int[] oldSlots={0,4,5,9};var items=List.of(new ItemStack(Items.BOW),new ItemStack(Items.IRON_BOOTS),new ItemStack(Items.DIAMOND_SWORD),new ItemStack(Items.DIAMOND_BOOTS));
        for(int i=0;i<oldSlots.length;i++){var entry=new net.minecraft.nbt.CompoundTag();entry.putInt("Slot",oldSlots[i]);entry.put("Item",items.get(i).save(registry));gear.add(entry);}
        record.put("Gear",gear);var records=new net.minecraft.nbt.ListTag();records.add(record);root.put("Loadouts",records);
        var migrated=NecromancerLoadouts.load(root,registry).save(new net.minecraft.nbt.CompoundTag(),registry);var saved=migrated.getList("Loadouts",10).getCompound(0);var savedGear=saved.getList("Gear",10);
        test.assertTrue(saved.getInt("EquipmentSlots")==6 && savedGear.size()==4,"Legacy format not marked migrated");int[] expected={0,4,6,10};
        for(int i=0;i<expected.length;i++)test.assertTrue(savedGear.getCompound(i).getInt("Slot")==expected[i] && ItemStack.matches(ItemStack.parse(registry,savedGear.getCompound(i).getCompound("Item")).orElse(ItemStack.EMPTY),items.get(i)),"Migration moves old row equipment into wrong slot");
        var offhand=new net.minecraft.nbt.CompoundTag();offhand.putInt("Slot",11);offhand.put("Item",new ItemStack(Items.TOTEM_OF_UNDYING).save(registry));savedGear.add(offhand);
        test.assertTrue(NecromancerLoadouts.load(migrated,registry).save(new net.minecraft.nbt.CompoundTag(),registry).equals(migrated),"New offhand items are lost on save/load");test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necromancer_ui")
    public static void summonedOffhandUsesRealBankItemAndSurvivesDismissal(GameTestHelper test){
        var player=player(test);var stack=weapon(player,5);var bank=NecromancerLoadouts.get(player,stack);roster(bank,0);var shield=new ItemStack(Items.SHIELD);bank.gear.setItem(5,shield);
        try{
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Cannot summon equipped reinforcement");var guard=guards(player).getFirst();
            test.assertTrue(guard.getOffhandItem()==shield && NecromancerEquipment.supplied(guard,EquipmentSlot.OFFHAND),"Summon offhand is copied or missing");
            BoneGuardEvents.cancel(player.getUUID());test.assertTrue(bank.gear.getItem(5)==shield && guard.getOffhandItem().isEmpty(),"Dismissal loses or duplicates real offhand");
        }finally{clean(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void customizationDescriptionUsesConfiguredKeybindForEverySuperRankAndVariant(GameTestHelper test){
        int checked=0;
        for(var entry:ModItems.ITEMS.getEntries()){
            if(!(entry.get() instanceof net.bananashelp20.forgermod.item.custom.MorsiumWeapon weapon) || weapon.isDagger())continue;
            for(int rank=4;rank<=7;rank++){
                var stack=Augmentations.setLevel(new ItemStack(entry.get()),AdditionalAbilities.BONE_GUARD,rank);
                var description=AbilityDescriptions.description(stack,AdditionalAbilities.BONE_GUARD,rank,false);
                test.assertTrue(description.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents,"Description lost translation");
                var values=((net.minecraft.network.chat.contents.TranslatableContents)description.getContents()).getArgs();
                test.assertTrue(values.length==2 && values[1] instanceof net.minecraft.network.chat.Component key
                        && key.getContents() instanceof net.minecraft.network.chat.contents.KeybindContents content
                        && content.getName().equals("key.forgermod.necromancer_customize"),"Customization tooltip hardcodes old shortcut");checked++;
            }
        }
        test.assertTrue(checked==40,"Skipped Necromancer variants/ranks");test.succeed();
    }
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
                test.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(Items.DIRT))==(rank>=5) && !menu.getSlot(1).mayPlace(new ItemStack(Items.IRON_CHESTPLATE)),"Hand/armor equipment acceptance or rank gate wrong");
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
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);var description=AbilityDescriptions.description(stack,AdditionalAbilities.BONE_GUARD,5,false);test.assertTrue(description.getString().contains("1 Skeleton and 1 Zombie") && description.getString().contains("Customize reinforcements"),"Description ignores current roster");
        }finally{clean(player);}test.succeed();
    }
}
