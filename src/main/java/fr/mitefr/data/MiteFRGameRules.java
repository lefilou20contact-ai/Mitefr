package fr.mitefr.data;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

/**
 * Règles de jeu configurables pour MITE-FR.
 * Accessibles via /gamerule dans le jeu.
 */
public final class MiteFRGameRules {

    public static GameRules.Key<GameRules.BooleanRule> ENABLE_TEMPERATURE;
    public static GameRules.Key<GameRules.BooleanRule> ENABLE_THIRST;
    public static GameRules.Key<GameRules.BooleanRule> ENABLE_DISEASES;
    public static GameRules.Key<GameRules.BooleanRule> ENABLE_SLEEP_DEPRIVATION;
    public static GameRules.Key<GameRules.BooleanRule> ENABLE_CONDITION;
    public static GameRules.Key<GameRules.BooleanRule> ENABLE_ERA_SYSTEM;
    public static GameRules.Key<GameRules.BooleanRule> ENABLE_LEGS_DE_FER;
    public static GameRules.Key<GameRules.BooleanRule> ENABLE_FOOD_SPOILAGE;
    public static GameRules.Key<GameRules.IntRule>     STARTING_HEARTS;
    public static GameRules.Key<GameRules.IntRule>     STARTING_HUNGER;

    public static void register() {
        ENABLE_TEMPERATURE       = GameRuleRegistry.register("mitefr_temperature",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        ENABLE_THIRST            = GameRuleRegistry.register("mitefr_thirst",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        ENABLE_DISEASES          = GameRuleRegistry.register("mitefr_diseases",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        ENABLE_SLEEP_DEPRIVATION = GameRuleRegistry.register("mitefr_sleep",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        ENABLE_CONDITION         = GameRuleRegistry.register("mitefr_condition",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        ENABLE_ERA_SYSTEM        = GameRuleRegistry.register("mitefr_eras",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        ENABLE_LEGS_DE_FER       = GameRuleRegistry.register("mitefr_legs_de_fer",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        ENABLE_FOOD_SPOILAGE     = GameRuleRegistry.register("mitefr_food_spoilage",
            GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        STARTING_HEARTS          = GameRuleRegistry.register("mitefr_starting_hearts",
            GameRules.Category.PLAYER, GameRuleFactory.createIntRule(2, 1, 10));
        STARTING_HUNGER          = GameRuleRegistry.register("mitefr_starting_hunger",
            GameRules.Category.PLAYER, GameRuleFactory.createIntRule(2, 1, 10));
    }
}
