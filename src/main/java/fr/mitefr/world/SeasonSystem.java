package fr.mitefr.world;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;

/**
 * Système de saisons sur un cycle de 20 jours de jeu (480 000 ticks).
 *
 * Printemps : cultures poussent normalement
 * Été       : croissance x2, chaleur passive
 * Automne   : croissance normale, préparation conseillée
 * Hiver     : croissance STOPPÉE, hypothermie nocturne aggravée, ressources rares
 */
public final class SeasonSystem {

    public static final long SEASON_TICKS = 120_000L; // 5 jours de jeu par saison
    public static final long CYCLE_TICKS  = SEASON_TICKS * 4;

    public enum Season {
        PRINTEMPS("Printemps", 0),
        ETE      ("Été",       1),
        AUTOMNE  ("Automne",   2),
        HIVER    ("Hiver",     3);

        public final String displayName;
        public final int    index;

        Season(String displayName, int index) {
            this.displayName = displayName;
            this.index       = index;
        }
    }

    private SeasonSystem() {}

    /**
     * Retourne la saison actuelle selon le temps absolu du monde.
     */
    public static Season getCurrentSeason(ServerWorld world) {
        long time          = world.getTimeOfDay();
        long posInCycle    = time % CYCLE_TICKS;
        int  seasonIndex   = (int) (posInCycle / SEASON_TICKS);
        return Season.values()[seasonIndex];
    }

    /**
     * Retourne le nombre de ticks restants dans la saison courante.
     */
    public static long getTicksRemainingInSeason(ServerWorld world) {
        long time       = world.getTimeOfDay();
        long posInCycle = time % CYCLE_TICKS;
        long posInSeason= posInCycle % SEASON_TICKS;
        return SEASON_TICKS - posInSeason;
    }

    /**
     * Modificateur de vitesse de croissance des cultures selon la saison.
     * 0 = pas de croissance, 1 = normale, 2 = accélérée.
     */
    public static float getCropGrowthMultiplier(ServerWorld world) {
        return switch (getCurrentSeason(world)) {
            case PRINTEMPS -> 1.0f;
            case ETE       -> 2.0f;
            case AUTOMNE   -> 0.7f;
            case HIVER     -> 0.0f;
        };
    }

    /**
     * Modificateur de température ambiante selon la saison.
     */
    public static float getTemperatureModifier(ServerWorld world) {
        return switch (getCurrentSeason(world)) {
            case PRINTEMPS ->  0f;
            case ETE       ->  8f;
            case AUTOMNE   -> -3f;
            case HIVER     -> -15f;
        };
    }

    /**
     * True si c'est l'hiver — utilisé pour aggraver l'hypothermie nocturne.
     */
    public static boolean isWinter(ServerWorld world) {
        return getCurrentSeason(world) == Season.HIVER;
    }

    /**
     * Broadcast la saison à tous les joueurs à chaque changement.
     * À appeler dans un ServerTickEvents.
     */
    public static void tick(ServerWorld world) {
        long time       = world.getTimeOfDay();
        long posInSeason= time % SEASON_TICKS;

        // Exactement au début d'une nouvelle saison
        if (posInSeason == 0) {
            Season season = getCurrentSeason(world);
            String msg = switch (season) {
                case PRINTEMPS -> "🌱 Le printemps arrive. La nature renaît.";
                case ETE       -> "☀ L'été s'installe. Attention à la chaleur.";
                case AUTOMNE   -> "🍂 L'automne commence. Préparez-vous pour l'hiver.";
                case HIVER     -> "❄ L'hiver est là. Survivez si vous le pouvez.";
            };

            for (var player : world.getPlayers()) {
                player.sendMessage(
                    net.minecraft.text.Text.literal(msg)
                        .formatted(net.minecraft.util.Formatting.GOLD),
                    false
                );
            }
        }
    }
}
