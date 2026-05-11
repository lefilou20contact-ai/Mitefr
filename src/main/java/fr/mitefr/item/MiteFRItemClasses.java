package fr.mitefr.item;

import fr.mitefr.system.FoodSystem;
import fr.mitefr.system.ThirstSystem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

// ══════════════════════════════════════════════════════════════════
// OUTILS SILEX
// ══════════════════════════════════════════════════════════════════

/**
 * Couteau en silex — outil de base de l'Âge du Silex.
 */
class FlintKnifeItem extends Item {
    FlintKnifeItem() {
        super(new FabricItemSettings().maxCount(1).maxDamage(60));
    }
}

/**
 * Hachette en silex — nécessaire pour couper le bois.
 */
class FlintHatchetItem extends Item {
    FlintHatchetItem() {
        super(new FabricItemSettings().maxCount(1).maxDamage(80));
    }

    @Override
    public boolean isSuitableFor(net.minecraft.block.BlockState state) {
        return state.isIn(net.minecraft.registry.tag.BlockTags.AXE_MINEABLE);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, net.minecraft.block.BlockState state) {
        if (state.isIn(net.minecraft.registry.tag.BlockTags.AXE_MINEABLE)) return 4.0f;
        return 1.0f;
    }
}

/**
 * Pic en os — peut miner la pierre basique, pas les minerais.
 */
class BonePickItem extends Item {
    BonePickItem() {
        super(new FabricItemSettings().maxCount(1).maxDamage(40));
    }

    @Override
    public boolean isSuitableFor(net.minecraft.block.BlockState state) {
        return state.isOf(net.minecraft.block.Blocks.STONE)
            || state.isOf(net.minecraft.block.Blocks.COBBLESTONE)
            || state.isOf(net.minecraft.block.Blocks.GRAVEL)
            || state.isOf(net.minecraft.block.Blocks.SANDSTONE);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, net.minecraft.block.BlockState state) {
        if (isSuitableFor(state)) return 2.0f;
        return 1.0f;
    }
}

// ══════════════════════════════════════════════════════════════════
// GOURDES
// ══════════════════════════════════════════════════════════════════

/**
 * Gourde vide — peut être remplie d'eau brute ou filtrée.
 */
class EmptyCanteenItem extends Item {
    EmptyCanteenItem() {
        super(new FabricItemSettings().maxCount(1));
    }
}

/**
 * Gourde filtrée (eau propre) — restaure +6 soif à chaque usage.
 */
class FilteredCanteenItem extends Item {
    FilteredCanteenItem() {
        super(new FabricItemSettings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            ThirstSystem.drinkPureWater(player, 6);
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}

// ══════════════════════════════════════════════════════════════════
// REMÈDES
// ══════════════════════════════════════════════════════════════════

/**
 * Cataplasme d'herbes — soigne la Rouille des Mines et la Fièvre des Marais.
 */
class HerbPoulticeItem extends Item {
    HerbPoulticeItem() {
        super(new FabricItemSettings().maxCount(8));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            var data = ((fr.mitefr.data.MitePlayerDataHolder) player).mitefr_getData();
            boolean healed = false;
            if (data.hasDisease(fr.mitefr.data.Disease.ROUILLE_DES_MINES)) {
                data.removeDisease(fr.mitefr.data.Disease.ROUILLE_DES_MINES);
                healed = true;
            }
            if (data.hasDisease(fr.mitefr.data.Disease.FIEVRE_DES_MARAIS)) {
                data.removeDisease(fr.mitefr.data.Disease.FIEVRE_DES_MARAIS);
                healed = true;
            }
            if (healed) {
                data.modifyCondition(15);
                player.sendMessage(Text.literal("✦ Le cataplasme fait effet.")
                    .formatted(Formatting.GREEN), false);
                ItemStack stack = user.getStackInHand(hand);
                stack.decrement(1);
                return TypedActionResult.success(stack);
            } else {
                player.sendMessage(Text.literal("Vous n'avez pas de maladie que ce remède peut soigner.")
                    .formatted(Formatting.YELLOW), false);
            }
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}

/**
 * Remède contre la Gangrène — urgence absolue.
 */
class AntiGangreneItem extends Item {
    AntiGangreneItem() {
        super(new FabricItemSettings().maxCount(4));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            var data = ((fr.mitefr.data.MitePlayerDataHolder) player).mitefr_getData();
            if (data.hasDisease(fr.mitefr.data.Disease.GANGRENE)) {
                data.removeDisease(fr.mitefr.data.Disease.GANGRENE);
                data.modifyCondition(20);
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.REGENERATION, 200, 1));
                player.sendMessage(Text.literal("✦ La gangrène est arrêtée.")
                    .formatted(Formatting.GREEN), false);
                ItemStack stack = user.getStackInHand(hand);
                stack.decrement(1);
                return TypedActionResult.success(stack);
            }
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}

/**
 * Extrait de citrus — prévient et soigne le Scorbut.
 */
class CitrusExtractItem extends Item {
    CitrusExtractItem() {
        super(new FabricItemSettings().maxCount(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            var data = ((fr.mitefr.data.MitePlayerDataHolder) player).mitefr_getData();
            if (data.hasDisease(fr.mitefr.data.Disease.SCORBUT)) {
                data.removeDisease(fr.mitefr.data.Disease.SCORBUT);
                player.sendMessage(Text.literal("✦ Le scorbut recule.")
                    .formatted(Formatting.GREEN), false);
            }
            FoodSystem.onAteVitaminFood(player);
            ItemStack stack = user.getStackInHand(hand);
            stack.decrement(1);
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}

// ══════════════════════════════════════════════════════════════════
// ÉQUIPEMENT SPÉCIAL
// ══════════════════════════════════════════════════════════════════

/**
 * Masque filtrant — protège des gaz toxiques souterrains.
 */
class GasMaskItem extends Item {
    GasMaskItem() {
        super(new FabricItemSettings().maxCount(1).maxDamage(300));
    }
}

/**
 * Gants de minage — protègent de la Rouille des Mines.
 */
class MiningGlovesItem extends Item {
    MiningGlovesItem() {
        super(new FabricItemSettings().maxCount(1).maxDamage(200));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}

/**
 * Kit de campement — déployable pour créer un point de respawn.
 */
class CampKitItem extends Item {
    CampKitItem() {
        super(new FabricItemSettings().maxCount(4));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            var pos  = player.getBlockPos();
            var dim  = world.getRegistryKey().getValue().toString();
            var data = ((fr.mitefr.data.MitePlayerDataHolder) player).mitefr_getData();

            data.setCamp(pos, dim);
            player.sendMessage(
                Text.literal("⛺ Campement établi ici. Vous renaîtrez à cet endroit si vous mourez.")
                    .formatted(Formatting.YELLOW), false);

            ItemStack stack = user.getStackInHand(hand);
            stack.decrement(1);
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}
