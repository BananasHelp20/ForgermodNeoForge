package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.world.entity.EntityType;
import java.util.List;

/** Normal non-boss undead; a skeleton horse entry includes its skeleton rider. */
public final class NecromancerCatalog {
    public record Choice(EntityType<?> type,int cost){}
    public static final int MAX_ENTRIES=10,EQUIPMENT_SLOTS=6;
    public static final List<Choice> CHOICES=List.of(
            new Choice(EntityType.SKELETON,1),new Choice(EntityType.ZOMBIE,1),new Choice(EntityType.ZOMBIE_VILLAGER,1),
            new Choice(EntityType.HUSK,2),new Choice(EntityType.DROWNED,2),new Choice(EntityType.STRAY,2),new Choice(EntityType.BOGGED,2),
            new Choice(EntityType.ZOMBIFIED_PIGLIN,2),new Choice(EntityType.PHANTOM,2),new Choice(EntityType.WITHER_SKELETON,3),
            new Choice(EntityType.SKELETON_HORSE,3),new Choice(EntityType.ZOGLIN,4));
    private NecromancerCatalog(){}
    public static boolean valid(int choice){return choice>=0 && choice<CHOICES.size();}
    public static int capacity(int storedRank){return storedRank<4?0:4+2*(Math.clamp(storedRank,4,7)-4);}
}
