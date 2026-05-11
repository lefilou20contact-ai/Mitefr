package fr.mitefr.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Empêche la régénération naturelle issue de la saturation alimentaire.
 * La méthode heal() dans LivingEntity est appelée par HungerManager
 * quand la saturation est maximale.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * On annule heal() si l'entité est un joueur et que la
     * régénération provient uniquement de la nourriture.
     * Pour distinguer : on garde les heals intentionnels (potions, etc.)
     * via un flag thread-local positionné dans HungerManager.
     */
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void onHeal(float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof PlayerEntity)) return;

        // Annuler uniquement si c'est la regen passive de faim
        // (identifiée par le flag dans HungerManagerMixin)
        if (HungerManagerMixin.isFoodRegenActive()) {
            ci.cancel();
        }
    }
}
