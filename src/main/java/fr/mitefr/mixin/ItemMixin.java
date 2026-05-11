package fr.mitefr.mixin;

import fr.mitefr.item.MiteFRItems;
import fr.mitefr.system.ThirstSystem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin sur Item :
 * - Intercepte l'usage du seau d'eau pour déclencher le système de soif
 * - L'eau du seau vanilla = eau sale (risque d'intoxication)
 */
@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand,
                       CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient()) return;
        if (!(user instanceof ServerPlayerEntity player)) return;

        ItemStack stack = user.getStackInHand(hand);
        Item item = stack.getItem();

        // Seau d'eau vanilla → eau sale
        if (item == Items.WATER_BUCKET) {
            ThirstSystem.drinkDirtyWater(player);
            return;
        }

        // Gourde filtrée MITE-FR → eau propre
        if (item == MiteFRItems.FILTERED_CANTEEN) {
            ThirstSystem.drinkPureWater(player, 6);
            NbtCompound nbt = stack.getOrCreateNbt();
            int charges = nbt.getInt("charges");
            if (charges <= 1) {
                user.setStackInHand(hand, new ItemStack(MiteFRItems.EMPTY_CANTEEN));
            } else {
                nbt.putInt("charges", charges - 1);
            }
            cir.setReturnValue(TypedActionResult.success(stack));
        }
    }
}
