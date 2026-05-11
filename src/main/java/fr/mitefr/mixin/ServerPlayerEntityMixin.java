package fr.mitefr.mixin;

import fr.mitefr.data.Disease;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.system.HealthSystem;
import fr.mitefr.system.SleepSystem;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin sur ServerPlayerEntity :
 *  - Applique x2 dégâts de chute si Scorbut
 *  - Recalcule la santé max lors d'un changement de niveau
 *  - Gère le réveil du lit
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    /**
     * Double les dégâts de chute si le joueur a le Scorbut.
     */
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifyFallDamage(float amount, DamageSource source) {
        if (!source.isOf(net.minecraft.entity.damage.DamageTypes.FALL)) return amount;
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        var data = ((MitePlayerDataHolder) self).mitefr_getData();
        if (data.hasDisease(Disease.SCORBUT)) {
            return amount * 2.0f;
        }
        return amount;
    }

    /**
     * Recalcule la santé max quand le joueur monte de niveau.
     */
    @Inject(method = "addExperience", at = @At("TAIL"))
    private void onAddExperience(int experience, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        HealthSystem.updateMaxHealth(self);
    }

    /**
     * Quand le joueur se réveille d'un lit.
     */
    @Inject(method = "wakeUp", at = @At("TAIL"))
    private void onWakeUp(boolean updateSleepingPlayers, boolean resetSleepTimer, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        SleepSystem.onWakeUp(self);
    }
}
