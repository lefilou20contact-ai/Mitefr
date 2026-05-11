package fr.mitefr.mixin;

import fr.mitefr.MiteFRConstants;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.PlayerMiteData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin client sur InGameHud :
 * - Affiche la jauge de Soif sous la barre de faim
 * - Affiche la Température et la Condition en texte HUD
 * - Affiche l'ère actuelle en bas à gauche
 */
@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void onRenderStatusBars(DrawContext context, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        PlayerMiteData data = ((MitePlayerDataHolder) client.player).mitefr_getData();
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        // Position de base (sous la barre de faim vanilla)
        int baseX = screenW / 2 + 10;
        int baseY = screenH - 49;

        // ── Jauge de Soif ──────────────────────────────────────
        drawThirstBar(context, data, baseX, baseY + 10);

        // ── Température ────────────────────────────────────────
        drawTemperatureIndicator(context, client, data, baseX - 100, baseY + 10);

        // ── Condition ──────────────────────────────────────────
        drawConditionIndicator(context, client, data, baseX - 100, baseY + 20);

        // ── Ère actuelle ───────────────────────────────────────
        drawEraIndicator(context, client, data, 4, screenH - 14);
    }

    private void drawThirstBar(DrawContext context, PlayerMiteData data, int x, int y) {
        // 10 icônes de soif (comme la faim)
        int thirst = data.getThirst();
        for (int i = 0; i < 10; i++) {
            int iconX = x + (9 - i) * 8;
            // Fond de l'icône
            context.fill(iconX, y, iconX + 7, y + 7, 0x55000000);
            // Remplissage selon la soif
            if (thirst > i * 2 + 1) {
                // Plein
                context.fill(iconX + 1, y + 1, iconX + 6, y + 6, 0xFF3399FF);
            } else if (thirst > i * 2) {
                // Demi
                context.fill(iconX + 1, y + 1, iconX + 3, y + 6, 0xFF3399FF);
            }
        }
    }

    private void drawTemperatureIndicator(DrawContext context, MinecraftClient client,
                                           PlayerMiteData data, int x, int y) {
        float temp = data.getTemperature();
        int color;
        if (temp <= MiteFRConstants.TEMP_COLD)         color = 0xFF55CCFF; // Bleu froid
        else if (temp >= MiteFRConstants.TEMP_HOT)     color = 0xFFFF5533; // Rouge chaud
        else                                            color = 0xFF88CC44; // Vert normal

        String text = String.format("%.0f°C", temp);
        context.drawTextWithShadow(client.textRenderer, text, x, y, color);
    }

    private void drawConditionIndicator(DrawContext context, MinecraftClient client,
                                         PlayerMiteData data, int x, int y) {
        int cond = data.getCondition();
        int color;
        String label;
        if (cond >= 75) { color = 0xFF88CC44; label = "Condition: ✦✦✦✦"; }
        else if (cond >= 50) { color = 0xFFCCCC00; label = "Condition: ✦✦✦○"; }
        else if (cond >= 25) { color = 0xFFFF8800; label = "Condition: ✦✦○○"; }
        else                 { color = 0xFFCC2222; label = "Condition: ✦○○○"; }

        context.drawTextWithShadow(client.textRenderer, label, x, y, color);
    }

    private void drawEraIndicator(DrawContext context, MinecraftClient client,
                                   PlayerMiteData data, int x, int y) {
        String era = "✦ " + data.getEra().displayName;
        context.drawTextWithShadow(client.textRenderer, era, x, y, 0xFFCC9933);
    }
}
