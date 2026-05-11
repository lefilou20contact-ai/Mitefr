package fr.mitefr.data;

/**
 * Toutes les maladies possibles dans MITE-FR.
 */
public enum Disease {
    ROUILLE_DES_MINES(
        "rouille_des_mines",
        "Rouille des Mines",
        "Vitesse de minage réduite de 40%.",
        72000  // 3 jours de jeu si non soigné
    ),
    FIEVRE_DES_MARAIS(
        "fievre_des_marais",
        "Fièvre des Marais",
        "Nausées cycliques, fièvre, ralentissement.",
        48000  // 2 jours
    ),
    GANGRENE(
        "gangrene",
        "Gangrène",
        "Dégâts croissants toutes les 30 secondes. Fatale si non soignée.",
        36000  // 1.5 jours — urgence
    ),
    SCORBUT(
        "scorbut",
        "Scorbut",
        "Os fragiles : les chutes de 2+ blocs infligent des dégâts doublés.",
        -1     // Permanent jusqu'à consommation de fruits/légumes
    ),
    INTOXICATION_EAU(
        "intoxication_eau",
        "Intoxication Hydrique",
        "Nausée et empoisonnement court.",
        1200   // 1 minute
    ),
    HYPOTHERMIE(
        "hypothermie",
        "Hypothermie",
        "Ralentissement sévère, condition qui chute rapidement.",
        -1     // Persiste tant que la temp est basse
    ),
    HYPERTHERMIE(
        "hyperthermie",
        "Hyperthermie",
        "Faim doublée, dégâts de chaleur périodiques.",
        -1
    );

    public final String id;
    public final String displayName;
    public final String description;
    /** Durée en ticks avant guérison naturelle. -1 = permanente jusqu'à remède. */
    public final int    naturalDurationTicks;

    Disease(String id, String displayName, String description, int naturalDurationTicks) {
        this.id                   = id;
        this.displayName          = displayName;
        this.description          = description;
        this.naturalDurationTicks = naturalDurationTicks;
    }

    public boolean isPermanent() {
        return naturalDurationTicks == -1;
    }
}
