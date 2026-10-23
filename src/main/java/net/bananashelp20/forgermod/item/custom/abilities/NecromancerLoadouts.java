package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Equipment has one server-side owner. Weapon NBT contains identity/roster preview only, never equipment copies. */
public final class NecromancerLoadouts extends SavedData {
    private static final String ID="forgermod_necromancer_id",PREVIEW="forgermod_necromancer_roster";
    private static final Factory<NecromancerLoadouts> FACTORY=new Factory<>(NecromancerLoadouts::new,NecromancerLoadouts::load);
    private final Map<UUID,Loadout> records=new HashMap<>();
    public static final class Loadout {
        public final UUID id;public final List<Integer> types=new ArrayList<>();
        public final SimpleContainer gear=new SimpleContainer(NecromancerCatalog.MAX_ENTRIES*NecromancerCatalog.EQUIPMENT_SLOTS);
        private Loadout(NecromancerLoadouts storage,UUID id){this.id=id;gear.addListener(ignored->storage.setDirty());}
        public int used(){return types.stream().mapToInt(type->NecromancerCatalog.CHOICES.get(type).cost()).sum();}
        public boolean add(int type,int capacity){if(BoneGuardEvents.busy(id) || !NecromancerCatalog.valid(type) || types.size()>=10 || used()+NecromancerCatalog.CHOICES.get(type).cost()>capacity)return false;types.add(type);gear.setChanged();return true;}
        public boolean replace(int index,int type,int capacity,ServerPlayer player){
            if(BoneGuardEvents.busy(id) || index<0 || index>=types.size() || !NecromancerCatalog.valid(type) || used()-NecromancerCatalog.CHOICES.get(types.get(index)).cost()+NecromancerCatalog.CHOICES.get(type).cost()>capacity)return false;
            if(types.get(index)==type)return true;returnGear(index,player);types.set(index,type);gear.setChanged();return true;
        }
        public void remove(int index,ServerPlayer player){
            if(BoneGuardEvents.busy(id) || index<0 || index>=types.size())return;returnGear(index,player);
            for(int row=index;row<types.size()-1;row++)for(int slot=0;slot<5;slot++)gear.setItem(row*5+slot,gear.removeItemNoUpdate((row+1)*5+slot));
            for(int slot=0;slot<5;slot++)gear.setItem((types.size()-1)*5+slot,ItemStack.EMPTY);types.remove(index);gear.setChanged();
        }
        public void returnGear(int index,ServerPlayer player){
            if(BoneGuardEvents.busy(id) || index<0 || index>=types.size())return;
            for(int slot=0;slot<5;slot++){
                var item=gear.removeItemNoUpdate(index*5+slot);if(item.isEmpty())continue;player.getInventory().add(item);
                if(!item.isEmpty()){var dropped=new ItemEntity(player.level(),player.getX(),player.getY(),player.getZ(),item);dropped.setDefaultPickUpDelay();player.level().addFreshEntity(dropped);}
            }gear.setChanged();
        }
        public void publish(ItemStack weapon){CustomData.update(DataComponents.CUSTOM_DATA,weapon,tag->{tag.putUUID(ID,id);tag.putIntArray(PREVIEW,types.stream().mapToInt(Integer::intValue).toArray());});}
    }
    public static UUID identity(ItemStack weapon){var tag=weapon.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return tag.hasUUID(ID)?tag.getUUID(ID):null;}
    public static Loadout get(ServerPlayer player,ItemStack weapon){
        var storage=player.server.overworld().getDataStorage().computeIfAbsent(FACTORY,"forgermod_necromancer_loadouts");
        UUID id=identity(weapon);if(id==null)id=UUID.randomUUID();final UUID key=id;
        var loadout=storage.records.computeIfAbsent(id,ignored->{var created=new Loadout(storage,key);for(int i=0;i<NecromancerCatalog.capacity(Augmentations.level(weapon,AdditionalAbilities.BONE_GUARD));i++)created.add(0,10);storage.setDirty();return created;});
        loadout.publish(weapon);return loadout;
    }
    public static int[] preview(ItemStack weapon,int rank){
        var tag=weapon.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        return tag.contains(PREVIEW,Tag.TAG_INT_ARRAY)?Arrays.stream(tag.getIntArray(PREVIEW)).filter(NecromancerCatalog::valid).limit(10).toArray():new int[NecromancerCatalog.capacity(rank)];
    }
    public static Component roster(ItemStack weapon,int rank){
        if(rank<4)return rank==1?Component.literal("1 Skeleton"):rank==2?Component.literal("2 Skeletons"):Component.literal("1 Skeleton and 1 Bogged");
        var counts=new LinkedHashMap<Integer,Integer>();for(int type:preview(weapon,rank))counts.merge(type,1,Integer::sum);
        if(counts.isEmpty())return Component.literal("no reinforcements");var result=Component.empty();int entry=0;
        for(var group:counts.entrySet()){
            if(entry>0)result.append(entry==counts.size()-1?" and ":", ");int count=group.getValue();result.append(Component.literal(count+" ")).append(NecromancerCatalog.CHOICES.get(group.getKey()).type().getDescription());if(count!=1)result.append("s");entry++;
        }return result;
    }
    public static NecromancerLoadouts load(CompoundTag tag,HolderLookup.Provider registry){
        var storage=new NecromancerLoadouts();var list=tag.getList("Loadouts",Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++){
            var saved=list.getCompound(i);if(!saved.hasUUID("Id"))continue;var record=new Loadout(storage,saved.getUUID("Id"));
            for(int type:saved.getIntArray("Types"))if(NecromancerCatalog.valid(type) && record.types.size()<10)record.types.add(type);
            var items=saved.getList("Gear",Tag.TAG_COMPOUND);for(int j=0;j<items.size();j++){var item=items.getCompound(j);int slot=item.getInt("Slot");if(slot>=0 && slot<record.types.size()*5)record.gear.setItem(slot,ItemStack.parse(registry,item.getCompound("Item")).orElse(ItemStack.EMPTY));}
            storage.records.put(record.id,record);
        }return storage;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registry){
        var list=new ListTag();for(var record:records.values()){
            var saved=new CompoundTag();saved.putUUID("Id",record.id);saved.putIntArray("Types",record.types.stream().mapToInt(Integer::intValue).toArray());var items=new ListTag();
            for(int i=0;i<record.types.size()*5;i++){var stack=record.gear.getItem(i);if(stack.isEmpty())continue;var item=new CompoundTag();item.putInt("Slot",i);item.put("Item",stack.save(registry));items.add(item);}saved.put("Gear",items);list.add(saved);
        }tag.put("Loadouts",list);return tag;
    }
}
