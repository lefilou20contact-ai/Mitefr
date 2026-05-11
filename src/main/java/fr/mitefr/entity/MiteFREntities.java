package fr.mitefr.entity;

import fr.mitefr.MiteFRMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Enregistrement de toutes les entités custom de MITE-FR.
 */
public final class MiteFREntities {

    public static final EntityType<HillScavengerEntity> HILL_SCAVENGER =
        register("hill_scavenger",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, HillScavengerEntity::new)
                .dimensions(EntityDimensions.fixed(0.7f, 1.8f))
                .build());

    public static final EntityType<CaveShadeEntity> CAVE_SHADE =
        register("cave_shade",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CaveShadeEntity::new)
                .dimensions(EntityDimensions.fixed(0.6f, 1.9f))
                .build());

    public static final EntityType<SwampSpecterEntity> SWAMP_SPECTER =
        register("swamp_specter",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, SwampSpecterEntity::new)
                .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
                .build());

    public static void register() {
        // Spawn rules
        registerSpawnRules();
    }

    private static void registerSpawnRules() {
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
            ctx -> ctx.getBiome().hasPrecipitation(),
            SpawnGroup.MONSTER,
            HILL_SCAVENGER, 60, 2, 4
        );

        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
            ctx -> true, // Toutes les grottes
            SpawnGroup.MONSTER,
            CAVE_SHADE, 20, 1, 2
        );

        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
            ctx -> ctx.getBiome().hasPrecipitation()
                && ctx.getBiome().getTemperature() < 0.8f,
            SpawnGroup.MONSTER,
            SWAMP_SPECTER, 15, 1, 3
        );
    }

    private static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, new Identifier(MiteFRMod.MOD_ID, name), type);
    }
}
