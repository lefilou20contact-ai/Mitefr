package fr.mitefr.mixin;

import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.PlayerMiteData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin client sur GameRenderer :
 * - Ajoute un léger vignetting rouge quand la condition est critique
 * - Augmente le champ de vision inversé (tunnel vision) en délire
 */
@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /**
     * Réduit le FOV dynamiquement quand le joueur est en état de délire.
     * FOV réduit = tunnel vision.
     */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void modifyFov(net.minecraft.client.render.Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        var client = MinecraftClient.getInstance();
        if (client.player == null) return;

        PlayerMiteData data = ((MitePlayerDataHolder) client.player).mitefr_getData();

        int deprivation = data.getSleepDeprivation();
        int condition   = data.getCondition();

        double modifier = 1.0;

        if (deprivation >= 3) {
            modifier *= 0.80;
        } else if (deprivation == 2) {
            modifier *= 0.92;
        }

        if (condition <= 25) {
            modifier *= 0.85;
        }

        if (modifier != 1.0) {
            cir.setReturnValue(cir.getReturnValue() * modifier);
        }
    }
}
