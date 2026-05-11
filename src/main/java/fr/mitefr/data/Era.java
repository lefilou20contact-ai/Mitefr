package fr.mitefr.data;

/**
 * Les 6 ères de progression de MITE-FR.
 * Chaque joueur possède une ère courante stockée en NBT.
 */
public enum Era {
    SILEX(0,    "Âge du Silex",    "age_silex"),
    CUIVRE(1,   "Âge du Cuivre",   "age_cuivre"),
    BRONZE(2,   "Âge du Bronze",   "age_bronze"),
    FER(3,      "Âge du Fer",      "age_fer"),
    ACIER(4,    "Âge de l'Acier",  "age_acier"),
    NEANT(5,    "Âge du Néant",    "age_neant");

    public final int    level;
    public final String displayName;
    public final String id;

    Era(int level, String displayName, String id) {
        this.level       = level;
        this.displayName = displayName;
        this.id          = id;
    }

    public boolean isAtLeast(Era other) {
        return this.level >= other.level;
    }

    public Era next() {
        Era[] values = values();
        return (this.level + 1 < values.length) ? values[this.level + 1] : this;
    }

    public static Era fromLevel(int level) {
        for (Era e : values()) {
            if (e.level == level) return e;
        }
        return SILEX;
    }
}
