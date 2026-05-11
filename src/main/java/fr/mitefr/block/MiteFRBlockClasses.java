package fr.mitefr.block;

import fr.mitefr.data.Era;
import fr.mitefr.data.MitePlayerDataHolder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// ══════════════════════════════════════════════════════════════════
// FOYER DE FORGE (Âge du Cuivre)
// ══════════════════════════════════════════════════════════════════

/**
 * Station d'artisanat de l'Âge du Cuivre.
 * Ouvre un menu de forge spécialisé au clic droit.
 */
class ClayForgeBlock extends Block {

    ClayForgeBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity sPlayer)) return ActionResult.PASS;

        var data = ((MitePlayerDataHolder) sPlayer).mitefr_getData();
        if (!data.getEra().isAtLeast(Era.CUIVRE)) {
            sPlayer.sendMessage(
                Text.literal("✦ Vous devez maîtriser l'Âge du Cuivre pour utiliser ceci.")
                    .formatted(Formatting.RED), false);
            return ActionResult.FAIL;
        }

        // Ouvre l'interface de forge (utilise le workbench vanilla pour l'instant)
        // Une interface custom serait la prochaine étape
        sPlayer.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
            (syncId, inv, p) -> new net.minecraft.screen.CraftingScreenHandler(syncId, inv,
                net.minecraft.screen.ScreenHandlerContext.create(world, pos)),
            Text.literal("Foyer de Forge")
        ));

        return ActionResult.SUCCESS;
    }
}

// ══════════════════════════════════════════════════════════════════
// FORGE DU BRONZIER (Âge du Bronze)
// ══════════════════════════════════════════════════════════════════

class BronzeForgeBlock extends Block {

    BronzeForgeBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity sPlayer)) return ActionResult.PASS;

        var data = ((MitePlayerDataHolder) sPlayer).mitefr_getData();
        if (!data.getEra().isAtLeast(Era.BRONZE)) {
            sPlayer.sendMessage(
                Text.literal("✦ L'Âge du Bronze est requis pour cette forge.")
                    .formatted(Formatting.RED), false);
            return ActionResult.FAIL;
        }

        sPlayer.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
            (syncId, inv, p) -> new net.minecraft.screen.CraftingScreenHandler(syncId, inv,
                net.minecraft.screen.ScreenHandlerContext.create(world, pos)),
            Text.literal("Forge du Bronzier")
        ));

        return ActionResult.SUCCESS;
    }
}

// ══════════════════════════════════════════════════════════════════
// ENCLUME DE MAÎTRE (Âge du Fer)
// ══════════════════════════════════════════════════════════════════

class MasterAnvilBlock extends Block {

    MasterAnvilBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity sPlayer)) return ActionResult.PASS;

        var data = ((MitePlayerDataHolder) sPlayer).mitefr_getData();
        if (!data.getEra().isAtLeast(Era.FER)) {
            sPlayer.sendMessage(
                Text.literal("✦ L'Âge du Fer est requis pour l'Enclume de Maître.")
                    .formatted(Formatting.RED), false);
            return ActionResult.FAIL;
        }

        // L'Enclume de Maître permet aussi les enchantements
        sPlayer.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
            (syncId, inv, p) -> new net.minecraft.screen.AnvilScreenHandler(syncId, inv,
                net.minecraft.screen.ScreenHandlerContext.create(world, pos)),
            Text.literal("Enclume de Maître")
        ));

        return ActionResult.SUCCESS;
    }
}

// ══════════════════════════════════════════════════════════════════
// ATELIER DU NÉANT (Âge du Néant)
// ══════════════════════════════════════════════════════════════════

class VoidWorkshopBlock extends Block {

    VoidWorkshopBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity sPlayer)) return ActionResult.PASS;

        var data = ((MitePlayerDataHolder) sPlayer).mitefr_getData();
        if (!data.getEra().isAtLeast(Era.NEANT)) {
            sPlayer.sendMessage(
                Text.literal("✦ L'Âge du Néant est requis. Vous en êtes loin...")
                    .formatted(Formatting.DARK_PURPLE), false);
            return ActionResult.FAIL;
        }

        sPlayer.sendMessage(
            Text.literal("✦ L'Atelier du Néant résonne de puissance obscure.")
                .formatted(Formatting.DARK_PURPLE), false);

        sPlayer.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
            (syncId, inv, p) -> new net.minecraft.screen.CraftingScreenHandler(syncId, inv,
                net.minecraft.screen.ScreenHandlerContext.create(world, pos)),
            Text.literal("✦ Atelier du Néant ✦")
        ));

        return ActionResult.SUCCESS;
    }
}
