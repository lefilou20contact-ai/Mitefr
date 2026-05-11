package fr.mitefr;

import fr.mitefr.network.MiteFRNetwork;
import net.fabricmc.api.ClientModInitializer;

public class MiteFRClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MiteFRNetwork.registerClientPackets();
        MiteFRMod.LOGGER.info("MITE-FR client initialisé.");
    }
}
