package fr.mitefr.mixin;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bloque la régénération naturelle issue de la saturation alimentaire.
 * Utilise un flag thread-local pour signaler à LivingEntityMixin
 * que le heal provient de la nourriture.
 */
@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Unique
    private static final ThreadLocal<Boolean> foodRegenActive =
        ThreadLocal.withInitial(() -> false);

    /**
     * Flag statique pour LivingEntityMixin.
     */
    public static boolean isFoodRegenActive() {
        return Boolean.TRUE.equals(foodRegenActive.get());
    }

    /**
     * Injection dans update() juste avant l'appel à player.heal()
     * pour lever le flag.
     */
    @Inject(
        method = "update",
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V"
        )
    )
    private void beforeFoodHeal(PlayerEntity player, CallbackInfo ci) {
        foodRegenActive.set(true);
    }

    /**
     * Après l'appel à heal(), on baisse le flag.
     */
    @Inject(
        method = "update",
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V",
            shift  = At.Shift.AFTER
        )
    )
    private void afterFoodHeal(PlayerEntity player, CallbackInfo ci) {
        foodRegenActive.set(false);
    }
}
