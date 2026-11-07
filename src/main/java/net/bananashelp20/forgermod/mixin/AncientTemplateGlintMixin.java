package net.bananashelp20.forgermod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bananashelp20.forgermod.client.AncientTemplateGlint;
import net.bananashelp20.forgermod.item.custom.AncientCraftingTemplateItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ItemRenderer.class)
public abstract class AncientTemplateGlintMixin {
    @ModifyVariable(method="render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at=@At("HEAD"),argsOnly=true)
    private MultiBufferSource forgermod$blueTemplateGlint(MultiBufferSource source,ItemStack stack,ItemDisplayContext context,boolean leftHand,PoseStack pose){
        return AncientCraftingTemplateItem.specialization(stack).isEmpty()?source:AncientTemplateGlint.wrap(source);
    }
}
