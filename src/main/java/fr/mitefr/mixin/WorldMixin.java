package fr.mitefr.mixin;

import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin sur ServerWorld :
 * - Applique le gaz toxique aux joueurs sous Y=20 sans masque filtrant
 */
@Mixin(ServerWorld.class)
public abstract class WorldMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(java.util.function.BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        if (!world.getGameRules().getBoolean(MiteFRGameRules.ENABLE_ERA_SYSTEM)) return;
        if (world.getTime() % 100 != 0) return; // Toutes les 5 secondes

        for (ServerPlayerEntity player : world.getPlayers()) {
            BlockPos pos = player.getBlockPos();
            if (pos.getY() > 20) continue;
            if (player.isCreative() || player.isSpectator()) continue;

            // Vérifier si le joueur porte un masque filtrant (item custom)
            boolean hasMask = false;
            var helmet = player.getInventory().getArmorStack(3);
            if (!helmet.isEmpty() && helmet.getNbt() != null) {
                hasMask = helmet.getNbt().getBoolean("mitefr_gas_mask");
            }

            if (!hasMask) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.POISON, 120, 0, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NAUSEA, 80, 0, false, false, true));

                var data = ((MitePlayerDataHolder) player).mitefr_getData();
                data.modifyCondition(-2);

                if (world.getTime() % 400 == 0) {
                    player.sendMessage(
                        Text.literal("☠ Des gaz toxiques s'infiltrent ici. Portez un masque filtrant.")
                            .formatted(Formatting.DARK_RED), true);
                }
            }
        }
    }
}
