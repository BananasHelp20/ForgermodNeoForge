package net.bananashelp20.forgermod.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.bananashelp20.forgermod.client.AxeAnimationClient;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerRenderer.class)
public abstract class AxePlayerRendererMixin {
    @Inject(method="render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"))
    private void forgermod$begin(AbstractClientPlayer player,float yaw,float partial,PoseStack pose,MultiBufferSource buffer,int light,CallbackInfo ci){pose.pushPose();AxeAnimationClient.root(player,partial,pose);}
    @Inject(method="render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("RETURN"))
    private void forgermod$end(AbstractClientPlayer player,float yaw,float partial,PoseStack pose,MultiBufferSource buffer,int light,CallbackInfo ci){pose.popPose();}
}
