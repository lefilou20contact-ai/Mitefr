package fr.mitefr.mixin;

import fr.mitefr.system.FoodSystem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Mixin sur Item pour les aliments :
 * - Les aliments crus ont 50% de chance d'empoisonner
 * - Gère la péremption à la consommation
 * - Déclenche FoodSystem.onFoodEaten()
 */
@Mixin(Item.class)
public abstract class FoodComponentMixin {

    /** Aliments crus qui empoisonnent à 50% */
    private static final Set<Item> RAW_FOODS = Set.of(
        Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.CHICKEN,
        Items.RABBIT, Items.COD, Items.SALMON, Items.TROPICAL_FISH
    );

    @Inject(method = "finishUsing", at = @At("TAIL"))
    private void onFinishUsing(ItemStack stack, World world, LivingEntity user,
                                CallbackInfo ci) {
        if (world.isClient()) return;
        if (!(user instanceof ServerPlayerEntity player)) return;
        if (stack.getItem().getFoodComponent() == null) return;

        // Aliment pourri
        if (FoodSystem.isSpoiled(stack)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.POISON, 600, 1, false, true, true));
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NAUSEA, 300, 0, false, true, true));
            return;
        }

        // Aliment cru → 50% empoisonnement
        if (RAW_FOODS.contains(stack.getItem()) && Math.random() < 0.5) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.POISON, 200, 0, false, true, true));
        }

        // Déclencher le système de Scorbut / légumes
        FoodSystem.onFoodEaten(player, stack);
    }
}
