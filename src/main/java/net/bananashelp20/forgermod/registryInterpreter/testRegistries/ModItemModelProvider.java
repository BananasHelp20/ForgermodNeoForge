package net.bananashelp20.forgermod.registryInterpreter.testRegistries;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ForgerMod.MOD_ID, existingFileHelper);
    }

    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(), ResourceLocation.parse("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"item/" + item.getId().getPath()));
    }

    private void basicItem(DeferredItem<?> item) {
        basicItem(item.get());
    }

    @Override
    protected void registerModels() {
        //!GENERATE MODELS

        basicItem(ModItems.ITEM1);
        basicItem(ModItems.ITEM2);
        basicItem(ModItems.ITEM3);
        handheldItem(ModItems.SWORD1);
        handheldItem(ModItems.SWORD2);
        handheldItem(ModItems.SWORD3);
        handheldItem(ModItems.SWORD4.get());
        handheldItem(ModItems.SWORD4_RUBY.get());
        handheldItem(ModItems.SWORD4_AMBER.get());
        handheldItem(ModItems.SWORD4_JADE.get());
        handheldItem(ModItems.SWORD4_AMETHYST.get());

    }
}