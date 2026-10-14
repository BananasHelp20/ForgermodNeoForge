package net.bananashelp20.forgermod.item.custom.abilities;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
/** Server-owned air jump budget, granted only by a normal jump and cleared on landing. */
public final class DoubleJumpState {
    private final Map<UUID,Integer> remaining = new HashMap<>();
    public void normalJump(UUID player) { normalJump(player,1); }
    public void normalJump(UUID player,int count) { remaining.put(player,Math.clamp(count,0,4)); }
    public boolean consume(UUID player) {
        int count=remaining.getOrDefault(player,0);if(count<=0)return false;
        if(count==1)remaining.remove(player);else remaining.put(player,count-1);return true;
    }
    public void clear(UUID player) { remaining.remove(player); }
}
