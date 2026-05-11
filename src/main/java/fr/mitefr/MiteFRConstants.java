package fr.mitefr;

import net.minecraft.util.Identifier;

/**
 * Constantes globales de MITE-FR.
 * Toutes les clés de données persistantes, identifiants de paquets réseau,
 * et valeurs de configuration du mod.
 */
public final class MiteFRConstants {

    // ── Santé & Faim ──────────────────────────────────────────────
    /** Santé max de départ (en demi-cœurs) */
    public static final float STARTING_MAX_HEALTH = 4.0f; // 2 cœurs
    /** Faim max de départ */
    public static final int   STARTING_MAX_HUNGER = 4;    // 2 icônes
    /** Santé max absolue (niveau 40+) */
    public static final float ABSOLUTE_MAX_HEALTH  = 12.0f; // 6 cœurs
    /** Faim max absolue */
    public static final int   ABSOLUTE_MAX_HUNGER  = 12;
    /** Niveaux requis pour +1 cœur / +1 faim */
    public static final int   LEVELS_PER_HEART     = 10;

    // ── Température ───────────────────────────────────────────────
    public static final float TEMP_FREEZING   = -20f;
    public static final float TEMP_COLD       = 10f;
    public static final float TEMP_NORMAL_MIN = 18f;
    public static final float TEMP_NORMAL_MAX = 28f;
    public static final float TEMP_HOT        = 38f;
    public static final float TEMP_BURNING    = 50f;

    // ── Condition physique ─────────────────────────────────────────
    public static final int CONDITION_MAX             = 100;
    public static final int CONDITION_TIER_FATIGUED   = 75;
    public static final int CONDITION_TIER_EXHAUSTED  = 50;
    public static final int CONDITION_TIER_CRITICAL   = 25;

    // ── Sommeil ───────────────────────────────────────────────────
    public static final long TICKS_BEFORE_TIRED       = 16_000L;
    public static final long TICKS_BEFORE_EXHAUSTED   = 24_000L;
    public static final long TICKS_BEFORE_DELIRIOUS   = 32_000L;

    // ── Soif ──────────────────────────────────────────────────────
    public static final int THIRST_MAX                = 20;
    public static final int THIRST_TICK_DRAIN         = 1;   // par 80 ticks
    public static final int THIRST_DIRTY_WATER_DAMAGE = 2;   // demi-cœurs

    // ── NBT Keys ──────────────────────────────────────────────────
    public static final String NBT_CONDITION          = "mitefr_condition";
    public static final String NBT_TEMPERATURE        = "mitefr_temperature";
    public static final String NBT_THIRST             = "mitefr_thirst";
    public static final String NBT_SLEEP_DEPRIVATION  = "mitefr_sleep_deprivation";
    public static final String NBT_AWAKE_TICKS        = "mitefr_awake_ticks";
    public static final String NBT_DISEASES           = "mitefr_diseases";
    public static final String NBT_ERA                = "mitefr_era";
    public static final String NBT_CAMP_X             = "mitefr_camp_x";
    public static final String NBT_CAMP_Y             = "mitefr_camp_y";
    public static final String NBT_CAMP_Z             = "mitefr_camp_z";
    public static final String NBT_CAMP_DIM           = "mitefr_camp_dim";
    public static final String NBT_XP_DEBT            = "mitefr_xp_debt";
    public static final String NBT_FOOD_AGE           = "mitefr_food_age";

    // ── Identifiants réseau ───────────────────────────────────────
    public static final Identifier PACKET_SYNC_STATS     = new Identifier(MiteFRMod.MOD_ID, "sync_stats");
    public static final Identifier PACKET_SYNC_DISEASE   = new Identifier(MiteFRMod.MOD_ID, "sync_disease");
    public static final Identifier PACKET_DEATH_SCREEN   = new Identifier(MiteFRMod.MOD_ID, "death_screen");
    public static final Identifier PACKET_CAMP_SET       = new Identifier(MiteFRMod.MOD_ID, "camp_set");

    private MiteFRConstants() {}
}
