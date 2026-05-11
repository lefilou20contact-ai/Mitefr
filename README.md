# MITE-FR — Minecraft Is Too Easy (Version Française Renforcée)

Mod Fabric pour Minecraft **1.20.4** — inspiré du mod chinois MITE (Avernite, 2014), réécrit pour les versions modernes avec des mécaniques encore plus poussées.

## Installation

1. Installer [Fabric Loader](https://fabricmc.net/use/) pour MC 1.20.4
2. Télécharger [Fabric API](https://modrinth.com/mod/fabric-api)
3. Placer `mitefr-1.0.0.jar` et `fabric-api-*.jar` dans votre dossier `mods/`
4. Lancer Minecraft

## Compiler depuis les sources

```bash
# Requis : JDK 17+, connexion internet (téléchargement de Minecraft mappings)
./gradlew build
# Le .jar se trouve dans build/libs/
```

## Systèmes implémentés

| Système | Description |
|---------|-------------|
| **Santé réduite** | 2 cœurs au départ, +1 cœur tous les 10 niveaux |
| **Régénération désactivée** | Seuls les remèdes et potions guérissent |
| **Soif** | Eau brute = intoxication, eau filtrée requise |
| **Température** | Hypothermie / hyperthermie selon biome, météo, profondeur |
| **Maladies** | Gangrène, Scorbut, Fièvre des Marais, Rouille des Mines... |
| **Privation de sommeil** | Hallucinations et malus progressifs |
| **Condition physique** | Jauge 0–100, malus en cascade si basse |
| **Ères** | 6 ères de progression (Silex → Néant) |
| **Legs de Fer** | Mort = perte de 5 niveaux + respawn au campement |
| **Saisons** | Cycle de 20 jours, l'hiver stoppe les cultures |
| **Péremption** | Les aliments pourrissent après 3 jours de jeu |
| **IA mobs** | Portée x2, no-despawn, hordes, escalade |
| **3 nouvelles créatures** | Charognard, Ombre Rampante, Spectre de Brume |

## Commandes admin

```
/mitefr status           — Affiche l'état complet du joueur
/mitefr era advance      — Passe à l'ère suivante
/mitefr era set <id>     — Définit l'ère (silex/cuivre/bronze/fer/acier/neant)
/mitefr disease add <id> — Ajoute une maladie
/mitefr disease cure     — Guérit tout
/mitefr season           — Affiche la saison courante
/mitefr reset            — Réinitialise les données du joueur
```

## Règles de jeu (gamerules)

Chaque système peut être désactivé individuellement :
```
/gamerule mitefr_temperature true/false
/gamerule mitefr_thirst true/false
/gamerule mitefr_diseases true/false
/gamerule mitefr_sleep true/false
/gamerule mitefr_condition true/false
/gamerule mitefr_eras true/false
/gamerule mitefr_legs_de_fer true/false
/gamerule mitefr_food_spoilage true/false
```

## Structure du projet

```
src/main/java/fr/mitefr/
├── MiteFRMod.java          — Point d'entrée principal
├── MiteFRClient.java       — Point d'entrée client
├── MiteFRConstants.java    — Constantes globales
├── MiteFREvents.java       — Enregistrement des événements Fabric
├── MiteFRCommands.java     — Commandes /mitefr
├── block/                  — Blocs custom (stations d'artisanat)
├── data/                   — Données : Era, Disease, PlayerMiteData
├── entity/                 — Entités custom (3 nouvelles créatures)
├── item/                   — Items custom (outils, remèdes, équipements)
├── mixin/                  — Mixins Spongepowered (11 mixins)
├── network/                — Synchronisation client-serveur
├── system/                 — Systèmes de survie (8 systèmes)
└── world/                  — Génération du monde et saisons
```

## Licence

MIT — libre d'utilisation, modification et redistribution.
