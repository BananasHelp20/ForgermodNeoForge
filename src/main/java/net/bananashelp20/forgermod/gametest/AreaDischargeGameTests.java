package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.*;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public final class AreaDischargeGameTests {
    private static final Map<GameTestHelper,Set<net.minecraft.world.level.ChunkPos>> CHUNKS=new IdentityHashMap<>();
    private static void keepLoaded(GameTestHelper test,Vec3 position){
        var chunk=new net.minecraft.world.level.ChunkPos(net.minecraft.core.BlockPos.containing(position));
        if(test.getLevel().setChunkForced(chunk.x,chunk.z,true))CHUNKS.computeIfAbsent(test,key->new HashSet<>()).add(chunk);
        test.getLevel().getChunk(chunk.x,chunk.z);
    }
    private static ServerPlayer player(GameTestHelper test){var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());return player;}
    private static Zombie mob(GameTestHelper test,Vec3 position){
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(position);
        mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);
        mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));keepLoaded(test,position);test.getLevel().addFreshEntity(mob);return mob;
    }
    private static void cleanup(GameTestHelper test,ServerPlayer player,Iterable<? extends Entity> entities){
        for(var entity:entities)entity.discard();AreaDischargeEvents.cancel(player.getUUID());LightningRodEvents.end(player);
        if(player.getMainHandItem().getItem() instanceof ElectriumWeapon weapon)weapon.cancelAbility(player,WeaponAbilitySlot.SECONDARY);
        WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
        test.getLevel().getEntitiesOfClass(LightningBolt.class,player.getBoundingBox().inflate(150)).forEach(Entity::discard);
        player.server.getPlayerList().remove(player);
        for(var chunk:CHUNKS.getOrDefault(test,Set.of()))test.getLevel().setChunkForced(chunk.x,chunk.z,false);CHUNKS.remove(test);
    }
    private static void checkLater(GameTestHelper test,int delay,ServerPlayer player,Iterable<? extends Entity> entities,Runnable check){
        test.runAfterDelay(delay,()->{try{check.run();}catch(RuntimeException|Error failure){cleanup(test,player,entities);throw failure;}});
    }
    private static void drive(GameTestHelper test,ServerPlayer player,Iterable<? extends Entity> entities,int ticks){
        // Embedded connections do not run the normal ServerPlayer doTick/player-event path.
        for(int tick=1;tick<=ticks;tick++)checkLater(test,tick,player,entities,()->AreaDischargeEvents.tick(player));
    }
    @GameTest(template="riftfang_test",batch="review_zeus_half_boundaries",timeoutTicks=40)
    public static void superRanksIncludeFiveTenFifteenTwentyButNotJustOutside(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position().add(0,30,0);player.setPos(origin);keepLoaded(test,origin);
        var targets=new ArrayList<Zombie>();
        checkLater(test,5,player,targets,()->{
            try{
                for(int rank=1;rank<=4;rank++){
                    double radius=5*rank;var edge=mob(test,origin.add(0,radius,0));var outside=mob(test,origin.add(0,radius+.125,0));targets.add(edge);targets.add(outside);
                    var stack=Augmentations.setLevel(new ItemStack(ModItems.STATIC_DAGGER.get()),Augmentations.AREA_DISCHARGE,rank+4);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
                    player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
                    long seed=0;while(ZeusStrikePlan.startsChain(net.minecraft.util.RandomSource.create(seed).nextFloat()))seed++;player.getRandom().setSeed(seed);
                    AbilityTestPackets.use(player,0);
                    test.assertTrue(edge.getHealth()<1000 && outside.getHealth()==1000,"Wrong native five-block wave boundary at super "+rank);
                    test.assertTrue(!AreaDischargeEvents.active(player),"Single nonempty outer wave did not skip earlier empty bands");
                    edge.discard();outside.discard();
                }
                test.succeed();
            }catch(Exception e){throw new RuntimeException(e);}finally{cleanup(test,player,targets);}
        });
    }
    @GameTest(template="riftfang_test",batch="review_area_progression")
    public static void progressionCooldownsAndNeutralEligibility(GameTestHelper test)throws Exception{
        var player=player(test);var zombie=mob(test,player.position().add(0,0,3));
        var golem=EntityType.IRON_GOLEM.create(test.getLevel());golem.setNoAi(true);golem.setNoGravity(true);golem.setPos(player.position().add(3,0,0));
        golem.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);golem.setHealth(1000);golem.setTarget(zombie);test.getLevel().addFreshEntity(golem);
        var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(player.position().add(-3,0,0));test.getLevel().addFreshEntity(cow);
        try{
            for(var item:List.of(ModItems.STATIC_DAGGER.get(),ModItems.STATIC_DAGGER_RUBY.get(),ModItems.STATIC_DAGGER_AMBER.get(),ModItems.STATIC_DAGGER_AMETHYST.get(),ModItems.STATIC_DAGGER_JADE.get())){
                var stack=new ItemStack(item);
                for(int rank=1;rank<=8;rank++){
                    stack=Augmentations.apply(stack,Augmentations.AREA_DISCHARGE);test.assertFalse(stack.isEmpty(),"Upgrade refused at "+rank);
                    player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
                    zombie.setHealth(1000);golem.setHealth(1000);golem.setTarget(zombie);
                    float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*(rank<5?.5F:2);
                    AbilityTestPackets.use(player,0);
                    test.assertTrue(Math.abs(zombie.getHealth()-(1000-damage))<.01,"Wrong base/super lightning damage at "+rank);
                    test.assertTrue((golem.getHealth()<1000)==(rank>=5) && cow.getHealth()==10,"Wrong neutral eligibility at "+rank);
                    test.assertTrue(zombie.getLastHurtByMob()==player,"Lightning loses player attribution");
                    test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsValue(player.level().getGameTime()+ReviewedRanks.cooldown(Augmentations.AREA_DISCHARGE,rank)),"Packet uses wrong cooldown");
                    test.assertTrue(Augmentations.displayRank(stack,Augmentations.AREA_DISCHARGE,rank)==(rank<5?rank:rank-4)
                            && Augmentations.isSuper(stack,Augmentations.AREA_DISCHARGE,rank)==(rank>=5),"Base IV becomes super too early");
                }
                test.assertTrue(Augmentations.apply(stack,Augmentations.AREA_DISCHARGE).isEmpty(),"Zeus exceeds IV");
                var extracted=Augmentations.extractFirst(stack,new ItemStack(ModItems.AUGMENT_TEMPLATE.get()));
                test.assertTrue(extracted!=null && Augmentations.level(extracted.template(),Augmentations.AREA_DISCHARGE)==8,"Extraction loses Zeus IV");
                test.assertTrue(Augmentations.level(Augmentations.merge(new ItemStack(item),extracted.template()),Augmentations.AREA_DISCHARGE)==8,"Template transfer loses Zeus IV");
            }
        }finally{cleanup(test,player,List.of(zombie,golem,cow));}
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_zeus_waves",timeoutTicks=60)
    public static void emptyWavesSkipMarkedTargetsStillHitAfterMovingAndSwitchCancels(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();var near=mob(test,origin.add(0,0,5));var far=mob(test,origin.add(0,0,12.5));
        var stack=Augmentations.setLevel(new ItemStack(ModItems.STATIC_DAGGER.get()),Augmentations.AREA_DISCHARGE,7);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        AbilityTestPackets.use(player,0);long ready=player.level().getGameTime()+4400;
        drive(test,player,List.of(near,far),23);
        test.assertTrue(far.getHealth()<1000 && near.getHealth()==1000 && AreaDischargeEvents.active(player),"Waves do not start at the outside");
        keepLoaded(test,origin.add(0,0,45));near.setPos(origin.add(0,0,45));
        checkLater(test,9,player,List.of(near,far),()->test.assertTrue(near.getHealth()==1000,"Inner wave fires before500ms"));
        checkLater(test,11,player,List.of(near,far),()->{
            test.assertTrue(near.getHealth()<1000 && !AreaDischargeEvents.active(player),"Empty middle wave not skipped or moved mark lost: health="+far.getHealth()+" removed="+far.isRemoved()+" active="+AreaDischargeEvents.active(player));
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsValue(ready),"Wave completion restarts cooldown");
            near.setHealth(1000);far.setHealth(1000);near.setPos(origin.add(0,0,5));far.setPos(origin.add(0,0,12.5));player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
            try{AbilityTestPackets.use(player,0);}catch(Exception e){throw new RuntimeException(e);}
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
        });
        test.runAfterDelay(23,()->{try{test.assertTrue(near.getHealth()==1000 && !AreaDischargeEvents.active(player),"Switch leaves queued lightning");test.succeed();}finally{cleanup(test,player,List.of(near,far));}});
    }
    @GameTest(template="riftfang_test",batch="review_zeus_chains",timeoutTicks=55)
    public static void chainsExtendBeyondTwentyAndUseMarkedTargetsWithTwoHundredMsDelays(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position().add(0,30,0);player.setPos(origin);keepLoaded(test,origin);List<Zombie> targets=new ArrayList<>();
        // Vertical nine-block hops stay in one entity-accessible chunk regardless of the random test origin.
        for(int i=0;i<7;i++)targets.add(mob(test,origin.add(0,19+9*i,0)));
        var stack=Augmentations.setLevel(new ItemStack(ModItems.STATIC_DAGGER.get()),Augmentations.AREA_DISCHARGE,8);stack=Augmentations.apply(stack,AdditionalAbilities.LIGHTNING_ROD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        float[] capturedDamage={0};
        // Schedule callbacks before the test starts iterating its pending-callback map.
        for(int tick=6;tick<=37;tick++)checkLater(test,tick,player,targets,()->AreaDischargeEvents.tick(player));
        // Newly forced chunks must finish becoming entity-accessible before targets can be selected.
        checkLater(test,5,player,targets,()->{
        player.doTick();
        long seed=0;while(!ZeusStrikePlan.startsChain(net.minecraft.util.RandomSource.create(seed).nextFloat()))seed++;player.getRandom().setSeed(seed);
        float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);capturedDamage[0]=damage;
        try{AbilityTestPackets.use(player,0);}catch(Exception e){throw new RuntimeException(e);}
        test.assertTrue(Math.abs(targets.getFirst().getHealth()-(1000-2*damage))<.01,"Leading empty waves delay the only initial target: health="+targets.getFirst().getHealth()+" expected="+(1000-2*damage)+" player="+player.position()+" origin="+origin+" queried="+player.serverLevel().getEntitiesOfClass(Zombie.class,player.getBoundingBox().inflate(20)).size()+" active="+AreaDischargeEvents.active(player));
        targets.get(1).setPos(origin.add(0,120,0));
        });
        checkLater(test,8,player,targets,()->test.assertTrue(targets.get(1).getHealth()==1000,"First chain fires before200ms"));
        checkLater(test,10,player,targets,()->test.assertTrue(targets.get(1).getHealth()<1000 && targets.get(2).getHealth()==1000,"Wrong per-jump delay or moved mark lost: first="+targets.get(1).getHealth()+" second="+targets.get(2).getHealth()+" removed="+targets.get(1).isRemoved()+" active="+AreaDischargeEvents.active(player)));
        test.runAfterDelay(27,()->{try{
            float damage=capturedDamage[0];
            for(int i=1;i<=5;i++)test.assertTrue(Math.abs(targets.get(i).getHealth()-(1000-.5F*damage))<.01,"Chain fails, repeats or has wrong damage at jump"+i+": health="+targets.get(i).getHealth()+" expected="+(1000-.5F*damage)+" removed="+targets.get(i).isRemoved());
            test.assertTrue(targets.get(6).getHealth()==1000 && AreaDischargeEvents.active(player) && player.getAbsorptionAmount()==0,"Chain exceeds five jumps or self wave fires before the chain ends");
        }catch(RuntimeException|Error failure){cleanup(test,player,targets);throw failure;}});
        test.runAfterDelay(37,()->{try{
            test.assertTrue(!AreaDischargeEvents.active(player) && player.getAbsorptionAmount()==10,"Final player-only Lightning Rod wave missing");
            test.assertTrue(player.getHealth()==20 && targets.get(6).getHealth()==1000,"Self wave hurts caster or nearby mobs");test.succeed();
        }finally{cleanup(test,player,targets);}});
    }
    @GameTest(template="riftfang_test",batch="review_area_charge")
    public static void areaHitsBuildAndDischargeChargeAttackAcrossAllVariants(GameTestHelper test)throws Exception{
        var player=player(test);List<Zombie> targets=new ArrayList<>();
        for(int i=0;i<16;i++){double angle=i*Math.PI/8;targets.add(mob(test,player.position().add(Math.cos(angle)*3,0,Math.sin(angle)*3)));}
        try{
            for(var item:List.of(ModItems.STATIC_DAGGER.get(),ModItems.STATIC_DAGGER_RUBY.get(),ModItems.STATIC_DAGGER_AMBER.get(),ModItems.STATIC_DAGGER_AMETHYST.get(),ModItems.STATIC_DAGGER_JADE.get())){
                var stack=Augmentations.apply(new ItemStack(item),Augmentations.AREA_DISCHARGE);
                stack=Augmentations.setLevel(stack,"tooltips.forgermod.ability.charge_attack",4);stack=Augmentations.apply(stack,AdditionalAbilities.LIGHTNING_ROD);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
                for(var target:targets)target.setHealth(1000);var weapon=(ElectriumWeapon)item;
                test.assertTrue(weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY),"Cannot arm Charge Attack");AbilityTestPackets.use(player,0);
                test.assertTrue(!weapon.isAbilityActive(player,WeaponAbilitySlot.SECONDARY) && player.getAbsorptionAmount()==10,"Sixteen area hits do not discharge/activate Lightning Rod");
                test.assertTrue(targets.stream().anyMatch(target->target.getHealth()<970),"Discharge adds no lightning damage");LightningRodEvents.end(player);
            }
        }finally{cleanup(test,player,targets);}
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_zeus_inward_plan")
    public static void allRanksStrikeOutsideInWithoutRepeatingTargets(GameTestHelper test){
        for(int waves=1;waves<=4;waves++){
            List<Integer> targets=new ArrayList<>();for(int band=1;band<=waves;band++)targets.add(band);
            var plan=ZeusStrikePlan.select(targets,waves,t->true,t->25.0*t*t,t->List.of(),(a,b)->0,Integer::valueOf,()->false);
            for(int i=0;i<waves;i++)test.assertTrue(plan.get(i).target()==waves-i && plan.get(i).delayTicks()==10*i
                    && plan.get(i).damageMultiplier()==2,"Zeus wave order, timing or damage changed at rank "+waves);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_zeus_offhand_wave",timeoutTicks=45)
    public static void offhandRodAddsOneSafeFinalWaveAndSwitchingCancelsIt(GameTestHelper test)throws Exception{
        var player=player(test);var target=mob(test,player.position().add(0,0,3));
        var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(player.position());test.getLevel().addFreshEntity(cow);
        var stack=Augmentations.setLevel(new ItemStack(ModItems.STATIC_DAGGER.get()),Augmentations.AREA_DISCHARGE,5);
        stack=Augmentations.apply(stack,Augmentations.DUAL_WIELD);
        var rod=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER_JADE.get()),AdditionalAbilities.LIGHTNING_ROD);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setItemInHand(InteractionHand.OFF_HAND,rod);
        var entities=List.of(target,cow);AbilityTestPackets.use(player,0);long ready=player.getData(WeaponCooldownAttachments.COOLDOWNS).values().iterator().next();
        drive(test,player,entities,33);
        checkLater(test,9,player,entities,()->test.assertTrue(player.getAbsorptionAmount()==0 && AreaDischargeEvents.active(player),"Self wave fires before final delay"));
        checkLater(test,11,player,entities,()->{
            test.assertTrue(player.getAbsorptionAmount()==10 && !AreaDischargeEvents.active(player),"Matching gemstone offhand rod does not supply final wave");
            test.assertTrue(player.getHealth()==20 && cow.getHealth()==10 && !cow.isOnFire(),"Player-only wave affects nearby cow or caster health");
            var bolts=test.getLevel().getEntitiesOfClass(LightningBolt.class,player.getBoundingBox().inflate(.1));
            test.assertTrue(bolts.size()==1,"Self wave missing or duplicated");bolts.getFirst().tick();
            test.assertTrue(cow.getHealth()==10 && !cow.isOnFire(),"Cosmetic self wave causes vanilla collateral damage");
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsValue(ready),"Self wave restarts cooldown");
            LightningRodEvents.end(player);player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
            try{AbilityTestPackets.use(player,0);}catch(Exception e){throw new RuntimeException(e);}
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        });
        checkLater(test,22,player,entities,()->{
            test.assertTrue(player.getAbsorptionAmount()==0 && !AreaDischargeEvents.active(player),"Removed offhand rod still grants a self wave");
            player.setItemInHand(InteractionHand.OFF_HAND,rod);player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
            try{AbilityTestPackets.use(player,0);}catch(Exception e){throw new RuntimeException(e);}
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        });
        test.runAfterDelay(34,()->{try{
            test.assertTrue(player.getAbsorptionAmount()==0 && !AreaDischargeEvents.active(player),"Switching weapon leaves pending self strike");test.succeed();
        }finally{cleanup(test,player,entities);}});
    }

}
