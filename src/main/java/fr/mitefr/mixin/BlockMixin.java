package fr.mitefr.mixin;

import fr.mitefr.data.Era;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin sur Block :
 * - Empêche de casser des blocs à mains nues quand un outil est requis
 * - Les logs nécessitent une hache, la pierre un pic
 */
@Mixin(Block.class)
public abstract class BlockMixin {

    private static final java.util.Set<Block> LOG_BLOCKS = java.util.Set.of(
        Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG,
        Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG,
        Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG, Blocks.BAMBOO_BLOCK,
        Blocks.OAK_WOOD, Blocks.SPRUCE_WOOD, Blocks.BIRCH_WOOD,
        Blocks.JUNGLE_WOOD, Blocks.ACACIA_WOOD, Blocks.DARK_OAK_WOOD
    );

    private static final java.util.Set<Block> STONE_BLOCKS = java.util.Set.of(
        Blocks.STONE, Blocks.COBBLESTONE, Blocks.DEEPSLATE,
        Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE,
        Blocks.SANDSTONE, Blocks.NETHERRACK
    );

    /**
     * Annule le cassage de blocs si :
     * - C'est un log et le joueur n'a pas de hache
     * - C'est de la pierre et le joueur n'a pas de pic
     */
    @Inject(method = "calcBlockBreakingDelta", at = @At("HEAD"), cancellable = true)
    private void onCalcBreakingDelta(BlockState state, PlayerEntity player,
                                      net.minecraft.world.BlockView world,
                                      BlockPos pos,
                                      CallbackInfoReturnable<Float> cir) {
        if (player.getWorld().isClient()) return;
        if (!(player instanceof ServerPlayerEntity sPlayer)) return;
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_ERA_SYSTEM)) return;

        Block block = state.getBlock();
        ItemStack held = player.getMainHandStack();

        boolean isLog   = LOG_BLOCKS.contains(block);
        boolean isStone = STONE_BLOCKS.contains(block);

        if (isLog && !isAxe(held)) {
            // Sans hache : vitesse de cassage x0 (impossible)
            cir.setReturnValue(-1.0f);
            if (player.getWorld().getTime() % 40 == 0) {
                sPlayer.sendMessage(
                    Text.literal("✦ Vous avez besoin d'une hache pour couper du bois.")
                        .formatted(Formatting.RED), true);
            }
            return;
        }

        if (isStone && !isPickaxe(held)) {
            cir.setReturnValue(-1.0f);
            if (player.getWorld().getTime() % 40 == 0) {
                sPlayer.sendMessage(
                    Text.literal("✦ Vous avez besoin d'un pic pour miner de la pierre.")
                        .formatted(Formatting.RED), true);
            }
        }
    }

    private static boolean isAxe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = net.minecraft.registry.Registries.ITEM
            .getId(stack.getItem()).toString();
        return id.contains("_axe") || stack.getNbt() != null
            && stack.getNbt().getBoolean("mitefr_is_axe");
    }

    private static boolean isPickaxe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = net.minecraft.registry.Registries.ITEM
            .getId(stack.getItem()).toString();
        return id.contains("_pickaxe") || stack.getNbt() != null
            && stack.getNbt().getBoolean("mitefr_is_pickaxe");
    }
}
