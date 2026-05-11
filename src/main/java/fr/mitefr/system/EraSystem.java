package fr.mitefr.system;

import fr.mitefr.data.Era;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.PlayerMiteData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Système de progression par ères.
 * Gère le passage d'une ère à l'autre et les débloquages associés.
 */
public final class EraSystem {

    private EraSystem() {}

    public static Era getEra(ServerPlayerEntity player) {
        return ((MitePlayerDataHolder) player).mitefr_getData().getEra();
    }

    public static void advanceEra(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        Era current = data.getEra();
        Era next    = current.next();

        if (next == current) {
            player.sendMessage(
                Text.literal("✦ Vous avez atteint l'ère ultime : " + current.displayName)
                    .formatted(Formatting.GOLD), false);
            return;
        }

        data.setEra(next);

        player.sendMessage(Text.literal(""), false);
        player.sendMessage(
            Text.literal("═══════════════════════════════")
                .formatted(Formatting.GOLD), false);
        player.sendMessage(
            Text.literal("  ✦ Rite de Passage accompli ✦")
                .formatted(Formatting.YELLOW), false);
        player.sendMessage(
            Text.literal("  Vous entrez dans l'" + next.displayName)
                .formatted(Formatting.GOLD), false);
        player.sendMessage(
            Text.literal("═══════════════════════════════")
                .formatted(Formatting.GOLD), false);

        // Bonus de passage
        onEraEntered(player, next);
    }

    private static void onEraEntered(ServerPlayerEntity player, Era era) {
        // Chaque ère débloque de nouvelles recettes via les advancement triggers
        // et peut donner un bonus passif
        switch (era) {
            case CUIVRE -> player.sendMessage(
                Text.literal("Déblocage : Foyer de Forge, outils en cuivre.")
                    .formatted(Formatting.GRAY), false);
            case BRONZE -> player.sendMessage(
                Text.literal("Déblocage : Forge du Bronzier, armures composites.")
                    .formatted(Formatting.GRAY), false);
            case FER -> player.sendMessage(
                Text.literal("Déblocage : Enclume de Maître, Autel des Runes.")
                    .formatted(Formatting.GRAY), false);
            case ACIER -> player.sendMessage(
                Text.literal("Déblocage : Acier trempé, verre soufflé.")
                    .formatted(Formatting.GRAY), false);
            case NEANT -> player.sendMessage(
                Text.literal("Déblocage : Atelier du Néant. La fin approche...")
                    .formatted(Formatting.DARK_PURPLE), false);
            default -> {}
        }
    }

    /**
     * Retourne true si le joueur est au moins à l'ère requise.
     */
    public static boolean hasEra(ServerPlayerEntity player, Era required) {
        return getEra(player).isAtLeast(required);
    }
}
