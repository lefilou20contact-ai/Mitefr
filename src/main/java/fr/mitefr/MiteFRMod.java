package fr.mitefr;

import fr.mitefr.block.MiteFRBlocks;
import fr.mitefr.data.MiteFRGameRules;
import fr.mitefr.entity.MiteFREntities;
import fr.mitefr.item.MiteFRItems;
import fr.mitefr.network.MiteFRNetwork;
import fr.mitefr.network.MiteFRSyncTicker;
import fr.mitefr.system.*;
import fr.mitefr.world.MiteFRWorldGen;
import fr.mitefr.world.SeasonSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiteFRMod implements ModInitializer {

    public static final String MOD_ID = "mitefr";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initialisation de MITE-FR — Bonne chance, tu en auras besoin.");

        // Enregistrement du contenu
        MiteFRBlocks.register();
        MiteFRItems.register();
        MiteFREntities.register();
        MiteFRGameRules.register();
        MiteFRWorldGen.register();
        MiteFRNetwork.registerServerPackets();

        // Commandes admin
        MiteFRCommands.register();

        // Synchronisation réseau périodique
        MiteFRSyncTicker.register();

        // Événements du monde
        MiteFREvents.register();

        // Tick serveur — systèmes de survie par joueur
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                TemperatureSystem.tick(player);
                DiseaseSystem.tick(player);
                SleepSystem.tick(player);
                ThirstSystem.tick(player);
                ConditionSystem.tick(player);
            });
            // Tick saisons
            server.getWorlds().forEach(SeasonSystem::tick);
        });

        LOGGER.info("MITE-FR chargé. Le monde est hostile. Vous êtes prévenus.");
    }
}
