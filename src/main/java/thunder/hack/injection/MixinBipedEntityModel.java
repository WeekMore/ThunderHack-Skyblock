package thunder.hack.injection;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.render.Animations;

/**
 * @author yuxiangll
 * @since 2024/8/16 上午11:59
 * IntelliJ IDEA
 */

@Mixin(BipedEntityModel.class)
public abstract class MixinBipedEntityModel<T extends LivingEntity> {
    @Shadow
    protected abstract void positionRightArm(T entity);

    @Shadow protected abstract void positionLeftArm(T entity);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;animateArms(Lnet/minecraft/entity/LivingEntity;F)V", shift = At.Shift.BEFORE), method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V")
    private void swordblocking$setBlockingAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (!ModuleManager.animations.isEnabled() || !Animations.block.getValue())
            return;
        if (!Animations.isWeaponBlocking(livingEntity))
            return;
        if (livingEntity.getOffHandStack().getItem() instanceof ShieldItem)
            this.positionRightArm(livingEntity);
        else
            this.positionLeftArm(livingEntity);
    }






}