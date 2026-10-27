package net.bananashelp20.forgermod.item.custom.attacks.axe;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Imported Blockbench linear curves. Rotations deliberately retain multiple full turns. */
public final class AxeAnimationData {
    public enum Clip {
        IDLE("idle",0), WALK("walk",0), SWING("axe_swing",.625), SWING2("axe_swing2",.58333),
        VERTICAL("axe_jump_attack",1.25), GRAVITY_CAST("axe_grtg_axe_slam",1.625);
        public final String name;public final double impact;
        Clip(String name,double impact){this.name=name;this.impact=impact;}
        public int impactTicks(){return (int)Math.ceil(impact*20-1e-5);}
    }
    public record Vec(double x,double y,double z) {
        public static final Vec ZERO=new Vec(0,0,0),ONE=new Vec(1,1,1);
        public Vec lerp(Vec end,double f){return new Vec(x+(end.x-x)*f,y+(end.y-y)*f,z+(end.z-z)*f);}
    }
    public record Pose(Vec position,Vec rotation,Vec scale) {}
    private record Key(double time,Vec value) {}
    private record Animation(double length,boolean loop,Map<String,Map<String,List<Key>>> bones) {}
    private static final Map<Clip,Animation> DATA=load();
    private AxeAnimationData(){}
    private static Map<Clip,Animation> load(){
        try(var input=AxeAnimationData.class.getResourceAsStream("/assets/forgermod/animations/axe_player.json")){
            if(input==null)throw new IllegalStateException("Missing authored axe animations");
            var clips=JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("clips");
            var result=new EnumMap<Clip,Animation>(Clip.class);
            for(var clip:Clip.values()){
                var json=clips.getAsJsonObject(clip.name);var bones=new HashMap<String,Map<String,List<Key>>>();
                for(var bone:json.getAsJsonObject("bones").entrySet()){
                    var channels=new HashMap<String,List<Key>>();
                    for(var frame:bone.getValue().getAsJsonArray()){
                        var key=frame.getAsJsonObject();var v=key.getAsJsonArray("value");
                        channels.computeIfAbsent(key.get("channel").getAsString(),ignored->new ArrayList<>())
                                .add(new Key(key.get("time").getAsDouble(),new Vec(v.get(0).getAsDouble(),v.get(1).getAsDouble(),v.get(2).getAsDouble())));
                    }
                    channels.values().forEach(keys->keys.sort(Comparator.comparingDouble(Key::time)));bones.put(bone.getKey(),channels);
                }
                result.put(clip,new Animation(json.get("length").getAsDouble(),json.get("loop").getAsBoolean(),bones));
            }
            return result;
        }catch(java.io.IOException e){throw new IllegalStateException("Cannot load authored axe curves",e);}
    }
    public static double length(Clip clip){return DATA.get(clip).length;}
    public static Pose sample(Clip clip,String bone,double seconds){
        var data=DATA.get(clip);double time=data.loop && data.length>0?Math.floorMod((long)(seconds*1_000_000),(long)(data.length*1_000_000))/1_000_000.0:Math.clamp(seconds,0,data.length);
        var channels=data.bones.getOrDefault(bone,Map.of());
        return new Pose(value(channels.get("position"),time,Vec.ZERO),value(channels.get("rotation"),time,Vec.ZERO),value(channels.get("scale"),time,Vec.ONE));
    }
    public static boolean has(Clip clip,String bone,String channel){return DATA.get(clip).bones.getOrDefault(bone,Map.of()).containsKey(channel);}
    private static Vec value(List<Key> keys,double time,Vec fallback){
        if(keys==null || keys.isEmpty())return fallback;
        if(time<=keys.getFirst().time)return keys.getFirst().value;
        for(int i=1;i<keys.size();i++)if(time<=keys.get(i).time){var a=keys.get(i-1);var b=keys.get(i);return a.value.lerp(b.value,(time-a.time)/Math.max(1e-9,b.time-a.time));}
        return keys.getLast().value;
    }
}
