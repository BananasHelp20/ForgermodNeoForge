package net.bananashelp20.forgermod.mixin;
import net.bananashelp20.forgermod.client.AxeAnimationClient;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerModel.class)
public abstract class AxePlayerModelMixin {
    @Unique private final java.util.Map<ModelPart,PartPose> forgermod$previous=new java.util.IdentityHashMap<>();
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("HEAD"))
    private void forgermod$restore(LivingEntity entity,float swing,float amount,float age,float yaw,float pitch,CallbackInfo ci){forgermod$previous.forEach(ModelPart::loadPose);forgermod$previous.clear();}
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
    private void forgermod$animate(LivingEntity entity,float swing,float amount,float age,float yaw,float pitch,CallbackInfo ci){
        if(!(entity instanceof Player player) || !AxeAnimationClient.eligible(player))return;
        var model=(PlayerModel<?>)(Object)this;
        for(var part:java.util.List.of(model.head,model.hat,model.body,model.rightArm,model.leftArm,model.rightLeg,model.leftLeg,model.rightSleeve,model.leftSleeve,model.rightPants,model.leftPants,model.jacket))forgermod$previous.put(part,part.storePose());
        AxeAnimationClient.pose(player,model,Math.clamp(age-entity.tickCount,0,1));
    }
}
