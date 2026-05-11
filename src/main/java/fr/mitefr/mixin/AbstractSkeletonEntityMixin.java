package fr.mitefr.mixin;

import net.minecraft.entity.mob.AbstractSkeletonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin sur AbstractSkeletonEntity :
 * - Augmente la vitesse de tir des squelettes
 */
@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonEntityMixin {

    /**
     * Réduit la durée du swing (tir plus fréquent).
     */
    @Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true)
    private void fasterBowSwing(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(1, cir.getReturnValue() - 3));
    }
}
