package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class NecromancerPassiveGameTests {
    private record Fixture(ServerPlayer owner,Mob guard,Mob target){}
    private static Fixture fixture(GameTestHelper test){
        var owner=TestPlayers.survival(test);owner.setNoGravity(true);owner.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));
        for(int x=1;x<=20;x++)for(int z=1;z<=20;z++){test.setBlock(new BlockPos(x,4,z),Blocks.STONE);for(int y=5;y<=10;y++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);}
        var weapon=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD);owner.setItemInHand(InteractionHand.MAIN_HAND,weapon);BoneGuardEvents.activate(owner,weapon);
        var guard=owner.level().getEntitiesOfClass(Mob.class,owner.getBoundingBox().inflate(6),m->m instanceof BoneGuardEvents.FriendlyGuard g && g.owner()==owner).getFirst();guard.setNoAi(true);guard.setNoGravity(true);guard.setPos(owner.position().add(0,0,3));
        var target=EntityType.ZOMBIE.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(guard.position().add(0,0,1));
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.getAttribute(Attributes.ARMOR).setBaseValue(0);target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);target.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));test.getLevel().addFreshEntity(target);
        return new Fixture(owner,guard,target);
    }
    private static ItemStack learn(Item item,String id,int rank){var stack=new ItemStack(item);for(var prerequisite:Augmentations.prerequisites(stack,id))stack=Augmentations.setLevel(stack,prerequisite,1);return Augmentations.setLevel(stack,id,rank);}
    private static void equip(Mob guard,ItemStack main,ItemStack off){guard.setItemSlot(EquipmentSlot.MAINHAND,main);guard.setItemSlot(EquipmentSlot.OFFHAND,off);guard.tick();tick(guard);}
    private static void tick(Mob guard){PassiveActors.mobPre(new EntityTickEvent.Pre(guard));PassiveActors.mobPost(new EntityTickEvent.Post(guard));}
    private static void close(Fixture f){BoneGuardEvents.cancel(f.owner().getUUID());f.target().discard();f.owner().server.getPlayerList().remove(f.owner());}
    @GameTest(template="riftfang_test",batch="necro_passive_catalog",timeoutTicks=200)
    public static void everyRegisteredPassiveAndWeaponVariantCanBeEquippedHitAndCleanedUp(GameTestHelper test){
        var f=fixture(test);Set<String> ids=new HashSet<>();int checked=0;
        try{for(var entry:ModItems.ITEMS.getEntries()){
            var fresh=new ItemStack(entry.get());if(!Augmentations.eligible(fresh))continue;
            for(var ability:Augmentations.pool(fresh))if(!ability.active()){
                var weapon=learn(entry.get(),ability.id(),1);test.assertFalse(weapon.isEmpty(),"Cannot equip passive "+ability.id());
                equip(f.guard(),weapon,ItemStack.EMPTY);f.target().invulnerableTime=0;f.target().setHealth(1000);f.guard().clearFire();
                NecromancerPassiveCombat.attack(f.guard(),f.target());tick(f.guard());
                test.assertTrue(f.owner().getHealth()==f.owner().getMaxHealth(),"Passive hurts summoner: "+ability.id());
                equip(f.guard(),ItemStack.EMPTY,ItemStack.EMPTY);ScarCompassEvents.end(f.guard().getUUID());ids.add(ability.id());checked++;
            }
        }
        test.assertTrue(checked>=500 && ids.size()>=40,"Passive catalog coverage incomplete: "+checked+" variants, "+ids.size()+" passives");
        }finally{close(f);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necro_passive_health")
    public static void leechPyromaniacHeatingUpLastStandAndOffhandSharingBelongToTheReinforcement(GameTestHelper test){
        var f=fixture(test);
        try{
            var main=new ItemStack(ModItems.ASSASSIN_DAGGER.get());var off=learn(ModItems.ASSASSIN_DAGGER_RUBY.get(),"tooltips.forgermod.ability.leech",1);off=Augmentations.setLevel(off,Augmentations.DUAL_WIELD,1);
            equip(f.guard(),main,off);f.guard().setHealth(5);float before=f.target().getHealth();NecromancerPassiveCombat.attack(f.guard(),f.target());
            test.assertTrue(f.guard().getHealth()>5 && f.target().getHealth()<before-8,"Offhand-only Leech/Dual Wield does not heal the guard and strike twice");
            var pyro=learn(ModItems.EMBERFANG_DAGGER.get(),net.bananashelp20.forgermod.item.custom.IgnisiumWeapon.PYROMANIAC,1);equip(f.guard(),pyro,ItemStack.EMPTY);f.guard().setHealth(5);f.guard().setRemainingFireTicks(100);f.target().invulnerableTime=0;
            NecromancerPassiveCombat.attack(f.guard(),f.target());test.assertTrue(f.guard().getHealth()==7 && f.guard().getRemainingFireTicks()<=0,"Pyromaniac fails to extinguish and heal guard");
            equip(f.guard(),learn(ModItems.INFERNAL_CLAYMORE.get(),RecommendedAbilities.HEATING_UP,1),ItemStack.EMPTY);f.guard().setHealth(5);f.guard().setRemainingFireTicks(100);
            test.assertFalse(f.guard().hurt(f.guard().damageSources().onFire(),1),"Heating Up takes burn damage");for(int i=0;i<40;i++)HeatingUpEvents.onTick(new PassiveActorTickEvent.Pre(f.guard()));
            test.assertTrue(f.guard().getHealth()==6,"Heating Up does not heal burning guard");
            equip(f.guard(),learn(ModItems.HOLLOW_CLAYMORE.get(),AdditionalAbilities.LAST_STAND,1),ItemStack.EMPTY);f.guard().setHealth(10);f.guard().invulnerableTime=0;f.guard().hurt(f.guard().damageSources().mobAttack(f.target()),100);
            test.assertTrue(f.guard().isAlive() && f.guard().getHealth()==1,"Last Stand does not protect guard");
            test.assertTrue(f.owner().getHealth()==f.owner().getMaxHealth() && !f.owner().hasEffect(MobEffects.FIRE_RESISTANCE),"Guard passives leaked to summoner");
        }finally{close(f);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necro_passive_attributes")
    public static void heldBuffsAttackSpeedAndCleanupApplyToAllReinforcementTypes(GameTestHelper test){
        var f=fixture(test);
        try{
            equip(f.guard(),learn(ModItems.INFERNAL_CLAYMORE.get(),RecommendedAbilities.FIRE_RESISTANCE,1),ItemStack.EMPTY);tick(f.guard());test.assertTrue(f.guard().hasEffect(MobEffects.FIRE_RESISTANCE),"Fire resistance not granted to guard");
            equip(f.guard(),ItemStack.EMPTY,ItemStack.EMPTY);test.assertFalse(f.guard().hasEffect(MobEffects.FIRE_RESISTANCE),"Weapon switch leaves owned fire resistance");
            equip(f.guard(),learn(ModItems.NIGHTMARE_DAGGER.get(),AdditionalAbilities.LONELY_DREAM,1),ItemStack.EMPTY);LonelyDreamEvents.refresh(f.guard());
            test.assertTrue(f.guard().getAttribute(Attributes.ATTACK_SPEED).hasModifier(LonelyDreamEvents.MODIFIER),"Lonely Dream does not change guard attack speed");
            equip(f.guard(),ItemStack.EMPTY,ItemStack.EMPTY);test.assertFalse(f.guard().getAttribute(Attributes.ATTACK_SPEED).hasModifier(LonelyDreamEvents.MODIFIER),"Attack-speed passive leaks after switch");
            equip(f.guard(),learn(ModItems.SHRIEKING_CLAYMORE.get(),AdditionalAbilities.HOMESICK,1),ItemStack.EMPTY);f.guard().setOnGround(true);test.getLevel().setBlockAndUpdate(f.guard().getOnPos(),Blocks.SCULK.defaultBlockState());HomesickEvents.onTick(new PassiveActorTickEvent.Post(f.guard()));
            test.assertTrue(f.guard().hasEffect(MobEffects.MOVEMENT_SPEED) && f.guard().hasEffect(MobEffects.DAMAGE_BOOST) && !f.owner().hasEffect(MobEffects.DAMAGE_BOOST),"Homesick does not belong to guard");
            for(var choice:NecromancerCatalog.CHOICES){var unit=NecromancerUndead.create(NecromancerCatalog.CHOICES.indexOf(choice),f.owner());for(var mob:unit.members()){test.assertTrue(mob.getAttribute(Attributes.ATTACK_SPEED)!=null && mob.getAttribute(Attributes.ARMOR)!=null && mob.getAttribute(Attributes.MAX_ABSORPTION)!=null,"Reinforcement type lacks passive attribute support");mob.discard();}}
        }finally{close(f);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necro_passive_shields")
    public static void automaticShieldBlocksWearRealGearAndColdBellowsIgnitesTheAttacker(GameTestHelper test){
        var f=fixture(test);
        try{
            equip(f.guard(),learn(ModItems.INFERNAL_CLAYMORE.get(),RecommendedAbilities.COLD_BELLOWS,1),new ItemStack(Items.SHIELD));f.guard().setTarget(f.target());
            var defense=new NecromancerPassiveCombat.Shield(f.guard());test.assertTrue(defense.canUse(),"Guard does not choose to defend with shield");defense.start();
            for(int i=0;i<6;i++){defense.tick();f.guard().tick();}test.assertTrue(f.guard().isBlocking(),"Guard shield never becomes active");
            float health=f.guard().getHealth();f.target().clearFire();f.guard().invulnerableTime=0;f.guard().hurt(f.guard().damageSources().mobAttack(f.target()),4);
            test.assertTrue(f.guard().getHealth()==health && f.target().getRemainingFireTicks()==60,"Shield/Cold Bellows does not block and ignite attacker");
            test.assertTrue(f.guard().getOffhandItem().getDamageValue()==5,"Automatic shield does not receive vanilla durability wear");defense.stop();test.assertFalse(f.guard().isUsingItem(),"Shield does not lower for counterattack");
        }finally{close(f);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necro_passive_chain")
    public static void chainAndRevengeDamageKeepSummonerAndAlliedGuardsSafe(GameTestHelper test){
        var f=fixture(test);var friend=NecromancerUndead.create(0,f.owner()).equipment();friend.setNoAi(true);friend.setNoGravity(true);friend.setPos(f.guard().position().add(.5,0,0));test.getLevel().addFreshEntity(friend);
        var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(f.target().position().add(0,0,3));test.getLevel().addFreshEntity(cow);
        try{
            equip(f.guard(),learn(ModItems.STATIC_DAGGER.get(),"tooltips.forgermod.passive.chain_lightning",1),ItemStack.EMPTY);
            long seed=0;while(!net.minecraft.util.RandomSource.create(seed).nextBoolean())seed++;f.guard().getRandom().setSeed(seed);
            ((net.bananashelp20.forgermod.item.custom.ElectriumWeapon)f.guard().getMainHandItem().getItem()).passiveChainLightning(f.target(),f.guard());
            test.assertTrue(cow.getHealth()<cow.getMaxHealth() && friend.getHealth()==friend.getMaxHealth() && f.owner().getHealth()==f.owner().getMaxHealth(),"Chain misses neutral mob or hurts friendly entities");
            cow.discard();equip(f.guard(),learn(ModItems.CURSEBLOOD_CLAYMORE.get(),AdditionalAbilities.REVENGEFUL_EXIT,1),ItemStack.EMPTY);
            float damage=(float)f.guard().getAttributeValue(Attributes.ATTACK_DAMAGE),before=f.target().getHealth();f.target().invulnerableTime=0;
            f.guard().hurt(f.guard().damageSources().mobAttack(f.target()),1000);
            test.assertTrue(!f.guard().isAlive() && Math.abs(before-f.target().getHealth()-damage*6)<.001,"Revengeful Exit loses the dying guard weapon or damage");
            test.assertTrue(f.owner().getHealth()==f.owner().getMaxHealth() && friend.getHealth()==friend.getMaxHealth(),"Revengeful Exit harms friendly entities");
        }finally{friend.discard();cow.discard();close(f);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necro_passive_soul")
    public static void killHealingAndAbsorptionAreGrantedToGuardInsteadOfSummoner(GameTestHelper test){
        var f=fixture(test);
        try{
            equip(f.guard(),learn(ModItems.HOLLOW_CLAYMORE.get(),AdditionalAbilities.SOUL_HUNGER,1),ItemStack.EMPTY);f.target().setHealth(1);f.target().invulnerableTime=0;NecromancerPassiveCombat.attack(f.guard(),f.target());
            test.assertTrue(f.guard().getAbsorptionAmount()==2 && f.owner().getAbsorptionAmount()==0,"Soul Hunger grants hearts to wrong actor");
            equip(f.guard(),ItemStack.EMPTY,ItemStack.EMPTY);SoulHungerEvents.end(f.guard());
            var victim=EntityType.ZOMBIE.create(test.getLevel());victim.setNoAi(true);victim.setNoGravity(true);victim.setPos(f.guard().position().add(0,0,1));test.getLevel().addFreshEntity(victim);
            equip(f.guard(),learn(ModItems.DREAMBOUND_CLAYMORE.get(),AdditionalAbilities.UNFINISHED_DREAM,1),ItemStack.EMPTY);f.guard().setHealth(5);victim.setHealth(1);NecromancerPassiveCombat.attack(f.guard(),victim);
            test.assertTrue(f.guard().getHealth()==6,"Unfinished Dream fails to heal reinforcement after untouched enemy kill");victim.discard();
            equip(f.guard(),learn(ModItems.CLAYMORE_OF_THUNDER.get(),AdditionalAbilities.LIGHTNING_ROD,1),ItemStack.EMPTY);test.assertTrue(LightningRodEvents.grant(f.guard()),"Lightning Rod does not accept reinforcement");
            test.assertTrue(f.guard().getAbsorptionAmount()==10 && f.guard().hasEffect(MobEffects.DAMAGE_BOOST),"Lightning Rod grants wrong actor/buffs");
            equip(f.guard(),ItemStack.EMPTY,ItemStack.EMPTY);test.assertTrue(f.guard().getAbsorptionAmount()==0 && !f.guard().hasEffect(MobEffects.DAMAGE_BOOST),"Lightning Rod does not clean up on switch");
        }finally{close(f);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necro_passive_damage")
    public static void backstabEchoingSpeedAndOffhandRanksUseTheReinforcementWeapon(GameTestHelper test){
        var f=fixture(test);
        try{
            var plain=new ItemStack(ModItems.RIFTFANG_DAGGER.get());equip(f.guard(),plain,ItemStack.EMPTY);f.target().setYRot(0);
            float base=(float)f.guard().getAttributeValue(Attributes.ATTACK_DAMAGE),before=f.target().getHealth();NecromancerPassiveCombat.attack(f.guard(),f.target());
            test.assertTrue(Math.abs(before-f.target().getHealth()-base)<.001,"Baseline guard weapon damage wrong");
            var backstab=learn(ModItems.RIFTFANG_DAGGER.get(),AdditionalAbilities.BACKSTAB,1);equip(f.guard(),backstab,ItemStack.EMPTY);f.target().invulnerableTime=0;before=f.target().getHealth();NecromancerPassiveCombat.attack(f.guard(),f.target());
            test.assertTrue(Math.abs(before-f.target().getHealth()-base*1.2)<.001,"Backstab modifier is player-only or uses summoner facing");
            var off=learn(ModItems.RIFTFANG_DAGGER_RUBY.get(),RecommendedAbilities.ECHOING_SPEED,4);off=Augmentations.setLevel(off,Augmentations.DUAL_WIELD,1);equip(f.guard(),plain,off);int interval=NecromancerPassiveCombat.interval(f.guard());
            for(int i=0;i<3;i++){f.target().invulnerableTime=0;NecromancerPassiveCombat.attack(f.guard(),f.target());}
            test.assertTrue(NecromancerPassiveCombat.interval(f.guard())<interval,"Offhand Echoing Speed does not increase actual guard attack speed");
            equip(f.guard(),ItemStack.EMPTY,ItemStack.EMPTY);test.assertTrue(f.guard().getAttribute(Attributes.ATTACK_SPEED).getModifiers().stream().noneMatch(m->m.id().getPath().equals("echoing_speed")),"Offhand passive state leaks after unequipping");
        }finally{close(f);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necro_passive_delayed",timeoutTicks=260)
    public static void aftershockUsesGuardDamageAndActiveDistanceTaxDoesNotFireForReinforcements(GameTestHelper test){
        var f=fixture(test);
        var weapon=learn(ModItems.SHRIEKING_CLAYMORE.get(),AdditionalAbilities.AFTERSHOCK,1);weapon=Augmentations.setLevel(weapon,AdditionalAbilities.RESONANCE_CRIT,1);equip(f.guard(),weapon,ItemStack.EMPTY);
        f.target().setOnGround(false);f.target().setDeltaMovement(0,.3,0);float before=f.target().getHealth(),base=(float)f.guard().getAttributeValue(Attributes.ATTACK_DAMAGE);NecromancerPassiveCombat.attack(f.guard(),f.target());f.target().setDeltaMovement(Vec3.ZERO);
        test.assertTrue(Math.abs(before-f.target().getHealth()-base*1.5)<.001,"Resonance Crit does not modify reinforcement melee");float after=f.target().getHealth();
        test.runAfterDelay(21,()->{
            test.assertTrue(Math.abs(after-f.target().getHealth()-1)<.001,"Aftershock missing, wrong actor, or recursive");
            equip(f.guard(),learn(ModItems.CLAYMORE_OF_THE_VOID.get(),RecommendedAbilities.DISTANCE_TAX,1),ItemStack.EMPTY);f.target().invulnerableTime=0;NecromancerPassiveCombat.attack(f.guard(),f.target());
            float marked=f.target().getHealth();f.guard().setPos(f.guard().position().add(0,0,-8));
            test.runAfterDelay(201,()->{
                try{test.assertTrue(Math.abs(marked-f.target().getHealth())<.01,"Reinforcement automatically uses active Distance Tax");test.succeed();}
                finally{close(f);}
            });
        });
    }
    @GameTest(template="riftfang_test",batch="necro_passive_slam",timeoutTicks=250)
    public static void axeSlamRequiresThreeFullyChargedAttacksAndDoesNotHitSummoner(GameTestHelper test){
        var f=fixture(test);equip(f.guard(),learn(ModItems.GHOST_AXE.get(),Augmentations.VERTICAL_STRIKE,1),ItemStack.EMPTY);f.guard().lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,f.target().getEyePosition());
        int interval=NecromancerPassiveCombat.interval(f.guard());float damage=(float)f.guard().getAttributeValue(Attributes.ATTACK_DAMAGE);
        for(int i=0;i<3;i++){f.target().invulnerableTime=0;NecromancerPassiveCombat.attack(f.guard(),f.target());}
        test.runAfterDelay(6,()->test.assertTrue(Math.abs(1000-f.target().getHealth()-3*damage)<.02,"Weak follow-up attacks charge an early slam: health="+f.target().getHealth()+" damage="+damage));
        test.runAfterDelay(interval+1,()->{f.target().invulnerableTime=0;NecromancerPassiveCombat.attack(f.guard(),f.target());});
        test.runAfterDelay(2*interval+2,()->{f.target().invulnerableTime=0;NecromancerPassiveCombat.attack(f.guard(),f.target());});
        test.runAfterDelay(2*interval+7,()->{
            try{test.assertTrue(Math.abs(1000-f.target().getHealth()-6*damage)<.02 && f.owner().getHealth()==f.owner().getMaxHealth(),"Three charged attacks do not produce one safe weapon-damage slam");test.succeed();}
            finally{close(f);}
        });
    }
    @GameTest(template="riftfang_test",batch="necro_passive_flowers")
    public static void graveVaultPlacesAndConsumesStoredFlowersAndPhotosynthesisRepairsGuardWeapon(GameTestHelper test){
        var f=fixture(test);long day=test.getLevel().getDayTime();
        try{
            var weapon=learn(ModItems.OVERGROWN_CLAYMORE.get(),RecommendedAbilities.GRAVE_VAULT,1);var storage=GraveVaultEvents.read(weapon,test.getLevel().registryAccess());storage.setItem(0,new ItemStack(Items.DANDELION,2));GraveVaultEvents.write(weapon,storage,test.getLevel().registryAccess());
            test.getLevel().setBlockAndUpdate(f.target().getOnPos(),Blocks.DIRT.defaultBlockState());var flowerPos=f.target().getOnPos().above();equip(f.guard(),weapon,ItemStack.EMPTY);f.target().setHealth(1);NecromancerPassiveCombat.attack(f.guard(),f.target());
            test.assertTrue(test.getLevel().getBlockState(flowerPos).is(Blocks.DANDELION) && GraveVaultEvents.read(weapon,test.getLevel().registryAccess()).getItem(0).getCount()==1,"Grave Vault does not place/consume reinforcement weapon flowers");
            test.getLevel().setDayTime(6000);var repair=learn(ModItems.OVERGROWN_CLAYMORE.get(),AdditionalAbilities.PHOTOSYNTHESIS,1);repair.setDamageValue(10);equip(f.guard(),repair,ItemStack.EMPTY);
            int before=repair.getDamageValue();for(int i=0;i<300;i++)PhotosynthesisEvents.tick(new PassiveActorTickEvent.Pre(f.guard()));
            test.assertTrue(repair.getDamageValue()==before-1,"Photosynthesis does not repair reinforcement weapon");
        }finally{test.getLevel().setDayTime(day);close(f);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necro_passive_isolation")
    public static void activeAugmentsStayInactiveAndPlayerOnlyFoodPassivesDoNotFeedOwner(GameTestHelper test){
        var f=fixture(test);var ownerFood=f.owner().getFoodData();ownerFood.setFoodLevel(10);ownerFood.setSaturation(0);
        try{
            var weapon=learn(ModItems.INFERNAL_CLAYMORE.get(),RecommendedAbilities.EXPLOSION,1);weapon=Augmentations.setLevel(weapon,RecommendedAbilities.FIRE_RESISTANCE,1);equip(f.guard(),weapon,ItemStack.EMPTY);
            for(int i=0;i<40;i++)tick(f.guard());test.assertTrue(f.guard().hasEffect(MobEffects.FIRE_RESISTANCE) && f.guard().isAlive() && !RecommendedAbilityRuntime.active(f.owner(),RecommendedAbilities.EXPLOSION),"Equipped active augment is automatically activated");
            equip(f.guard(),learn(ModItems.OVERGROWN_CLAYMORE.get(),AdditionalAbilities.SUNFED,1),ItemStack.EMPTY);for(int i=0;i<320;i++)tick(f.guard());
            test.assertTrue(ownerFood.getFoodLevel()==10 && ownerFood.getSaturationLevel()==0,"Reinforcement hunger passive feeds summoner");
        }finally{close(f);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necro_passive_xp")
    public static void reinforcementKillsGiveHalfExperienceAndDeathsDueBonusBeforeHalving(GameTestHelper test){
        var f=fixture(test);var observed=new java.util.ArrayList<net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent> listener=event->{if(event.getEntity()==f.target())observed.add(event);};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
        try{
            equip(f.guard(),new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),ItemStack.EMPTY);f.target().setHealth(1);NecromancerPassiveCombat.attack(f.guard(),f.target());
            test.assertTrue(observed.size()==1 && observed.getFirst().getAttackingPlayer()==f.owner() && observed.getFirst().getDroppedExperience()==observed.getFirst().getOriginalExperience()/2,"Reinforcement melee kill does not grant half of normal zombie XP");
            var victim=EntityType.ZOMBIE.create(test.getLevel());victim.setNoAi(true);victim.setNoGravity(true);victim.setPos(f.guard().position().add(0,0,1));test.getLevel().addFreshEntity(victim);
            equip(f.guard(),learn(ModItems.HOLLOW_CLAYMORE.get(),AdditionalAbilities.DEATHS_DUE,1),ItemStack.EMPTY);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(victim,victim.damageSources().mobAttack(f.guard())));
            var xp=new net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent(victim,f.owner(),8);DeathsDueEvents.experience(xp);
            test.assertTrue(xp.getDroppedExperience()==5,"Death's Due bonus is applied after halving or twice");
            f.owner().setItemInHand(InteractionHand.MAIN_HAND,learn(ModItems.HOLLOW_CLAYMORE.get(),AdditionalAbilities.DEATHS_DUE,1));equip(f.guard(),new ItemStack(Items.BOW),ItemStack.EMPTY);
            var arrow=EntityType.ARROW.create(test.getLevel());arrow.setOwner(f.owner());arrow.getPersistentData().putUUID("forgermod_bone_guard_shooter",f.guard().getUUID());
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(victim,victim.damageSources().arrow(arrow,f.owner())));
            var rangedXp=new net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent(victim,f.owner(),8);DeathsDueEvents.experience(rangedXp);
            test.assertTrue(rangedXp.getDroppedExperience()==4,"Ranged reinforcement XP is not halved or uses summoner's Death's Due");arrow.discard();victim.discard();
        }finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);close(f);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="necro_passive_projectile_owner")
    public static void creditedArrowsDoNotBorrowSummonerHungerOrVibrationPassives(GameTestHelper test){
        var f=fixture(test);var warden=EntityType.WARDEN.create(test.getLevel());var arrow=EntityType.ARROW.create(test.getLevel());
        try{
            var weapon=learn(ModItems.SHRIEKING_CLAYMORE.get(),AdditionalAbilities.ECHO_APPETITE,1);weapon=Augmentations.setLevel(weapon,RecommendedAbilities.WARDEN_TONE,1);f.owner().setItemInHand(InteractionHand.MAIN_HAND,weapon);
            equip(f.guard(),new ItemStack(Items.BOW),ItemStack.EMPTY);f.owner().getFoodData().setFoodLevel(10);f.owner().getFoodData().setSaturation(0);arrow.setOwner(f.owner());arrow.getPersistentData().putUUID("forgermod_bone_guard_shooter",f.guard().getUUID());
            EchoAppetiteEvents.onWardenDeath(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(warden,warden.damageSources().arrow(arrow,f.owner())));
            test.assertTrue(f.owner().getFoodData().getFoodLevel()==10,"Reinforcement kill borrows summoner's hunger passive");
            var vibration=new net.neoforged.neoforge.event.VanillaGameEvent(test.getLevel(),net.minecraft.world.level.gameevent.GameEvent.PROJECTILE_LAND,f.guard().position(),net.minecraft.world.level.gameevent.GameEvent.Context.of(arrow));WardenToneEvents.onVibration(vibration);
            test.assertFalse(vibration.isCanceled(),"Reinforcement arrow borrows summoner's Warden Tone");
            arrow.getPersistentData().remove("forgermod_bone_guard_shooter");
            EchoAppetiteEvents.onWardenDeath(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(warden,warden.damageSources().arrow(arrow,f.owner())));
            test.assertTrue(f.owner().getFoodData().getFoodLevel()==14,"Real player arrow lost Echo Appetite");
            var direct=new net.neoforged.neoforge.event.VanillaGameEvent(test.getLevel(),net.minecraft.world.level.gameevent.GameEvent.PROJECTILE_LAND,f.owner().position(),net.minecraft.world.level.gameevent.GameEvent.Context.of(arrow));WardenToneEvents.onVibration(direct);test.assertTrue(direct.isCanceled(),"Real player arrow lost Warden Tone");
        }finally{arrow.discard();warden.discard();close(f);}test.succeed();
    }
}
