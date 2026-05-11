package fr.mitefr.system;

import fr.mitefr.MiteFRConstants;
import fr.mitefr.data.Disease;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import fr.mitefr.data.PlayerMiteData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Système alimentaire avancé :
 * - Péremption des aliments (3 jours de jeu = 72 000 ticks)
 * - Suivi de la diversité alimentaire (anti-scorbut)
 */
public final class FoodSystem {

    public static final long SPOIL_TICKS = 72_000L;
    public static final String NBT_AGE = MiteFRConstants.NBT_FOOD_AGE;
    private static final long SCURVY_THRESHOLD = 120_000L;

    private FoodSystem() {}

    /**
     * Fait vieillir tous les aliments de l'inventaire du joueur.
     * Appelé toutes les 20 ticks depuis le tick serveur principal.
     */
    public static void tickInventoryAging(ServerPlayerEntity player) {
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_FOOD_SPOILAGE)) return;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty() || !stack.getItem().isFood()) continue;

            NbtCompound nbt = stack.getOrCreateNbt();
            long age = nbt.getLong(NBT_AGE);
            nbt.putLong(NBT_AGE, age + 20L);

            if (age >= SPOIL_TICKS && !nbt.getBoolean("mitefr_spoiled")) {
                nbt.putBoolean("mitefr_spoiled", true);
                stack.setCustomName(
                    Text.literal("[Avarie] ").formatted(Formatting.DARK_RED)
                        .append(stack.getName().copy().formatted(Formatting.GRAY))
                );
                player.sendMessage(
                    Text.literal("⚠ Un aliment dans votre inventaire s'est avarié !")
                        .formatted(Formatting.YELLOW),
                    true
                );
            }
        }
    }

    /**
     * Alias de tickInventoryAging — appelé depuis MiteFREvents.
     */
    public static void tickInventorySpoilage(ServerPlayerEntity player) {
        tickInventoryAging(player);
    }

    /**
     * Vérifie si un aliment est avarié.
     */
    public static boolean isSpoiled(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasNbt()) return false;
        return stack.getNbt().getBoolean("mitefr_spoiled");
    }

    /**
     * Marque un item comme fraîchement créé/cuisiné.
     */
    public static void markFresh(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putLong(NBT_AGE, 0L);
        nbt.putBoolean("mitefr_spoiled", false);
    }

    /**
     * Appelé quand le joueur mange un aliment.
     * Gère le suivi anti-scorbut.
     */
    public static void onFoodEaten(ServerPlayerEntity player, ItemStack stack) {
        if (isVitaminFood(stack)) {
            onAteVitaminFood(player);
        }
    }

    /**
     * Notifie que le joueur a mangé un aliment contenant de la vitamine C.
     */
    public static void onAteVitaminFood(ServerPlayerEntity player) {
        PlayerMiteData miteData = ((MitePlayerDataHolder) player).mitefr_getData();
        if (miteData.hasDisease(Disease.SCORBUT)) {
            miteData.removeDisease(Disease.SCORBUT);
            player.sendMessage(
                Text.literal("✦ Le scorbut régresse grâce à votre alimentation.")
                    .formatted(Formatting.GREEN),
                false
            );
        }
    }

    /**
     * Tick du compteur scorbut (appelé toutes les 200 ticks depuis DiseaseSystem).
     */
    public static void tickScurvyCounter(ServerPlayerEntity player) {
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_DISEASES)) return;
        // Détection du scorbut via PlayerMiteData (implémentation future)
    }

    /**
     * Vérifie si un aliment contient de la vitamine C.
     */
    public static boolean isVitaminFood(ItemStack stack) {
        var item = stack.getItem();
        return item == net.minecraft.item.Items.APPLE
            || item == net.minecraft.item.Items.GOLDEN_APPLE
            || item == net.minecraft.item.Items.ENCHANTED_GOLDEN_APPLE
            || item == net.minecraft.item.Items.MELON_SLICE
            || item == net.minecraft.item.Items.SWEET_BERRIES
            || item == net.minecraft.item.Items.GLOW_BERRIES
            || item == net.minecraft.item.Items.PUMPKIN_PIE;
    }
}
