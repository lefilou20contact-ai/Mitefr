package fr.mitefr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.mitefr.data.Disease;
import fr.mitefr.data.Era;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.PlayerMiteData;
import fr.mitefr.network.MiteFRNetwork;
import fr.mitefr.system.EraSystem;
import fr.mitefr.system.HealthSystem;
import fr.mitefr.world.SeasonSystem;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

/**
 * Commandes admin pour MITE-FR.
 *
 * /mitefr status           — Affiche l'état complet du joueur
 * /mitefr era <era_id>     — Change l'ère du joueur
 * /mitefr era advance      — Passe à l'ère suivante
 * /mitefr disease add <id> — Ajoute une maladie
 * /mitefr disease cure     — Guérit toutes les maladies
 * /mitefr season           — Affiche la saison actuelle
 * /mitefr reset            — Remet les stats à zéro (debug)
 */
public final class MiteFRCommands {

    private MiteFRCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> registerAll(dispatcher)
        );
    }

    private static void registerAll(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("mitefr")
                .requires(src -> src.hasPermissionLevel(2))

                // /mitefr status
                .then(literal("status").executes(ctx -> {
                    var src    = ctx.getSource();
                    var player = src.getPlayer();
                    if (player == null) return 0;
                    printStatus(player);
                    return 1;
                }))

                // /mitefr era advance
                .then(literal("era")
                    .then(literal("advance").executes(ctx -> {
                        var player = ctx.getSource().getPlayer();
                        if (player == null) return 0;
                        EraSystem.advanceEra(player);
                        HealthSystem.updateMaxHealth(player);
                        MiteFRNetwork.syncStats(player);
                        return 1;
                    }))
                    .then(literal("set")
                        .then(argument("era_id", StringArgumentType.word())
                            .executes(ctx -> {
                                var player = ctx.getSource().getPlayer();
                                if (player == null) return 0;
                                String eraId = StringArgumentType.getString(ctx, "era_id");
                                Era target = null;
                                for (Era e : Era.values()) {
                                    if (e.id.equals(eraId) || e.name().equalsIgnoreCase(eraId)) {
                                        target = e;
                                        break;
                                    }
                                }
                                if (target == null) {
                                    ctx.getSource().sendFeedback(
                                        () -> Text.literal("Ère inconnue: " + eraId)
                                            .formatted(Formatting.RED), false);
                                    return 0;
                                }
                                final Era finalTarget = target;
                                ((MitePlayerDataHolder) player).mitefr_getData().setEra(finalTarget);
                                HealthSystem.updateMaxHealth(player);
                                MiteFRNetwork.syncStats(player);
                                ctx.getSource().sendFeedback(
                                    () -> Text.literal("Ère définie : " + finalTarget.displayName)
                                        .formatted(Formatting.GREEN), true);
                                return 1;
                            })
                        )
                    )
                )

                // /mitefr disease add <id>
                .then(literal("disease")
                    .then(literal("add")
                        .then(argument("disease_id", StringArgumentType.word())
                            .executes(ctx -> {
                                var player = ctx.getSource().getPlayer();
                                if (player == null) return 0;
                                String id = StringArgumentType.getString(ctx, "disease_id");
                                Disease target = null;
                                for (Disease d : Disease.values()) {
                                    if (d.id.equals(id) || d.name().equalsIgnoreCase(id)) {
                                        target = d;
                                        break;
                                    }
                                }
                                if (target == null) {
                                    ctx.getSource().sendFeedback(
                                        () -> Text.literal("Maladie inconnue: " + id)
                                            .formatted(Formatting.RED), false);
                                    return 0;
                                }
                                final Disease finalDisease = target;
                                ((MitePlayerDataHolder) player).mitefr_getData().addDisease(finalDisease);
                                MiteFRNetwork.syncDiseases(player);
                                ctx.getSource().sendFeedback(
                                    () -> Text.literal("Maladie ajoutée: " + finalDisease.displayName)
                                        .formatted(Formatting.YELLOW), true);
                                return 1;
                            })
                        )
                    )
                    .then(literal("cure").executes(ctx -> {
                        var player = ctx.getSource().getPlayer();
                        if (player == null) return 0;
                        var data = ((MitePlayerDataHolder) player).mitefr_getData();
                        for (Disease d : Disease.values()) data.removeDisease(d);
                        MiteFRNetwork.syncDiseases(player);
                        ctx.getSource().sendFeedback(
                            () -> Text.literal("Toutes les maladies soignées.")
                                .formatted(Formatting.GREEN), true);
                        return 1;
                    }))
                )

                // /mitefr season
                .then(literal("season").executes(ctx -> {
                    var src   = ctx.getSource();
                    ServerWorld sw = src.getWorld();
                    var season = SeasonSystem.getCurrentSeason(sw);
                    long remaining = SeasonSystem.getTicksRemainingInSeason(sw);
                    long days = remaining / 24000;
                    src.sendFeedback(
                        () -> Text.literal("Saison: " + season.displayName
                            + " | Jours restants: " + days)
                            .formatted(Formatting.GOLD), false);
                    return 1;
                }))

                // /mitefr reset
                .then(literal("reset").executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    if (player == null) return 0;
                    ((MitePlayerDataHolder) player).mitefr_setData(new PlayerMiteData());
                    HealthSystem.updateMaxHealth(player);
                    MiteFRNetwork.syncStats(player);
                    ctx.getSource().sendFeedback(
                        () -> Text.literal("Données MITE-FR réinitialisées.")
                            .formatted(Formatting.YELLOW), true);
                    return 1;
                }))
        );
    }

    private static void printStatus(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        var src = player.getCommandSource();

        src.sendFeedback(() -> Text.literal("═══ MITE-FR Status ═══").formatted(Formatting.GOLD), false);
        src.sendFeedback(() -> Text.literal("Ère          : " + data.getEra().displayName).formatted(Formatting.YELLOW), false);
        src.sendFeedback(() -> Text.literal("Condition    : " + data.getCondition() + "/100").formatted(Formatting.AQUA), false);
        src.sendFeedback(() -> Text.literal("Soif         : " + data.getThirst() + "/20").formatted(Formatting.AQUA), false);
        src.sendFeedback(() -> Text.literal("Température  : " + String.format("%.1f", data.getTemperature()) + "°C").formatted(Formatting.AQUA), false);
        src.sendFeedback(() -> Text.literal("Sommeil      : niveau " + data.getSleepDeprivation()).formatted(Formatting.AQUA), false);
        src.sendFeedback(() -> Text.literal("Dette XP     : " + data.getXpDebt()).formatted(Formatting.RED), false);
        src.sendFeedback(() -> Text.literal("Campement    : " + (data.hasCamp() ? data.getCampPos().toShortString() : "Aucun")).formatted(Formatting.WHITE), false);

        if (data.getDiseases().isEmpty()) {
            src.sendFeedback(() -> Text.literal("Maladies     : Aucune").formatted(Formatting.GREEN), false);
        } else {
            src.sendFeedback(() -> Text.literal("Maladies :").formatted(Formatting.RED), false);
            for (Disease d : data.getDiseases()) {
                src.sendFeedback(() -> Text.literal("  ☠ " + d.displayName).formatted(Formatting.RED), false);
            }
        }
        src.sendFeedback(() -> Text.literal("══════════════════════").formatted(Formatting.GOLD), false);
    }
}
