package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.abilities.NecromancerCatalog;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class BoneGuardGameTests {
    private static void assertLineup(GameTestHelper test,java.util.List<Mob> mobs,int rank) {
        int count=rank<4?(rank==1?1:2):4;
        test.assertTrue(mobs.size()==count,"Wrong default roster at rank "+rank);
        test.assertTrue(mobs.stream().filter(m->m.getType()==EntityType.BOGGED).count()==(rank==3?1:0),"Wrong Bogged count");
        for(var mob:mobs){
            test.assertTrue(mob.getMainHandItem().is(Items.BOW) && !mob.getMainHandItem().isEnchanted(),"Unconfigured mob lacks default equipment");
            for(var slot:java.util.List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET))test.assertTrue(mob.getItemBySlot(slot).isEmpty(),"Free armor was supplied");
        }
    }
    private static void mixed(net.minecraft.server.level.ServerPlayer player,ItemStack stack,boolean bowWither){
        var loadout=NecromancerLoadouts.get(player,stack);loadout.types.clear();loadout.types.addAll(java.util.List.of(0,5,6,9,1));
        if(bowWither){var bow=new ItemStack(Items.BOW);bow.enchant(player.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FLAME),1);loadout.gear.setItem(3*NecromancerCatalog.EQUIPMENT_SLOTS,bow);loadout.gear.setItem(4*NecromancerCatalog.EQUIPMENT_SLOTS,new ItemStack(Items.IRON_SWORD));}
        loadout.publish(stack);
    }
    @GameTest(template="riftfang_test")
    public static void allTenVariantsSummonOneFriendlyLootlessArcher(GameTestHelper test) {
        var player=TestPlayers.survival(test);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);
        for(int x=4;x<=14;x++) for(int y=5;y<=8;y++) for(int z=4;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.setPos(point.add(0,0,5));test.getLevel().addFreshEntity(zombie);
        int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof MorsiumWeapon weapon && !weapon.isDagger();
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.BONE_GUARD))==eligible,"Wrong Bone Guard pool");if(!eligible)continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.BONE_GUARD),"Fresh weapon summons guard");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.BONE_GUARD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BONE_GUARD),"Summon failed");
            var guards=player.level().getEntitiesOfClass(BoneGuardEvents.GuardSkeleton.class,player.getBoundingBox().inflate(3));test.assertTrue(guards.size()==1,"Wrong guard count");var guard=guards.getFirst();
            test.assertFalse(WeaponAbilityTargets.enemy(player,guard),"Summon is hostile to owner");test.assertFalse(guard.canAttack(player),"Summon attacks owner");
            test.assertTrue(!guard.shouldBeSaved() && !guard.shouldDropExperience(),"Guard persists or drops XP");
            BoneGuardEvents.tick(new PlayerTickEvent.Pre(player));test.assertTrue(guard.getTarget()==zombie,"Guard does not find enemy");
            guard.performRangedAttack(zombie,1);var arrows=player.level().getEntitiesOfClass(AbstractArrow.class,guard.getBoundingBox().inflate(2),a->a.getOwner()==player);test.assertTrue(arrows.size()==1,"Guard does not shoot actual arrow");
            var arrow=arrows.getFirst();test.assertTrue(arrow.getDeltaMovement().length()>1 && arrow.pickup==AbstractArrow.Pickup.DISALLOWED,"Arrow is stationary or collectible");
            var friendlyHit=new ProjectileImpactEvent(arrow,new EntityHitResult(player));BoneGuardEvents.impact(friendlyHit);test.assertTrue(friendlyHit.isCanceled() && arrow.isRemoved(),"Guard arrow injures owner");
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BONE_GUARD),"Ready recast rejected");guard=player.level().getEntitiesOfClass(BoneGuardEvents.GuardSkeleton.class,player.getBoundingBox().inflate(4),LivingEntity::isAlive).getFirst();
            var box=guard.getBoundingBox().inflate(2);int drops=player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).size();
            guard.hurt(player.damageSources().playerAttack(player),100);test.assertTrue(player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).size()==drops,"Guard drops bow or bones");BoneGuardEvents.cancel(player.getUUID());
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BONE_GUARD),"Cannot summon replacement after death");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertTrue(BoneGuardEvents.active(player),"Switch removes guard");BoneGuardEvents.cancel(player.getUUID());
        }} finally {BoneGuardEvents.cancel(player.getUUID());zombie.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==10,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=40)
    public static void tenMinuteGuardKeepsFightingAfterSwitchAndExpires(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));
        var stack=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);AbilityTestPackets.use(player,0);
        var field=BoneGuardEvents.class.getDeclaredField("GUARDS");field.setAccessible(true);@SuppressWarnings("unchecked") var guards=(java.util.Map<java.util.UUID,Object>)field.get(null);var guard=guards.get(player.getUUID());
        var expiry=guard.getClass().getDeclaredMethod("expires");expiry.setAccessible(true);test.assertTrue((Long)expiry.invoke(guard)==player.level().getGameTime()+12000,"Level I lifetime is not ten minutes");
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));test.assertTrue(BoneGuardEvents.active(player),"Equipped-session cancellation despawns guard");
        var parts=guard.getClass().getRecordComponents();var types=new Class<?>[parts.length];var values=new Object[parts.length];
        for(int i=0;i<parts.length;i++){types[i]=parts[i].getType();var accessor=parts[i].getAccessor();accessor.setAccessible(true);values[i]=parts[i].getName().equals("expires")?player.level().getGameTime()+10:accessor.invoke(guard);}
        var ctor=guard.getClass().getDeclaredConstructor(types);ctor.setAccessible(true);guards.put(player.getUUID(),ctor.newInstance(values));
        test.runAfterDelay(11,()->{try {test.assertFalse(BoneGuardEvents.active(player),"Expired persistent guard remains");test.succeed();}finally {BoneGuardEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}});
    }
    @GameTest(template="riftfang_test")
    public static void tenVariantsUseExactLineupsAndEquipmentAtEveryRank(GameTestHelper test) {
        var player=TestPlayers.survival(test);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);player.setNoGravity(true);
        for(int x=3;x<=14;x++)for(int y=5;y<=9;y++)for(int z=3;z<=14;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var golem=EntityType.IRON_GOLEM.create(test.getLevel());golem.setNoAi(true);golem.setNoGravity(true);golem.setPos(point.add(0,0,5));test.getLevel().addFreshEntity(golem);
        var enemy=EntityType.ZOMBIE.create(test.getLevel());enemy.setNoAi(true);enemy.setNoGravity(true);enemy.setPos(point.add(4,0,4));test.getLevel().addFreshEntity(enemy);
        int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries())if(entry.get() instanceof MorsiumWeapon w && !w.isDagger()) {
            count++;var stack=new ItemStack(entry.get());
            for(int rank=1;rank<=7;rank++) {
                stack=Augmentations.apply(stack,AdditionalAbilities.BONE_GUARD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(BoneGuardEvents.activate(player,stack),"Guard group cannot activate");
                var mobs=player.level().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(5),m->m instanceof BoneGuardEvents.FriendlyGuard);
                assertLineup(test,mobs,rank);
                for(var mob:mobs) {test.assertFalse(mob.canAttack(player) || mob.canAttack(golem),"Guard attacks owner/friendly golem");golem.setTarget(mob);test.assertTrue(golem.getTarget()==null,"Friendly golem targets guard");mob.setTarget(golem);test.assertTrue(mob.getTarget()==null,"Guard targets friendly golem");test.assertFalse(mob.shouldBeSaved(),"Temporary guard saves");}
                if(rank>=4)test.assertTrue(Augmentations.displayRank(stack,AdditionalAbilities.BONE_GUARD,rank)==rank-3 && Augmentations.abilityName(stack,AdditionalAbilities.BONE_GUARD,rank).getString().equals("Necromancer"),"Necromancer name/rank wrong");
                if(rank==7)for(var mob:mobs) {
                    if(mob instanceof BoneGuardEvents.GuardBogged bogged)test.assertFalse(bogged.readyForShearing(),"Temporary bogged can farm mushrooms");
                    if(mob instanceof net.minecraft.world.entity.monster.AbstractSkeleton archer && mob.getMainHandItem().is(Items.BOW)) {
                        archer.performRangedAttack(enemy,1);
                        var arrows=player.level().getEntitiesOfClass(AbstractArrow.class,mob.getBoundingBox().inflate(2),a->a.getOwner()==player && !a.isRemoved());
                        test.assertTrue(arrows.size()==1 && arrows.getFirst().pickup==AbstractArrow.Pickup.DISALLOWED,"Necromancer archer arrow ownership/pickup wrong");
                        var hit=new ProjectileImpactEvent(arrows.getFirst(),new EntityHitResult(golem));BoneGuardEvents.impact(hit);test.assertTrue(hit.isCanceled() && arrows.getFirst().isRemoved(),"Necromancer arrow hurts friendly golem");
                    }
                }
                if(rank>=4) {
                    int drops=player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,player.getBoundingBox().inflate(6)).size();
                    for(var mob:mobs)mob.hurt(player.damageSources().genericKill(),10000);
                    test.assertTrue(mobs.stream().noneMatch(LivingEntity::isAlive) && player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,player.getBoundingBox().inflate(6)).size()==drops,"Armored reinforcements drop farmable equipment");
                }
                BoneGuardEvents.cancel(player.getUUID());
            }
            test.assertTrue(Augmentations.apply(stack,AdditionalAbilities.BONE_GUARD).isEmpty(),"Necromancer exceeds IV");
        }}finally {BoneGuardEvents.cancel(player.getUUID());golem.discard();enemy.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==10,"Skipped Bone Guard variants");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void hostileTargetGoalsAcquireGuardsWithoutBeingHitFirst(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);
        for(int x=3;x<=15;x++)for(int y=5;y<=9;y++)for(int z=3;z<=15;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var stack=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var enemies=new java.util.ArrayList<Mob>();
        try {
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Guard did not spawn");
            for(var type:java.util.List.of(EntityType.ZOMBIE,EntityType.SKELETON,EntityType.SPIDER)) {
                var mob=type.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(point.add(5,0,0));test.getLevel().addFreshEntity(mob);enemies.add(mob);
                mob.getRandom().setSeed(73);
                for(int tick=0;tick<200 && !(mob.getTarget() instanceof BoneGuardEvents.FriendlyGuard);tick++)mob.targetSelector.tick();
                test.assertTrue(mob.getTarget() instanceof BoneGuardEvents.FriendlyGuard,"Hostile AI ignores nearby reinforcement: "+type);
            }
        } finally {BoneGuardEvents.cancel(player.getUUID());enemies.forEach(Entity::discard);player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void allReinforcementsDrawRetaliationWhileArrowsKeepPlayerCredit(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);
        for(int x=3;x<=15;x++)for(int y=5;y<=9;y++)for(int z=3;z<=15;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var stack=Augmentations.setLevel(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD,7);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        mixed(player,stack,false);
        var enemy=EntityType.SKELETON.create(test.getLevel());enemy.setNoAi(true);enemy.setNoGravity(true);enemy.setPos(point.add(5,0,0));enemy.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);enemy.setHealth(1000);test.getLevel().addFreshEntity(enemy);
        try {
            test.assertTrue(BoneGuardEvents.activate(player,stack),"Necromancer did not spawn");
            var guards=player.level().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(5),m->m instanceof BoneGuardEvents.FriendlyGuard);
            test.assertTrue(guards.size()==5,"Missing reinforcement types");
            for(var guard:guards) {
                enemy.setTarget(player);enemy.invulnerableTime=0;
                if(!guard.getMainHandItem().is(Items.BOW))guard.doHurtTarget(enemy);
                else {
                    ((net.minecraft.world.entity.monster.AbstractSkeleton)guard).performRangedAttack(enemy,1);
                    var arrows=player.level().getEntitiesOfClass(AbstractArrow.class,guard.getBoundingBox().inflate(2),a->a.getOwner()==player && !a.isRemoved());
                    test.assertTrue(arrows.size()==1,"Reinforcement arrow not found");var arrow=arrows.getFirst();
                    try {enemy.hurt(player.damageSources().arrow(arrow,player),2);test.assertTrue(arrow.getOwner()==player,"Arrow loses player kill credit");}finally {arrow.discard();}
                }
                test.assertTrue(enemy.getTarget()==guard,"Damage does not immediately target its actual reinforcement attacker: "+guard.getType());
                BoneGuardEvents.retaliationTick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(enemy));
                test.assertTrue(enemy.getTarget()==guard && enemy.getLastHurtByMob()==guard,"Vanilla damage bookkeeping still points revenge at the player: "+guard.getType());
                enemy.targetSelector.tick();
                test.assertTrue(enemy.getTarget() instanceof BoneGuardEvents.FriendlyGuard,"Enemy AI abandons reinforcement combat: "+guard.getType()+" target="+enemy.getTarget());
            }
            enemy.invulnerableTime=0;enemy.hurt(player.damageSources().playerAttack(player),1);
            BoneGuardEvents.retaliationTick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(enemy));enemy.setTarget(player);
            test.assertTrue(enemy.getTarget()==player && enemy.getLastHurtByMob()==player,"A real player hit cannot reclaim retaliation");
        } finally {BoneGuardEvents.cancel(player.getUUID());enemy.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }

    @GameTest(template="riftfang_test",timeoutTicks=160)
    public static void necromancerIVGroupUsesRealBowAndMeleeAIAtOneZombie(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.setNoGravity(true);
        var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);
        for(int x=3;x<=20;x++)for(int z=3;z<=20;z++) {
            test.setBlock(new BlockPos(x,4,z),Blocks.STONE);
            for(int y=5;y<=10;y++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        }
        var stack=Augmentations.setLevel(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD,7);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        mixed(player,stack,true);
        var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.setPos(point.add(0,0,6));
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100000);zombie.setHealth(100000);
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        zombie.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));test.getLevel().addFreshEntity(zombie);
        var fired=new java.util.HashSet<java.util.UUID>();var flaming=new java.util.HashSet<java.util.UUID>();var struck=new java.util.HashSet<java.util.UUID>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> listener=event->{
            if(event.getEntity() instanceof AbstractArrow arrow && arrow.getOwner()==player && arrow.getPersistentData().hasUUID("forgermod_bone_guard_shooter")) {
                var shooter=arrow.getPersistentData().getUUID("forgermod_bone_guard_shooter");fired.add(shooter);if(arrow.isOnFire())flaming.add(shooter);
            }
        };
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post> meleeListener=event->{
            if(event.getEntity()==zombie && event.getSource().getEntity() instanceof Mob guard && guard instanceof BoneGuardEvents.FriendlyGuard && event.getNewDamage()>0)struck.add(guard.getUUID());
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(meleeListener);
        test.assertTrue(BoneGuardEvents.activate(player,stack),"Necromancer IV group failed to spawn");
        BoneGuardEvents.tick(new PlayerTickEvent.Pre(player));
        var archers=player.level().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(5),mob->mob instanceof BoneGuardEvents.FriendlyGuard && mob.getMainHandItem().is(Items.BOW));
        var melee=player.level().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(5),mob->mob instanceof BoneGuardEvents.FriendlyGuard && !mob.getMainHandItem().is(Items.BOW));
        test.runAfterDelay(120,()->{
            try {
                test.assertTrue(archers.size()==4 && archers.stream().allMatch(mob->fired.contains(mob.getUUID())),"Not all skeleton, bogged, stray and wither-skeleton bow AIs fired at the zombie: "+fired.size());
                test.assertTrue(archers.stream().filter(mob->mob.getType()==EntityType.WITHER_SKELETON).allMatch(mob->flaming.contains(mob.getUUID())),"Flame bows did not shoot burning arrows");
                test.assertTrue(melee.size()==1 && melee.stream().allMatch(mob->struck.contains(mob.getUUID())),"Iron/diamond sword reinforcements did not use real melee AI: "+struck.size());
                test.succeed();
            } finally {
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(meleeListener);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);BoneGuardEvents.cancel(player.getUUID());zombie.discard();player.server.getPlayerList().remove(player);
            }
        });
    }

}
