package fr.mitefr.system;

import fr.mitefr.MiteFRConstants;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import fr.mitefr.data.PlayerMiteData;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

/**
 * Gère la santé et la faim maximale selon le niveau d'expérience du joueur.
 * +1 cœur et +1 icône de faim tous les LEVELS_PER_HEART niveaux.
 */
public final class HealthSystem {

    private static final UUID HEALTH_MODIFIER_UUID =
        UUID.fromString("a3c8b1d4-7e2f-4a0b-9c6d-1f5e3a2b8c4d");

    private HealthSystem() {}

    /**
     * Recalcule et applique la santé max selon le niveau actuel.
     */
    public static void updateMaxHealth(ServerPlayerEntity player) {
        int level = player.experienceLevel;
        int bonusHearts = Math.min(level / MiteFRConstants.LEVELS_PER_HEART, 4);
        float maxHealth = MiteFRConstants.STARTING_MAX_HEALTH + (bonusHearts * 2f);
        maxHealth = Math.min(maxHealth, MiteFRConstants.ABSOLUTE_MAX_HEALTH);

        EntityAttributeInstance attr =
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr == null) return;

        attr.removeModifier(HEALTH_MODIFIER_UUID);

        double delta = maxHealth - 20.0;
        EntityAttributeModifier mod = new EntityAttributeModifier(
            HEALTH_MODIFIER_UUID,
            "mitefr_max_health",
            delta,
            EntityAttributeModifier.Operation.ADDITION
        );
        attr.addPersistentModifier(mod);

        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    /**
     * Applique la pénalité de dette XP : -1 cœur supplémentaire si xpDebt > 0.
     */
    public static void applyDebtPenalty(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();

        EntityAttributeInstance attr =
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr == null) return;

        int level = player.experienceLevel;
        int bonusHearts = Math.min(level / MiteFRConstants.LEVELS_PER_HEART, 4);
        float maxHealth = MiteFRConstants.STARTING_MAX_HEALTH + (bonusHearts * 2f);
        if (data.getXpDebt() > 0) {
            maxHealth = Math.max(2.0f, maxHealth - 2.0f);
        }

        attr.removeModifier(HEALTH_MODIFIER_UUID);
        double delta = maxHealth - 20.0;
        EntityAttributeModifier mod = new EntityAttributeModifier(
            HEALTH_MODIFIER_UUID,
            "mitefr_max_health",
            delta,
            EntityAttributeModifier.Operation.ADDITION
        );
        attr.addPersistentModifier(mod);

        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    /**
     * Applique la pénalité Legs de Fer à la mort.
     * Perd 5 niveaux (ou ajoute une dette si < 5 niveaux).
     */
    public static void applyLegsDeFeRDeath(ServerPlayerEntity player) {
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_LEGS_DE_FER)) return;
        LegsDeFerSystem.onPlayerDeath(player);
    }

    /**
     * Tick périodique de la dette XP — réduit la dette si le joueur a des niveaux.
     */
    public static void tickXpDebt(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        if (data.getXpDebt() <= 0) return;

        // La dette est remboursée au gain de niveau (via LegsDeFerSystem.onLevelUp)
        // Ici on applique simplement la pénalité de santé si dette active
        applyDebtPenalty(player);
    }

    /**
     * Applique les statistiques initiales à un nouveau joueur.
     */
    public static void initNewPlayer(ServerPlayerEntity player) {
        EntityAttributeInstance attr =
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr != null) {
            attr.removeModifier(HEALTH_MODIFIER_UUID);
            double delta = MiteFRConstants.STARTING_MAX_HEALTH - 20.0;
            attr.addPersistentModifier(new EntityAttributeModifier(
                HEALTH_MODIFIER_UUID, "mitefr_max_health",
                delta, EntityAttributeModifier.Operation.ADDITION
            ));
            player.setHealth(MiteFRConstants.STARTING_MAX_HEALTH);
        }

        player.getHungerManager().setFoodLevel(MiteFRConstants.STARTING_MAX_HUNGER);

        player.sendMessage(
            Text.literal("☠ Bienvenue dans MITE-FR. Vous avez 2 cœurs. Bonne chance.")
                .formatted(Formatting.DARK_RED),
            false
        );
    }
}
