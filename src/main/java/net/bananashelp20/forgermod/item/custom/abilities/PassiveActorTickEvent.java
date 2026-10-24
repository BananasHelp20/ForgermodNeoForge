package net.bananashelp20.forgermod.item.custom.abilities;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
/** Shared tick for existing weapon passives; never activates an ability. */
public abstract class PassiveActorTickEvent extends Event {
    private final Entity actor;
    protected PassiveActorTickEvent(Entity actor){this.actor=actor;}
    public Entity getEntity(){return actor;}
    public static final class Pre extends PassiveActorTickEvent {public Pre(Entity actor){super(actor);}}
    public static final class Post extends PassiveActorTickEvent {public Post(Entity actor){super(actor);}}
}
