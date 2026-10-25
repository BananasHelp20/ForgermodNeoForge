package net.bananashelp20.forgermod.augmentation;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Describes only the supplied rank, never a future upgrade or super form. */
public final class AbilityDescriptions {
    private AbilityDescriptions() {}

    private static final java.util.Map<String,String> ENGLISH = loadEnglish();
    private static java.util.Map<String,String> loadEnglish() {
        try (var input=AbilityDescriptions.class.getResourceAsStream("/assets/forgermod/lang/en_us.json")) {
            if(input==null) throw new IllegalStateException("Missing language resource");
            return new com.google.gson.Gson().fromJson(new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8),
                    new com.google.gson.reflect.TypeToken<java.util.Map<String,String>>(){}.getType());
        } catch(java.io.IOException exception) { throw new IllegalStateException(exception); }
    }
    public static Component white(Object value) {
        return (value instanceof Component component?component.copy():Component.literal(String.valueOf(value))).withStyle(ChatFormatting.WHITE);
    }
    public static Component text(String key,String fallback,Object... values) {
        return Component.translatableWithFallback(key,fallback,java.util.Arrays.stream(values).map(AbilityDescriptions::white).toArray()).withStyle(ChatFormatting.GRAY);
    }
    public static Component description(ItemStack stack,String id,int rank,boolean compact) {
        if(compact && AdditionalAbilities.THUNDERCLAP.equals(id))return text(id+".card","Dash and slash through nearby mobs; each hit adds half weapon damage as lightning.");
        Component reviewed=reviewed(stack,id,Math.max(1,rank));
        if(reviewed!=null) return reviewed;
        Component current=currentDescription(stack,id,rank,compact);
        if(current.getContents() instanceof TranslatableContents translated) {
            String key=translated.getKey();
            String english=ENGLISH.get(key);
            if(translated.getArgs().length>0)
                return text(key,english==null?key:english,translated.getArgs());
            if(english!=null) {
                var resolved=RankedDescription.resolve(english,Math.max(1,rank));
                return text(key+".highlighted",resolved.format(),resolved.values().toArray());
            }
        }
        return current.copy().withStyle(ChatFormatting.GRAY);
    }
    private static Component reviewed(ItemStack stack,String id,int rank) {
        int jade=JadeAugments.isJade(stack)?1:0;
        return switch(id) {
            case "tooltips.forgermod.ability.sonic_boom" -> text(id+".reviewed",
                    "Activate this ability to fire a piercing Sonic Boom with a %s for %s.",
                    number(net.bananashelp20.forgermod.item.custom.abilities.SonicBoomBlast.RANGE)+" block range",
                    number(net.bananashelp20.forgermod.item.custom.abilities.SonicBoomBlast.damageForAttack(weaponDamage(stack)))+" damage");
            case AdditionalAbilities.SKULL_THROW -> {
                var radius=white(number(net.bananashelp20.forgermod.item.custom.abilities.SkullThrowEvents.burstRadius(rank))+" blocks");
                var effect=Component.literal("Wither "+net.bananashelp20.forgermod.item.custom.WeaponTooltips.romanNumeral(1+jade))
                        .withStyle(jade>0?ChatFormatting.GREEN:ChatFormatting.GRAY);
                var duration=Component.literal((net.bananashelp20.forgermod.item.custom.abilities.SkullThrowEvents.witherTicks(rank)/20+jade)+" seconds")
                        .withStyle(jade>0?ChatFormatting.GREEN:ChatFormatting.WHITE);
                yield Component.translatableWithFallback(id+".reviewed",
                        "Fire a fast skull projectile that bursts within %s and applies %s for %s.",radius,effect,duration)
                        .withStyle(ChatFormatting.GRAY);
            }
            case "tooltips.forgermod.ability.rooting_roots" -> {
                var range=white(net.bananashelp20.forgermod.item.custom.abilities.RootingRootsState.RANGE+" blocks");
                var duration=Component.literal((net.bananashelp20.forgermod.item.custom.abilities.RootingRootsState.durationTicks(jade>0)/20)+" seconds")
                        .withStyle(jade>0?ChatFormatting.GREEN:ChatFormatting.WHITE);
                yield Component.translatableWithFallback(id+".reviewed",
                        "Activate this ability for roots shooting out of the ground that immobilize and poison all enemies within %s for %s.",range,duration)
                        .withStyle(ChatFormatting.GRAY);
            }
            case AdditionalAbilities.RINGING_STEEL -> text(id+".reviewed","Your next hit will damage the enemy's held weapon(s) for %s.",
                    net.bananashelp20.forgermod.item.custom.abilities.RingingSteelEvents.durabilityDamage(rank)+" durability");
            case "tooltips.forgermod.ability.poisoned_vein" -> {
                var effect=Component.literal("Poison "+net.bananashelp20.forgermod.item.custom.WeaponTooltips.romanNumeral(1+jade))
                        .withStyle(jade>0?ChatFormatting.GREEN:ChatFormatting.GRAY);
                var duration=Component.literal((30+jade)+" seconds").withStyle(jade>0?ChatFormatting.GREEN:ChatFormatting.WHITE);
                yield Component.translatableWithFallback(id+".reviewed",
                        "Your next dagger kill leaves a 5-second cloud. Everyone besides you within 3 blocks receives %s for %s.",effect,duration)
                        .withStyle(ChatFormatting.GRAY);
            }
            case RecommendedAbilities.NULL_RECEIPT -> text(id+".reviewed","Absorb knockback for the next %s, then release it with the next vertical strike.",
                    net.bananashelp20.forgermod.item.custom.abilities.NullReceiptEvents.HIT_COUNT+" incoming hits");
            case AdditionalAbilities.THUNDERCLAP -> text(id+".current","Aim at a mob within %s and dash to it with a lightning slash. Deal a full melee hit plus %s lightning damage. Chain to the nearest unhit mob within %s of each previous target until none remain.",
                    number(net.bananashelp20.forgermod.item.custom.abilities.ThunderclapEvents.AIM_RANGE)+" blocks","half weapon",number(net.bananashelp20.forgermod.item.custom.abilities.ThunderclapEvents.CHAIN_RANGE)+" blocks");
            case AdditionalAbilities.BONE_GUARD -> rank<4
                    ?text(id+".reviewed","Activate this ability to summon %s that will fight for you in battle. Reinforcements disappear after 10 minutes",net.bananashelp20.forgermod.item.custom.abilities.NecromancerLoadouts.roster(stack,rank))
                    :text(id+".super.reviewed","Summon %s that fight for you. Customize reinforcements using %s.",net.bananashelp20.forgermod.item.custom.abilities.NecromancerLoadouts.roster(stack,rank),Component.keybind("key.forgermod.necromancer_customize"));
            case RecommendedAbilities.EXPLOSION -> rank<5?null:text(id+".super.reviewed","Create the same explosion and Resistance as Explosion, and fire 4 ghast fireballs in the cardinal directions relative to your facing and 8 blaze fireballs between them. Projectiles deal %s of their normal damage. Terrain damage can be toggled in the mod settings and is disabled by default.",number(100*ReviewedRanks.blastProjectileMultiplier(rank))+"%");
            case AdditionalAbilities.DIRECT_CURRENT -> rank<4
                    ?text(id+".reviewed","Mark up to %s by aiming at them and pressing the ability activation key. By marking them, they get connected one by one, forming a constant current that connects them. Enemies entering/passing the current are being dealt %s every 0.5s.",Math.min(4,rank+1)+" mobs","4 damage")
                    :text(id+".super.reviewed","Mark up to %s as corners. Two marks form a line; three or four close the circuit and fill the enclosed shape. The field deals %s every second and stuns for %s per pulse. It lasts %s from the first connection.","4 mobs",number(1+.25*Math.max(0,rank-4))+" damage","0.5 seconds","20 seconds");
            case RecommendedAbilities.CRUCIBLE_HOOK -> text(id+(rank>=5?".super.reviewed":".reviewed"),
                    "Activate the ability while aiming at a target mob. You will pull the mob to you and automatically hit with a smash attack dealing %s. The smash will also set the enemy ablaze. Burn duration: %s."
                            +(rank>=5?" Aim at a block within %s to grapple. Activate again to move the hook to another reachable block. Right-click or switch weapons to release, preserving momentum. Block-grappling cooldown: %s.":""),
                    rank>=5?new Object[]{number(weaponDamage(stack)*(1+.25*rank))+" damage",(2+rank)+" seconds",ReviewedRanks.grappleRange(rank)+" blocks",net.bananashelp20.forgermod.item.custom.abilities.CrucibleGrapplerEvents.cooldownTicks(rank)/20+" seconds"}
                            :new Object[]{number(weaponDamage(stack)*(1+.25*rank))+" damage",(2+rank)+" seconds"});
            case AdditionalAbilities.CHALLENGE_ROAR -> text(id+(rank>=5?".super.reviewed":".reviewed"),
                    "Agro all hostile mobs within %s on you, and giving you %s for %s. Agroed enemis will not target any other entitiy during that time."
                            +(rank>=5?" Additionally, all enemies within the range gain Weakness I and Slowness I while your strength effect is active.":""),
                    ReviewedRanks.roarRange(rank)+" blocks","strength "+net.bananashelp20.forgermod.item.custom.WeaponTooltips.romanNumeral(1+jade),
                    (ReviewedRanks.roarTicks(rank)/20+jade)+" seconds");
            case Augmentations.AREA_DISCHARGE -> rank<5
                    ?text(id+".reviewed","By activating this ability, all hostile mobs within 5 blocks are being struck by lightning dealing half your weapon damage to each target.")
                    :text(id+".super.reviewed","Strike all hostile and agroed neutral creatures within %s for twice weapon damage in %s. All targets are marked on activation. Waves move inward toward you, 0.5 seconds apart, skipping empty waves. Each initial strike has a 50% chance to start up to 5 chains, each reaching 10 blocks from the previous target, 0.2 seconds apart, and dealing half weapon damage. Chains may extend beyond the initial wave radius. With Lightning Rod on this weapon or a matching dual-wielded dagger, a final lightning strike hits only you.",
                            number(net.bananashelp20.forgermod.item.custom.abilities.ZeusStrikePlan.waveRadius(rank-4))+" blocks",Math.clamp(rank-4,1,4)+(rank==5?" wave":" waves"));
            case "tooltips.forgermod.ability.eye_of_the_storm" -> text(id+".reviewed","you create a small storm around you with you being the center. This storm affects all enemies within a %s and lasts %s.",
                    number(ReviewedRanks.stormRadius(rank))+" block radius",(ReviewedRanks.stormTicks(rank)/20+jade)+" seconds");
            case AdditionalAbilities.DISPLACE -> rank>=4
                    ?text(id+".reviewed.max","Aim at any %s to swap places with it when activating the ability while aiming at it.","visible entity")
                    :text(id+".reviewed","Aim at any living entity within %s to swap places with it when activating the ability while aiming at it.","10 blocks");
            case AdditionalAbilities.GRAVITY_WELL -> text(id+".reviewed","Aim at an enemy and activate the ability to pull an enemy within %s to you and immediately strike it with a vertical strike.","6 blocks");
            case RecommendedAbilities.ENDER_SWING -> text(id+".reviewed","Swing at all mobs within a %s, lifting them up to %s or the highest clear space below a ceiling. Increase their gravity and fall damage by %s.",
                    number(ReviewedRanks.gravitySwingRadius(rank))+" block radius",ReviewedRanks.gravitySwingHeight(rank)+" blocks","+"+number(ReviewedRanks.gravitySwingBonus(rank)*100)+"%");
            case AdditionalAbilities.GHOSTWALK -> rank<4
                    ?text(id+".reviewed","Become invisible for %s or until striking an enemy.",(10+jade)+" seconds")
                    :text(id+".super.reviewed","makes you fully invisible for %s or until you strike an enemy. Hitting an enemy while being invisible deals %s.",
                            (10+2*Math.max(0,rank-4)+jade)+" seconds",number(2*weaponDamage(stack))+" damage");
            case RecommendedAbilities.FLAMEBORN -> text(id+".reviewed","You gain fire resistance for %s, and explode in a burst of fire dealing %s and setting all entities within %s ablaze for %s.",
                    (30+jade)+" seconds",number(weaponDamage(stack)*net.bananashelp20.forgermod.item.custom.abilities.FlamebornAbility.damageMultiplier(rank))+" damage","10 blocks",(rank>1?7:5)+" seconds");
            case RecommendedAbilities.CINDER_DECOY -> text(id+".reviewed","Creates a decoy that atrackts all hostile mobs for %s. When it disappears or is being dealt the player's health as total damage, the decoy explodes in a fire explosion with a radius of %s, making enemies burn for %s.",
                    "600 seconds",(1+Math.clamp(rank,1,4))+" blocks",(2+rank)+" seconds");
            case "tooltips.forgermod.ability.charge_attack" -> text(id+".reviewed","Progressively gain %s in the next %s. The last hit fully charges a lightning attack dealing %s%s",
                    "+"+number(net.bananashelp20.forgermod.item.custom.abilities.ChargeAttackState.maximumBonus(rank)*100)+"% damage",
                    net.bananashelp20.forgermod.item.custom.abilities.ChargeAttackState.chargingHits(rank)+" hits",
                    number(net.bananashelp20.forgermod.item.custom.abilities.ChargeAttackState.lightningDamage(rank))+" damage",
                    rank>=4 && Augmentations.hasPassive(stack,AdditionalAbilities.LIGHTNING_ROD)?" and also striking you with thunder activating lightning rod.":".");
            case RecommendedAbilities.FALSE_AWAKENING -> text(id+".reviewed","Create a decoy directly beside the enemy you aim at. When that enemy is distracted, you deal %s to that enemy for a duration of %s.",
                    (5+5*rank)+"% more damage",(30+5*(rank-1))+" seconds");
            case RecommendedAbilities.ECHO_HEARING -> text(id+".reviewed","In the next %s after activating this ability, you will be able to hear sounds made from all entities within a %s. After entities make a sound, they glow for %s.",
                    (8+2*Math.clamp(rank,1,4))+" seconds",(24+8*Math.clamp(rank,1,4))+" block radius",
                    number(ReviewedRanks.echoGlowTicks(rank)/20.0+jade)+" seconds");
            case "tooltips.forgermod.ability.lucid_dreaming" -> text(id+".reviewed","Your next kill grants %s and %s for %s.",
                    "Speed "+net.bananashelp20.forgermod.item.custom.WeaponTooltips.romanNumeral(2+jade),"50% less fall damage",(30+jade)+" seconds");
            case AdditionalAbilities.GREEN_FEAST -> text(id+".reviewed","remove 1 wheat from your inventory to heal %s and gain %s.","2 health",(ReviewedRanks.foodPoints(rank)/2)+" hungerbars");
            case AdditionalAbilities.JUST_A_DREAM -> text(id+".reviewed","Immediately teleport to your respawn point across all dimensions. if it's not available, return to the world spawn.");
            case AdditionalAbilities.DREAM_FEAST -> text(id+".reviewed","Instantly gain %s.",(ReviewedRanks.foodPoints(rank)/2)+" hunger bars");
            case RecommendedAbilities.DELUSION -> text(id+".reviewed","Your next hit stops the mob from targeting anything for %s.",(ReviewedRanks.delusionTicks(rank)/20)+" seconds");
            case "tooltips.forgermod.ability.deep_wound" -> text(id+".reviewed","Your next critical hit deals double damage and inflicts Slowness %s for %s.",
                    net.bananashelp20.forgermod.item.custom.WeaponTooltips.romanNumeral(3+jade),(5+jade)+" seconds");
            case RecommendedAbilities.FLAMING_STOMP -> text(id+".reviewed","your next axe attack becomes a vertical 5x5 strike dealing %s and setting enemies ablaze for %s.",number(weaponDamage(stack))+" damage","5 seconds");
            case AdditionalAbilities.LULLABY -> text(id+".reviewed","Aim at a mob within %s to put it to sleep for %s. Any form of damage wakes it.",(8+2*rank)+" blocks",(3+jade)+" seconds");
            case AdditionalAbilities.LIVE_WIRE -> text(id+".reviewed","In the next %s, enemies that hit you take %s in return.","8 seconds","1 hearts damage");
            case AdditionalAbilities.FLAME_BREATH -> text(id+".reviewed","Incinerate the area in front of you with a range of %s, igninting enemies for %s.",(6+rank)+" blocks",(2+rank)+" seconds");
            case AdditionalAbilities.FEATHER_SHELTER -> text(id+".reviewed","Give yourself and allies within %s slow falling for %s.",(2+2*rank)+" blocks",(10+jade)+" seconds");
            case RecommendedAbilities.EXPLOSIVE_HITS -> text(id+".reviewed","Your next 10 melee hits create small blasts that deal an additional %s. The areal blast has a radius of %s around the hit enemy.","30% areal damage",(2+rank)+" blocks");
            case AdditionalAbilities.DOWNDRAFT_BELL -> text(id+".reviewed","Force all flying mobs within a %s to the ground. (phantom\'s worst nightmare)",(2+2*rank)+" block radius");
            case RecommendedAbilities.DEATH_STARE -> rank>=4 ? text(id+".reviewed.max","Teleport beside the mob you aim at when activating the ability.") : text(id+".reviewed","Teleport beside the mob you aim at when activating the ability. The mob has to be within a distance of %s.",((int)net.bananashelp20.forgermod.item.custom.abilities.DeathStareAbility.range(rank))+" blocks");
            case AdditionalAbilities.CROSSWIND -> text(id+".reviewed","Deflect incoming projectiles away from you for 2 minutes. Your own shots are unaffected. Cooldown: %s.",(RecommendedAbilities.cooldown(id,rank)/20)+" seconds");
            case AdditionalAbilities.BLOOD_SACRAFICE -> text(id+".reviewed","Requires more than 4 HP. Sacrifice half of your total HP to deal double weapon damage for %s.",
                    (net.bananashelp20.forgermod.item.custom.abilities.SacrificeCost.durationTicks(rank)/20)+" seconds");
            case AdditionalAbilities.BLOOD_RUSH -> text(id+".current","On activation pays %s to gain %s for %s.",
                    "1 heart","speed "+net.bananashelp20.forgermod.item.custom.WeaponTooltips.romanNumeral(2+jade),(10+jade)+" seconds");
            default -> null;
        };
    }

    public static String number(double value) {
        return String.format(java.util.Locale.ROOT,"%.2f",value).replaceAll("\\.?0+$","");
    }
    public static double weaponDamage(ItemStack stack) {
        var attributes=stack.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        return 1+attributes.modifiers().stream().filter(entry->entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE))
                .mapToDouble(entry->entry.modifier().amount()).sum();
    }


    private static final java.util.Set<String> JADE_KEYS = java.util.Set.of(
            "tooltips.forgermod.ability.absolute_nightmare.current.jade",
            "tooltips.forgermod.ability.always_the_gardeners.current.jade",
            "tooltips.forgermod.ability.lullaby.current.jade",
            "tooltips.forgermod.ability.flameborn.current.jade",
            "tooltips.forgermod.ability.feather_shelter.current.jade",
            "tooltips.forgermod.ability.skull_throw.current.jade",
            "tooltips.forgermod.ability.challenge_roar.current.jade",
            "tooltips.forgermod.passive.lightning_rod.current.jade",
            "tooltips.forgermod.ability.explosion.jade",
            "tooltips.forgermod.ability.lucid_dreaming.jade",
            "tooltips.forgermod.ability.deep_wound.jade",
            "tooltips.forgermod.ability.rooting_roots.jade",
            "tooltips.forgermod.ability.poisoned_vein.jade",
            "tooltips.forgermod.ability.blood_rush.jade",
            "tooltips.forgermod.passive.daydreamer.jade",
            "tooltips.forgermod.passive.homesick.jade",
            "tooltips.forgermod.passive.scar_compass.jade",
            "tooltips.forgermod.ability.always_the_gardeners.current.card.jade",
            "tooltips.forgermod.ability.flameborn.current.card.jade",
            "tooltips.forgermod.ability.absolute_nightmare.current.card.jade",
            "tooltips.forgermod.ability.lullaby.current.card.jade",
            "tooltips.forgermod.passive.lightning_rod.current.card.jade",
            "tooltips.forgermod.ability.feather_shelter.current.card.jade",
            "tooltips.forgermod.ability.skull_throw.current.card.jade",
            "tooltips.forgermod.ability.challenge_roar.current.card.jade"
    );

    private static Component currentDescription(ItemStack stack, String id, int rank, boolean compact) {
        rank = Math.max(1, rank);
        var values = AugmentUpgradeDescriptions.values(stack, id, rank);
        if (id.equals(Augmentations.DOUBLE_JUMP) && rank < 4)
            values.put("air_jumps", Component.translatable("augmentation.forgermod.preview.value.one_air_jump"));
        if (id.equals(AdditionalAbilities.BONE_GUARD) && rank >= 4)
            values.put("equipment", Component.translatable(rank >= 7
                    ? "tooltips.forgermod.ability.bone_guard.elite_equipment"
                    : "tooltips.forgermod.ability.bone_guard.armor"));
        String variant = id;
        if (Augmentations.isSuper(stack,id,rank)
                && !id.equals(Augmentations.DOUBLE_JUMP)) variant += ".super";
        if (Augmentations.isTemplate(stack) && id.equals(AdditionalAbilities.WINDCHARGE)) {
            // A template has no weapon type yet; describe both current possibilities.
            return Component.translatable(rank >= 4 ? "tooltips.forgermod.ability.windcharge.template.super.current"
                            : "tooltips.forgermod.ability.windcharge.template.current",
                    values.get("launch"), values.get("axe_launch"));
        }
        Component result = switch (variant) {
            case "tooltips.forgermod.ability.cinder_decoy" -> Component.translatable("tooltips.forgermod.ability.cinder_decoy.current", values.get("duration"), values.get("radius"), values.get("burn"));
            case "tooltips.forgermod.passive.cold_bellows" -> Component.translatable("tooltips.forgermod.passive.cold_bellows.current", values.get("burn"));
            case "tooltips.forgermod.passive.heating_up" -> Component.translatable("tooltips.forgermod.passive.heating_up.current", values.get("healing_interval"));
            case "tooltips.forgermod.passive.fire_resistance" -> Component.translatable("tooltips.forgermod.passive.fire_resistance.current", values.get("hands"));
            case "tooltips.forgermod.ability.explosive_hits" -> Component.translatable("tooltips.forgermod.ability.explosive_hits.current", values.get("radius"));
            case "tooltips.forgermod.ability.crucible_hook" -> Component.translatable("tooltips.forgermod.ability.crucible_hook.current", values.get("damage"), values.get("burn"));
            case "tooltips.forgermod.ability.strong_arm" -> Component.translatable("tooltips.forgermod.ability.strong_arm.current", values.get("chance"));
            case "tooltips.forgermod.ability.death_stare" -> Component.translatable("tooltips.forgermod.ability.death_stare.current", values.get("range"));
            case "tooltips.forgermod.passive.echoing_speed" -> Component.translatable("tooltips.forgermod.passive.echoing_speed.current", values.get("speed_cap"));
            case "tooltips.forgermod.passive.distance_tax" -> Component.translatable("tooltips.forgermod.passive.distance_tax.current", values.get("distance_damage"));
            case "tooltips.forgermod.ability.false_awakening" -> Component.translatable("tooltips.forgermod.ability.false_awakening.current", values.get("duration"), values.get("bonus_damage"));
            case "tooltips.forgermod.ability.always_the_gardeners" -> Component.translatable("tooltips.forgermod.ability.always_the_gardeners.current", values.get("radius"));
            case "tooltips.forgermod.ability.echo_pin" -> Component.translatable("tooltips.forgermod.ability.echo_pin.current", values.get("range"), values.get("duration"));
            case "tooltips.forgermod.passive.echo_salvage" -> Component.translatable("tooltips.forgermod.passive.echo_salvage.current", values.get("stored_strikes"));
            case "tooltips.forgermod.ability.ender_swing" -> Component.translatable("tooltips.forgermod.ability.ender_swing.current", values.get("damage"));
            case "tooltips.forgermod.ability.flameborn" -> Component.translatable("tooltips.forgermod.ability.flameborn.current", values.get("burn"));
            case "tooltips.forgermod.passive.double_jump" -> Component.translatable("tooltips.forgermod.passive.double_jump.current", values.get("air_jumps"));
            case "tooltips.forgermod.ability.absolute_nightmare" -> Component.translatable("tooltips.forgermod.ability.absolute_nightmare.current", values.get("duration"));
            case "tooltips.forgermod.ability.leech" -> Component.translatable("tooltips.forgermod.ability.leech.current", values.get("healing"));
            case "tooltips.forgermod.ability.flame_breath" -> Component.translatable("tooltips.forgermod.ability.flame_breath.current", values.get("range"), values.get("burn"));
            case "tooltips.forgermod.ability.magma_toss" -> Component.translatable("tooltips.forgermod.ability.magma_toss.current", values.get("radius"), values.get("burn"));
            case "tooltips.forgermod.ability.void_pulse" -> Component.translatable("tooltips.forgermod.ability.void_pulse.current", values.get("radius"));
            case "tooltips.forgermod.ability.lullaby" -> Component.translatable("tooltips.forgermod.ability.lullaby.current", values.get("range"));
            case "tooltips.forgermod.passive.lightning_rod" -> Component.translatable("tooltips.forgermod.passive.lightning_rod.current", values.get("lightning_interval"));
            case "tooltips.forgermod.ability.direct_current" -> Component.translatable("tooltips.forgermod.ability.direct_current.current", values.get("targets"));
            case "tooltips.forgermod.ability.direct_current.super" -> Component.translatable("tooltips.forgermod.ability.direct_current.super.current", values.get("targets"), values.get("field_damage"));
            case "tooltips.forgermod.ability.stunning_presence" -> Component.translatable("tooltips.forgermod.ability.stunning_presence.current", values.get("pulse_interval"));
            case "tooltips.forgermod.ability.downdraft_bell" -> Component.translatable("tooltips.forgermod.ability.downdraft_bell.current", values.get("radius"));
            case "tooltips.forgermod.ability.windcharge" -> Component.translatable("tooltips.forgermod.ability.windcharge.current", values.get("launch"));
            case "tooltips.forgermod.ability.windcharge.super" -> Component.translatable("tooltips.forgermod.ability.windcharge.super.current", values.get("launch"), values.get("smash_damage"));
            case "tooltips.forgermod.ability.vacuum_burst" -> Component.translatable("tooltips.forgermod.ability.vacuum_burst.current", values.get("radius"));
            case "tooltips.forgermod.ability.feather_shelter" -> Component.translatable("tooltips.forgermod.ability.feather_shelter.current", values.get("radius"));
            case "tooltips.forgermod.ability.ghostwalk" -> Component.translatable("tooltips.forgermod.ability.ghostwalk.current", values.get("duration"));
            case "tooltips.forgermod.ability.ghostwalk.super" -> Component.translatable("tooltips.forgermod.ability.ghostwalk.super.current", values.get("duration"));
            case "tooltips.forgermod.ability.bone_guard" -> Component.translatable("tooltips.forgermod.ability.bone_guard.current", values.get("summons"), values.get("ally_range"));
            case "tooltips.forgermod.ability.bone_guard.super" -> Component.translatable("tooltips.forgermod.ability.bone_guard.super.current", values.get("summons"), values.get("ally_range"), values.get("equipment"));
            case "tooltips.forgermod.ability.skull_throw" -> Component.translatable("tooltips.forgermod.ability.skull_throw.current", values.get("radius"));
            case "tooltips.forgermod.ability.challenge_roar" -> Component.translatable("tooltips.forgermod.ability.challenge_roar.current", values.get("range"));
            default -> Component.translatable(compact && Language.getInstance().has(id + ".card") ? id + ".card" : id);
        };
        if (compact && result.getContents() instanceof TranslatableContents translated
                && Language.getInstance().has(translated.getKey() + ".card"))
            result = Component.translatable(translated.getKey() + ".card", translated.getArgs());
        if (JadeAugments.isJade(stack) && result.getContents() instanceof TranslatableContents translated) {
            // These keys describe only Jade's presently reinforced effects, not future upgrades.
            String jadeKey = translated.getKey() + ".jade";
            if (JADE_KEYS.contains(jadeKey)) return Component.translatable(jadeKey, translated.getArgs());
        }
        return result;
    }

    public static boolean canBecomeSuper(ItemStack stack, String id, int rank) {
        return rank > 0 && rank < Augmentations.superStart(id) && Augmentations.hasSuperForm(stack, id);
    }

    /** Call only in expanded tooltips, after the cooldown (or passive description). */
    public static void appendSuperHint(List<Component> tooltip, ItemStack stack, String id, int rank) {
        if (canBecomeSuper(stack, id, rank))
            tooltip.add(Component.translatable("augmentation.forgermod.super_available").withStyle(ChatFormatting.GRAY));
    }
}
