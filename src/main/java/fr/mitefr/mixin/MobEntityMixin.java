package fr.mitefr.mixin;

import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin sur MobEntity :
 * - Double la portée de détection des mobs
 * - Empêche le despawn naturel
 */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    /**
     * Empêche le despawn naturel — comme dans MITE original.
     * Tous les mobs restent dans le monde indéfiniment.
     */
    @Inject(method = "canImmediatelyDespawn", at = @At("RETURN"), cancellable = true)
    private void preventDespawn(double distanceSquared, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    /**
     * Double la portée de suivi du joueur.
     */
    @Inject(method = "getFollowRange", at = @At("RETURN"), cancellable = true)
    private void doubleFollowRange(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValue() * 2.0);
    }
}
