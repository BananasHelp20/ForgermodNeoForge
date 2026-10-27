package net.bananashelp20.forgermod.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.bananashelp20.forgermod.client.AxeAnimationClient;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeAnimationItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandLayer.class)
public abstract class AxeItemInHandLayerMixin extends RenderLayer<LivingEntity,HumanoidModel<LivingEntity>> {
    protected AxeItemInHandLayerMixin(RenderLayerParent<LivingEntity,HumanoidModel<LivingEntity>> parent){super(parent);}
    @Inject(method="renderArmWithItem",at=@At("HEAD"),cancellable=true)
    private void forgermod$axe(LivingEntity entity,ItemStack stack,ItemDisplayContext context,HumanoidArm arm,PoseStack pose,MultiBufferSource buffer,int light,CallbackInfo ci){
        if(!(entity instanceof Player player) || stack!=player.getMainHandItem() || !AxeAnimationItems.isModAxe(stack) || !AxeAnimationClient.eligible(player) || !AxeAnimationClient.authoredPose(player,net.minecraft.client.Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true)))return;
        pose.pushPose();(arm==HumanoidArm.RIGHT?getParentModel().rightArm:getParentModel().leftArm).translateAndRotate(pose);
        AxeAnimationClient.held(player,stack,arm,pose,buffer,light,net.minecraft.client.Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
        pose.popPose();ci.cancel();
    }
}
