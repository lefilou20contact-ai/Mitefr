package fr.mitefr.system;

import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import fr.mitefr.data.PlayerMiteData;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Système "Legs de Fer" — mort partielle.
 *
 * À la mort :
 *  1. Perte de 5 niveaux d'XP (ou dette si < 5 niveaux)
 *  2. Inventaire dispersé au sol (60 secondes avant disparition)
 *  3. Réapparition au campement établi (ou spawn si aucun)
 *  4. Si dette d'XP > 0 : -1 cœur temporaire supplémentaire
 */
public final class LegsDeFerSystem {

    /** XP perdu à chaque mort */
    private static final int XP_LOSS_ON_DEATH = 5;

    /** Durée de vie des items au sol après mort (ticks) */
    public static final int ITEM_DESPAWN_TICKS = 1200; // 60 secondes

    private LegsDeFerSystem() {}

    /**
     * Appelé depuis le mixin de mort du joueur.
     * Gère la logique complète du système Legs de Fer.
     */
    public static void onPlayerDeath(ServerPlayerEntity player) {
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_LEGS_DE_FER)) return;

        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();

        // 1. Perte d'XP
        int currentLevel = player.experienceLevel;
        if (currentLevel >= XP_LOSS_ON_DEATH) {
            player.addExperienceLevels(-XP_LOSS_ON_DEATH);
        } else {
            // Dette d'XP
            int debt = XP_LOSS_ON_DEATH - currentLevel;
            data.addXpDebt(debt);
            player.addExperienceLevels(-currentLevel);
        }

        // 2. Appliquer la pénalité de dette si nécessaire
        if (data.getXpDebt() > 0) {
            HealthSystem.applyDebtPenalty(player);
        }

        // Notifier
        player.sendMessage(
            Text.literal("☠ MORT — Perte de " + XP_LOSS_ON_DEATH + " niveaux. "
                + (data.getXpDebt() > 0 ? "Dette d'XP : " + data.getXpDebt() + " niveaux." : ""))
                .formatted(Formatting.DARK_RED),
            false
        );
    }

    /**
     * Définit le point de réapparition selon le campement.
     * Retourne le BlockPos du camp, ou null pour le spawn d'origine.
     */
    public static BlockPos getRespawnPos(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        if (data.hasCamp()) {
            return data.getCampPos();
        }
        return null; // Spawn original
    }

    /**
     * Établit un campement à la position actuelle du joueur.
     * Nécessite l'item Feu de Camp Artisanal dans l'inventaire.
     */
    public static boolean establishCamp(ServerPlayerEntity player) {
        // Vérifier l'item requis (Feu de Camp vanilla pour l'instant)
        if (!playerHasCampItem(player)) {
            player.sendMessage(
                Text.literal("Vous avez besoin d'un Feu de Camp pour établir un campement.")
                    .formatted(Formatting.RED),
                false
            );
            return false;
        }

        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        BlockPos pos     = player.getBlockPos();
        String dimKey    = player.getWorld().getRegistryKey().getValue().toString();

        data.setCamp(pos, dimKey);

        player.sendMessage(
            Text.literal("⛺ Campement établi ici. Vous réapparaîtrez à cet endroit.")
                .formatted(Formatting.GREEN),
            false
        );
        return true;
    }

    /**
     * Rembourse la dette d'XP lorsque le joueur gagne des niveaux.
     */
    public static void onLevelUp(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        if (data.getXpDebt() <= 0) return;

        data.addXpDebt(-1);
        player.addExperienceLevels(-1); // Annule le gain pour rembourser

        if (data.getXpDebt() == 0) {
            HealthSystem.updateMaxHealth(player);
            player.sendMessage(
                Text.literal("✦ Dette d'XP remboursée. Votre vitalité est restaurée.")
                    .formatted(Formatting.GREEN),
                false
            );
        }
    }

    private static boolean playerHasCampItem(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s.getItem() == net.minecraft.item.Items.CAMPFIRE) return true;
        }
        return false;
    }
}
