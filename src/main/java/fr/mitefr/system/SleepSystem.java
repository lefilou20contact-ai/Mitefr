package fr.mitefr.system;

import fr.mitefr.MiteFRConstants;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import fr.mitefr.data.PlayerMiteData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Système de privation de sommeil.
 * Chaque tick éveillé incrémente un compteur.
 * Paliers : Fatigué → Épuisé → Délirant
 */
public final class SleepSystem {

    private SleepSystem() {}

    public static void tick(ServerPlayerEntity player) {
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_SLEEP_DEPRIVATION)) return;

        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();

        // Si le joueur dort, on réinitialise
        if (player.isSleeping()) {
            if (data.getAwakeTicks() > 0) {
                data.resetAwakeTicks();
                player.sendMessage(
                    Text.literal("✦ Vous vous sentez reposé.")
                        .formatted(Formatting.GREEN), true);
            }
            return;
        }

        data.incrementAwakeTicks();
        long awake = data.getAwakeTicks();

        int newLevel = computeDeprivationLevel(awake);
        if (newLevel != data.getSleepDeprivation()) {
            data.setSleepDeprivation(newLevel);
            notifyDeprivationChange(player, newLevel);
        }

        applyDeprivationEffects(player, data, newLevel);
    }

    private static int computeDeprivationLevel(long awake) {
        if (awake >= MiteFRConstants.TICKS_BEFORE_DELIRIOUS)  return 3;
        if (awake >= MiteFRConstants.TICKS_BEFORE_EXHAUSTED)  return 2;
        if (awake >= MiteFRConstants.TICKS_BEFORE_TIRED)      return 1;
        return 0;
    }

    private static void applyDeprivationEffects(ServerPlayerEntity player,
                                                 PlayerMiteData data, int level) {
        long tick = player.getWorld().getTime();
        switch (level) {
            case 1 -> { // Fatigué
                if (tick % 100 == 0) data.modifyCondition(-1);
            }
            case 2 -> { // Épuisé
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 60, 0, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, 60, 0, false, false, true));
                if (tick % 60 == 0) data.modifyCondition(-2);
            }
            case 3 -> { // Délirant
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 60, 1, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, 60, 1, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NAUSEA,   60, 0, false, false, true));
                if (tick % 40 == 0) data.modifyCondition(-3);
                // Endormissement spontané
                if (tick % 400 == 0 && Math.random() < 0.3) {
                    player.sendMessage(
                        Text.literal("💤 Vous vous endormez debout...")
                            .formatted(Formatting.GRAY), false);
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.BLINDNESS, 80, 0, false, false, true));
                }
            }
        }
    }

    private static void notifyDeprivationChange(ServerPlayerEntity player, int level) {
        String[] messages = {
            "✦ Vous vous sentez reposé.",
            "😴 Vous êtes fatigué. Dormez bientôt.",
            "😵 Vous êtes épuisé. Vos réflexes sont altérés.",
            "💀 Vous délirez de fatigue. Trouvez un abri immédiatement."
        };
        Formatting[] colors = {
            Formatting.GREEN, Formatting.YELLOW, Formatting.GOLD, Formatting.RED
        };
        player.sendMessage(
            Text.literal(messages[level]).formatted(colors[level]), false);
    }

    /** Appelé quand le joueur finit de dormir dans un lit. */
    public static void onWakeUp(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        data.resetAwakeTicks();
        data.modifyCondition(20);
    }
}
