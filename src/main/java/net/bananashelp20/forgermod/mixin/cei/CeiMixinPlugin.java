package net.bananashelp20.forgermod.mixin.cei;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Older NeoForge 21.1 builds ignore requiredMods on mixin metadata. */
public final class CeiMixinPlugin implements IMixinConfigPlugin {
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return true; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() {
        var loading = FMLLoader.getLoadingModList();
        return loading != null && loading.getModFileById("create_enchantment_industry") != null
                ? List.of("cei.CeiEnchanterBehaviourMixin", "cei.CeiForgerBlockEntityMixin", "cei.CeiForgerInventoryMixin")
                : List.of();
    }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
