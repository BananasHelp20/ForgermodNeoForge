package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import static net.bananashelp20.forgermod.augmentation.RecommendedAbilities.*;
import static net.bananashelp20.forgermod.augmentation.AdditionalAbilities.*;
/** Short, local vanilla-sample cues for actives that previously had no dedicated sound. */
public final class WeaponAbilitySounds {
    public record Clip(SoundEvent sound,float volume,float pitch){}
    private static final Set<String> EXISTING=Set.of(EXPLOSION,CINDER_DECOY,EXPLOSIVE_HITS,CRUCIBLE_HOOK,SWING_ATTACK,DEATH_STARE,
            FLAMING_STOMP,FLAMEBORN,ENDER_SWING,FLAME_BREATH,VOID_PULSE,GRAVITY_WELL,THUNDERCLAP,WINDCHARGE,POCKET_STORM,SKULL_THROW,CHALLENGE_ROAR,
            Augmentations.AREA_DISCHARGE,"tooltips.forgermod.ability.void_step","tooltips.forgermod.ability.sudden_presence","tooltips.forgermod.ability.sonic_boom");
    private WeaponAbilitySounds(){}
    private static Clip cue(SoundEvent sound,float volume,float pitch){return new Clip(sound,volume,pitch);}
    private static Clip cue(Holder<SoundEvent> sound,float volume,float pitch){return cue(sound.value(),volume,pitch);}
    public static List<Clip> profile(ItemStack stack,String id){
        if(id==null)return List.of();
        if(WINDCHARGE.equals(id)){
            if(stack.getItem() instanceof SwordItemWithEffect weapon && weapon.isAxe() && Augmentations.level(stack,id)>=4)return List.of(); // Skybreaker already has its heavy landing sound.
            return List.of(cue(SoundEvents.WIND_CHARGE_THROW,.7F,1.2F));
        }
        return switch(id){
            case STRONG_ARM -> List.of(cue(SoundEvents.ARMOR_EQUIP_IRON,0.6F,0.7F), cue(SoundEvents.PLAYER_ATTACK_STRONG,0.45F,0.7F));
            case DISTANCE_TAX -> List.of(cue(SoundEvents.AMETHYST_BLOCK_CHIME,.5F,.65F));
            case NULL_RECEIPT -> List.of(cue(SoundEvents.RESPAWN_ANCHOR_CHARGE,0.55F,0.65F));
            case FALSE_AWAKENING -> List.of(cue(SoundEvents.ILLUSIONER_MIRROR_MOVE,0.65F,1.1F));
            case DELUSION -> List.of(cue(SoundEvents.ILLUSIONER_CAST_SPELL,0.55F,0.9F));
            case GARDENERS -> List.of(cue(SoundEvents.BONE_MEAL_USE,0.65F,0.8F), cue(SoundEvents.GRASS_PLACE,0.45F,1.1F));
            case ECHO_HEARING -> List.of(cue(SoundEvents.SCULK_CLICKING,0.4F,0.8F), cue(SoundEvents.WARDEN_LISTENING,0.5F,1.1F));
            case MAGMA_TOSS -> List.of(cue(SoundEvents.BLAZE_SHOOT,0.55F,0.7F), cue(SoundEvents.BASALT_BREAK,0.4F,0.7F));
            case DISPLACE -> List.of(cue(SoundEvents.ENDERMAN_TELEPORT,0.65F,1.2F));
            case JUST_A_DREAM -> List.of(cue(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN,0.5F,1.1F), cue(SoundEvents.ENDERMAN_TELEPORT,0.5F,0.8F));
            case LULLABY -> List.of(cue(SoundEvents.NOTE_BLOCK_CHIME,0.7F,0.5F), cue(SoundEvents.AMETHYST_BLOCK_CHIME,0.4F,0.75F));
            case DREAM_FEAST -> List.of(cue(SoundEvents.PLAYER_BURP,0.4F,1.2F), cue(SoundEvents.AMETHYST_BLOCK_CHIME,0.5F,1.3F));
            case MAGNETIC_THROW -> List.of(cue(SoundEvents.TRIDENT_THROW,0.55F,1.3F), cue(SoundEvents.AMETHYST_CLUSTER_HIT,0.4F,1.5F));
            case LIVE_WIRE -> List.of(cue(SoundEvents.RESPAWN_ANCHOR_CHARGE,0.45F,1.7F), cue(SoundEvents.CONDUIT_ACTIVATE,0.4F,1.3F));
            case STATIC_BLADE -> List.of(cue(SoundEvents.TRIDENT_THROW,0.65F,1.7F), cue(SoundEvents.FIREWORK_ROCKET_BLAST,0.3F,1.7F));
            case ALTERNATING_CURRENT -> List.of(cue(SoundEvents.COPPER_BULB_TURN_ON,0.5F,1.5F), cue(SoundEvents.AMETHYST_BLOCK_RESONATE,0.5F,1.6F));
            case DIRECT_CURRENT -> List.of(cue(SoundEvents.BEACON_POWER_SELECT,0.5F,1.5F), cue(SoundEvents.COPPER_BULB_TURN_ON,0.4F,1.6F));
            case STUNNING_PRESENCE -> List.of(cue(SoundEvents.CONDUIT_ACTIVATE,0.6F,1.3F), cue(SoundEvents.TRIDENT_THUNDER,0.25F,1.8F));
            case DOWNDRAFT_BELL -> List.of(cue(SoundEvents.BREEZE_WIND_CHARGE_BURST,0.75F,0.65F));
            case CROSSWIND -> List.of(cue(SoundEvents.BREEZE_WIND_CHARGE_BURST,0.6F,1.5F));
            case UPWIND -> List.of(cue(SoundEvents.BREEZE_SHOOT,0.65F,1.3F));
            case VACUUM_BURST -> List.of(cue(SoundEvents.BREEZE_WIND_CHARGE_BURST,0.75F,1.25F));
            case FEATHER_SHELTER -> List.of(cue(SoundEvents.PHANTOM_FLAP,0.6F,1.2F));
            case BLOOD_RUSH -> List.of(cue(SoundEvents.PLAYER_ATTACK_STRONG,0.6F,1.15F), cue(SoundEvents.WARDEN_HEARTBEAT,0.55F,1.5F));
            case BLOOD_SACRAFICE -> List.of(cue(SoundEvents.PLAYER_HURT,0.55F,0.6F), cue(SoundEvents.WARDEN_HEARTBEAT,0.6F,0.8F));
            case GREEN_FEAST -> List.of(cue(SoundEvents.PLAYER_BURP,0.4F,1.3F), cue(SoundEvents.BONE_MEAL_USE,0.55F,1.2F));
            case GHOSTWALK -> List.of(cue(SoundEvents.SOUL_ESCAPE,0.65F,0.8F), cue(SoundEvents.ENDERMAN_TELEPORT,0.25F,0.7F));
            case BONE_GUARD -> List.of(cue(SoundEvents.SKELETON_AMBIENT,0.4F,0.8F), cue(SoundEvents.SOUL_ESCAPE,0.6F,1.05F));
            case RINGING_STEEL -> List.of(cue(SoundEvents.ANVIL_HIT,0.6F,1.5F));
            case "tooltips.forgermod.ability.sonic_crit" -> List.of(cue(SoundEvents.SCULK_BLOCK_CHARGE,0.6F,0.9F));
            case "tooltips.forgermod.ability.rooting_roots" -> List.of(cue(SoundEvents.AZALEA_PLACE,0.6F,0.65F), cue(SoundEvents.BONE_MEAL_USE,0.5F,0.8F));
            case "tooltips.forgermod.ability.poisoned_vein" -> List.of(cue(SoundEvents.BREWING_STAND_BREW,0.65F,0.75F), cue(SoundEvents.SLIME_SQUISH_SMALL,0.4F,0.8F));
            case "tooltips.forgermod.ability.strengthened_bones" -> List.of(cue(SoundEvents.BONE_BLOCK_PLACE,0.7F,0.7F), cue(SoundEvents.SKELETON_AMBIENT,0.3F,1.2F));
            case "tooltips.forgermod.ability.storing_anger" -> List.of(cue(SoundEvents.WARDEN_HEARTBEAT,0.65F,0.7F), cue(SoundEvents.BEACON_POWER_SELECT,0.4F,0.65F));
            case "tooltips.forgermod.ability.absolute_nightmare" -> List.of(cue(SoundEvents.ELDER_GUARDIAN_CURSE,0.4F,1.4F));
            case "tooltips.forgermod.ability.lucid_dreaming" -> List.of(cue(SoundEvents.AMETHYST_BLOCK_CHIME,0.65F,1.1F), cue(SoundEvents.ENCHANTMENT_TABLE_USE,0.4F,1.1F));
            case "tooltips.forgermod.ability.eye_of_the_storm" -> List.of(cue(SoundEvents.BREEZE_WIND_CHARGE_BURST,0.6F,0.8F), cue(SoundEvents.CONDUIT_ACTIVATE,0.45F,0.8F));
            case "tooltips.forgermod.ability.windy_dash" -> List.of(cue(SoundEvents.BREEZE_SHOOT,0.65F,1.25F), cue(SoundEvents.PHANTOM_FLAP,0.35F,1.3F));
            case "tooltips.forgermod.ability.deep_wound" -> List.of(cue(SoundEvents.PLAYER_ATTACK_CRIT,0.6F,0.7F), cue(SoundEvents.GRINDSTONE_USE,0.4F,1.4F));
            case "tooltips.forgermod.ability.charge_attack" -> List.of(cue(SoundEvents.COPPER_BULB_TURN_ON,0.55F,1.7F), cue(SoundEvents.RESPAWN_ANCHOR_CHARGE,0.4F,1.7F));
            default -> List.of();
        };
    }
    public static boolean covered(ItemStack stack,String id){return id!=null && (EXISTING.contains(id) || !profile(stack,id).isEmpty());}
    public static void play(ServerPlayer player,ItemStack stack,String id){
        for(var cue:profile(stack,id))player.serverLevel().playSound(null,player.blockPosition(),cue.sound(),SoundSource.PLAYERS,cue.volume(),cue.pitch());
    }
}
