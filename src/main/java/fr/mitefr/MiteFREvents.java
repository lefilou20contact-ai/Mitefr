package fr.mitefr;

import fr.mitefr.data.Disease;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import fr.mitefr.data.PlayerMiteData;
import fr.mitefr.system.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Enregistrement de tous les événements Fabric de MITE-FR.
 */
public final class MiteFREvents {

    private MiteFREvents() {}

    public static void register() {

        // ── Connexion joueur ──────────────────────────────────────
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            loadOrInitPlayerData(player);
            HealthSystem.updateMaxHealth(player);
            sendWelcomeMessage(player);
        });

        // ── Déconnexion — sauvegarde ──────────────────────────────
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            savePlayerData(handler.player);
        });

        // ── Mort du joueur — Legs de Fer ──────────────────────────
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_LEGS_DE_FER)) return;

            PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();

            // Appliquer la pénalité Legs de Fer
            HealthSystem.applyLegsDeFeRDeath(player);

            // Message de mort personnalisé
            player.sendMessage(
                Text.literal("☠ Vous êtes mort. Vos affaires se dispersent.")
                    .formatted(Formatting.DARK_RED), false);
            player.sendMessage(
                Text.literal("Vous perdez 5 niveaux. Votre campement vous rappelle à lui.")
                    .formatted(Formatting.RED), false);
        });

        // ── Respawn — téléporter au campement ─────────────────────
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // Copier les données MITE vers le nouveau joueur
            PlayerMiteData oldData = ((MitePlayerDataHolder) oldPlayer).mitefr_getData();
            ((MitePlayerDataHolder) newPlayer).mitefr_setData(oldData);

            // Téléporter au campement si existant
            if (oldData.hasCamp()) {
                BlockPos camp = oldData.getCampPos();
                newPlayer.teleport(camp.getX() + 0.5, camp.getY(), camp.getZ() + 0.5);
                newPlayer.sendMessage(
                    Text.literal("⛺ Vous revenez à votre campement.")
                        .formatted(Formatting.YELLOW), false);
            } else {
                newPlayer.sendMessage(
                    Text.literal("⛺ Vous n'avez pas de campement. Vous renaissez au point de départ.")
                        .formatted(Formatting.GRAY), false);
            }

            HealthSystem.updateMaxHealth(newPlayer);
        });

        // ── Casser un bloc ─────────────────────────────────────────
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity sPlayer)) return true;
            if (!world.getGameRules().getBoolean(MiteFRGameRules.ENABLE_ERA_SYSTEM)) return true;

            // Vérifier si le joueur porte des gants pour la Rouille des Mines
            checkMiningDisease(sPlayer, state.getBlock());

            // Vérifier l'ère pour le minage de minerais spéciaux
            return checkEraMiningAllowed(sPlayer, state.getBlock());
        });

        // ── Tick serveur supplémentaire pour systèmes périodiques ─
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return; // Toutes les secondes
            server.getPlayerManager().getPlayerList().forEach(player -> {
                DiseaseSystem.checkEnvironmentalDiseases(player);
                FoodSystem.tickInventorySpoilage(player);
                HealthSystem.tickXpDebt(player);
            });
        });
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static void loadOrInitPlayerData(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        if (data == null) {
            data = new PlayerMiteData();
            ((MitePlayerDataHolder) player).mitefr_setData(data);
        }
    }

    private static void savePlayerData(ServerPlayerEntity player) {
        // Les données sont sauvegardées via le mixin PlayerEntityMixin
        // qui écrit dans le NBT du joueur à chaque sauvegarde de chunk
    }

    private static void sendWelcomeMessage(ServerPlayerEntity player) {
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(
            Text.literal("  ☠ MITE-FR — Le monde est hostile. Bonne chance.")
                .formatted(Formatting.DARK_RED), false);
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        player.sendMessage(
            Text.literal("  Ère actuelle : " + data.getEra().displayName)
                .formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal(""), false);
    }

    private static void checkMiningDisease(ServerPlayerEntity player,
                                            net.minecraft.block.Block block) {
        // Rouille des Mines : toucher de la pierre/minerai sans gants
        boolean isRockOrOre = block == Blocks.STONE
            || block == Blocks.DEEPSLATE
            || block == Blocks.IRON_ORE
            || block == Blocks.COAL_ORE
            || block == Blocks.COPPER_ORE
            || block == Blocks.GOLD_ORE
            || block == Blocks.DIAMOND_ORE
            || block == Blocks.ANCIENT_DEBRIS;

        if (!isRockOrOre) return;

        ItemStack gloves = player.getInventory().getArmorStack(1); // boots=0, legs=1... main=2=chest=3=head
        // En Java Edition, les slots armure : 0=feet, 1=legs, 2=chest, 3=head
        // Les gants n'existent pas vanilla — on vérifie les bottes comme proxy pour "équipé"
        // Dans MITE-FR on utilise les items custom "Gants en cuir", etc. (voir MiteFRItems)
        // Pour l'instant, si l'item main a le tag mitefr_gloves, on est protégé
        ItemStack held = player.getMainHandStack();
        boolean hasGloves = held.getNbt() != null && held.getNbt().getBoolean("mitefr_gloves");

        if (!hasGloves && Math.random() < 0.003) { // 0.3% par bloc cassé
            PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
            if (!data.hasDisease(Disease.ROUILLE_DES_MINES)) {
                data.addDisease(Disease.ROUILLE_DES_MINES);
                DiseaseSystem.notifyNewDisease(player, Disease.ROUILLE_DES_MINES);
            }
        }
    }

    private static boolean checkEraMiningAllowed(ServerPlayerEntity player,
                                                   net.minecraft.block.Block block) {
        PlayerMiteData data  = ((MitePlayerDataHolder) player).mitefr_getData();
        var era              = data.getEra();

        // Le diamant et l'ancient debris nécessitent l'ère FER minimum
        if ((block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
          || block == Blocks.ANCIENT_DEBRIS) && !era.isAtLeast(fr.mitefr.data.Era.FER)) {
            player.sendMessage(
                Text.literal("✦ Vous n'avez pas les connaissances pour extraire ceci.")
                    .formatted(Formatting.RED), true);
            return false;
        }

        // L'or nécessite l'ère CUIVRE minimum
        if ((block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE)
            && !era.isAtLeast(fr.mitefr.data.Era.CUIVRE)) {
            player.sendMessage(
                Text.literal("✦ Maîtrisez d'abord le cuivre avant l'or.")
                    .formatted(Formatting.RED), true);
            return false;
        }

        return true;
    }
}
