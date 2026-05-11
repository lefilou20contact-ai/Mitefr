package fr.mitefr.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Synchronise périodiquement les stats MITE-FR de chaque joueur
 * vers son client (toutes les 2 secondes = 40 ticks).
 */
public final class MiteFRSyncTicker {

    private static final int SYNC_INTERVAL = 40;

    private MiteFRSyncTicker() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(MiteFRSyncTicker::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        if (server.getTicks() % SYNC_INTERVAL != 0) return;

        for (var player : server.getPlayerManager().getPlayerList()) {
            MiteFRNetwork.syncStats(player);
            // Sync maladies moins souvent (toutes les 4 secondes)
            if (server.getTicks() % (SYNC_INTERVAL * 2) == 0) {
                MiteFRNetwork.syncDiseases(player);
            }
        }
    }
}
