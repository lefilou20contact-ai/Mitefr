package fr.mitefr.network;

import fr.mitefr.MiteFRConstants;
import fr.mitefr.data.Disease;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.PlayerMiteData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Gestion des paquets réseau MITE-FR.
 *
 * Paquets serveur → client :
 *  - SYNC_STATS   : condition, soif, température, niveau de privation de sommeil
 *  - SYNC_DISEASE : liste des maladies actives
 *  - DEATH_SCREEN : données pour l'écran de mort custom
 *
 * Paquets client → serveur :
 *  - CAMP_SET     : confirmation de pose du camp (déjà géré en item)
 */
public final class MiteFRNetwork {

    private MiteFRNetwork() {}

    // ── Enregistrement côté serveur ────────────────────────────────

    public static void registerServerPackets() {
        // Aucun paquet entrant nécessaire pour l'instant —
        // toutes les actions sont traitées server-side
    }

    // ── Enregistrement côté client ─────────────────────────────────

    public static void registerClientPackets() {
        // Réception de la sync des stats
        ClientPlayNetworking.registerGlobalReceiver(
            MiteFRConstants.PACKET_SYNC_STATS,
            (client, handler, buf, responseSender) -> {
                int   condition    = buf.readInt();
                int   thirst       = buf.readInt();
                float temperature  = buf.readFloat();
                int   sleepLevel   = buf.readInt();
                int   eraLevel     = buf.readInt();

                client.execute(() -> {
                    if (client.player == null) return;
                    PlayerMiteData data = ((MitePlayerDataHolder) client.player).mitefr_getData();
                    data.setCondition(condition);
                    data.setThirst(thirst);
                    data.setTemperature(temperature);
                    data.setSleepDeprivation(sleepLevel);
                    data.setEra(fr.mitefr.data.Era.fromLevel(eraLevel));
                });
            }
        );

        // Réception de la sync des maladies
        ClientPlayNetworking.registerGlobalReceiver(
            MiteFRConstants.PACKET_SYNC_DISEASE,
            (client, handler, buf, responseSender) -> {
                int count = buf.readInt();
                int[] diseaseOrdinals = new int[count];
                for (int i = 0; i < count; i++) {
                    diseaseOrdinals[i] = buf.readInt();
                }

                client.execute(() -> {
                    if (client.player == null) return;
                    PlayerMiteData data = ((MitePlayerDataHolder) client.player).mitefr_getData();
                    // Clear et recharger
                    for (Disease d : Disease.values()) data.removeDisease(d);
                    for (int ord : diseaseOrdinals) {
                        data.addDisease(Disease.values()[ord]);
                    }
                });
            }
        );
    }

    // ── Envoi de données ──────────────────────────────────────────

    /**
     * Synchronise toutes les stats MITE d'un joueur vers son client.
     * À appeler périodiquement (toutes les 2 secondes) ou après un changement.
     */
    public static void syncStats(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt  (data.getCondition());
        buf.writeInt  (data.getThirst());
        buf.writeFloat(data.getTemperature());
        buf.writeInt  (data.getSleepDeprivation());
        buf.writeInt  (data.getEra().level);

        ServerPlayNetworking.send(player, MiteFRConstants.PACKET_SYNC_STATS, buf);
    }

    /**
     * Synchronise les maladies actives vers le client du joueur.
     */
    public static void syncDiseases(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();

        var diseases = data.getDiseases().stream().toList();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(diseases.size());
        for (Disease d : diseases) {
            buf.writeInt(d.ordinal());
        }

        ServerPlayNetworking.send(player, MiteFRConstants.PACKET_SYNC_DISEASE, buf);
    }
}
