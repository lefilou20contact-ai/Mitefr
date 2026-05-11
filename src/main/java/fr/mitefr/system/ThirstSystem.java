package fr.mitefr.system;

import fr.mitefr.MiteFRConstants;
import fr.mitefr.data.Disease;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import fr.mitefr.data.PlayerMiteData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Gestion de la soif.
 *
 * La soif diminue toutes les 80 ticks.
 * À 0 : déshydratation, condition chute, éventuellement dégâts.
 * Eau purifiée : restaure +6.
 * Eau brute : restaure +3 mais inflige Intoxication.
 */
public final class ThirstSystem {

    private static final int DRAIN_INTERVAL = 80;

    private ThirstSystem() {}

    public static void tick(ServerPlayerEntity player) {
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_THIRST)) return;

        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        data.setThirstTimer(data.getThirstTimer() + 1);
        if (data.getThirstTimer() < DRAIN_INTERVAL) return;
        data.setThirstTimer(0);

        // Pluie = récupère un peu de soif
        if (player.getWorld().isRaining() && player.getWorld().isSkyVisible(player.getBlockPos())) {
            data.modifyThirst(1);
            return;
        }

        // Drain normal
        data.modifyThirst(-MiteFRConstants.THIRST_TICK_DRAIN);

        int thirst = data.getThirst();
        if (thirst <= 0) {
            // Déshydratation sévère
            player.damage(player.getDamageSources().generic(), 0.5f);
            data.modifyCondition(-3);
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 100, 1, false, false, true));
        } else if (thirst <= 3) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 100, 0, false, false, true));
            player.sendMessage(
                Text.literal("💧 Vous mourez de soif...")
                    .formatted(Formatting.AQUA),
                true
            );
        } else if (thirst <= 7) {
            player.sendMessage(
                Text.literal("💧 Vous avez soif.")
                    .formatted(Formatting.DARK_AQUA),
                true
            );
        }
    }

    /**
     * Appelé lors de la consommation d'eau purifiée (gourde filtrée, eau bouillie...).
     */
    public static void drinkPureWater(ServerPlayerEntity player, int amount) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        data.modifyThirst(amount);
        player.sendMessage(
            Text.literal("💧 Désaltéré.")
                .formatted(Formatting.AQUA),
            true
        );
    }

    /**
     * Appelé lors de la consommation d'eau brute (lac, rivière, seau).
     * Risque d'empoisonnement élevé.
     */
    public static void drinkDirtyWater(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        data.modifyThirst(3);

        if (Math.random() < 0.80) {
            // 80% de chance d'intoxication
            if (!data.hasDisease(Disease.INTOXICATION_EAU)) {
                data.addDisease(Disease.INTOXICATION_EAU);
            }
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.POISON, 600, 0, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NAUSEA, 400, 0, false, false, true));
            player.sendMessage(
                Text.literal("☠ Cette eau était contaminée !")
                    .formatted(Formatting.RED),
                false
            );
        }
    }
}
